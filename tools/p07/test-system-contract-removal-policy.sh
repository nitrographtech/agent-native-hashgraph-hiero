#!/usr/bin/env bash
# SPDX-License-Identifier: Apache-2.0
set -euo pipefail

repo_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
policy="$repo_root/tools/p07/verify-system-contracts-removed.sh"
scratch=$(mktemp -d)
trap 'find "$scratch" -depth -delete' EXIT

expect_rejection() {
  local name=$1
  shift
  if "$@" >/dev/null 2>&1; then
    printf 'P07 policy control failed to reject %s\n' "$name" >&2
    exit 1
  fi
}

mkdir -p "$scratch/repo/module/src/main/java/example"
printf '%s\n' 'package example; import com.hedera.node.app.service.contract.impl.exec.systemcontracts.HtsSystemContract;' \
  >"$scratch/repo/module/src/main/java/example/Injected.java"
expect_rejection 'source injection' env P07_REPO_ROOT="$scratch/repo" P07_ARTIFACT_ROOT="$scratch/empty" "$policy"

find "$scratch/repo" -depth -delete
mkdir -p "$scratch/repo/module/src/main/java" "$scratch/classes/com/hedera/node/app/service/contract/impl/exec/systemcontracts"
touch "$scratch/classes/com/hedera/node/app/service/contract/impl/exec/systemcontracts/Injected.class"
(cd "$scratch/classes" && "$JAVA_HOME/bin/jar" cf "$scratch/injected.jar" .)
expect_rejection 'class injection' env P07_REPO_ROOT="$scratch/repo" P07_ARTIFACT_ROOT="$scratch" "$policy"

find "$scratch/classes" -depth -delete
mkdir -p "$scratch/classes/META-INF/services"
printf '%s\n' 'com.hedera.node.app.service.contract.impl.exec.systemcontracts.InjectedProvider' \
  >"$scratch/classes/META-INF/services/example.Service"
(cd "$scratch/classes" && "$JAVA_HOME/bin/jar" cf "$scratch/service.jar" .)
expect_rejection 'service injection' env P07_REPO_ROOT="$scratch/repo" P07_ARTIFACT_ROOT="$scratch" "$policy"

find "$scratch" -mindepth 1 -depth -delete
mkdir -p "$scratch/repo/module/src/main/java"
printf '%s\n' 'module injected { exports com.hedera.node.app.service.contract.impl.exec.systemcontracts; }' \
  >"$scratch/repo/module/src/main/java/module-info.java"
expect_rejection 'JPMS injection' env P07_REPO_ROOT="$scratch/repo" P07_ARTIFACT_ROOT="$scratch/empty" "$policy"

printf 'P07 system-contract removal intentional-failure controls passed\n'
