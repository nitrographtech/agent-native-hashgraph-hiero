#!/usr/bin/env bash
# SPDX-License-Identifier: Apache-2.0
set -euo pipefail

repo_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
policy="$repo_root/tools/p07/verify-executable-contract-engine-removed.sh"
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

mkdir -p "$scratch/repo/hedera-node/hedera-app/src/main/java/example"
printf '%s\n' 'package example; class HederaEVM {}' \
  >"$scratch/repo/hedera-node/hedera-app/src/main/java/example/Injected.java"
expect_rejection source env P07_REPO_ROOT="$scratch/repo" P07_ARTIFACT_ROOT="$scratch/empty" "$policy"

find "$scratch" -mindepth 1 -depth -delete
mkdir -p "$scratch/repo/hedera-node/hedera-app/src/main/java" \
  "$scratch/classes/com/hedera/node/app/service/contract/impl/hevm"
touch "$scratch/classes/com/hedera/node/app/service/contract/impl/hevm/HederaEVM.class"
(cd "$scratch/classes" && "$JAVA_HOME/bin/jar" cf "$scratch/injected.jar" .)
expect_rejection class env P07_REPO_ROOT="$scratch/repo" P07_ARTIFACT_ROOT="$scratch" "$policy"

find "$scratch/classes" -depth -delete
mkdir -p "$scratch/classes/META-INF/services"
printf '%s\n' 'example.FullContractRuntimeProviderFactory' \
  >"$scratch/classes/META-INF/services/example.Factory"
(cd "$scratch/classes" && "$JAVA_HOME/bin/jar" cf "$scratch/service.jar" .)
expect_rejection service env P07_REPO_ROOT="$scratch/repo" P07_ARTIFACT_ROOT="$scratch" "$policy"

find "$scratch" -mindepth 1 -depth -delete
mkdir -p "$scratch/repo/hedera-node/hedera-app/src/main/java"
printf '%s\n' 'module injected { uses example.ActionSidecarContentTracer; }' \
  >"$scratch/repo/hedera-node/hedera-app/src/main/java/module-info.java"
expect_rejection module env P07_REPO_ROOT="$scratch/repo" P07_ARTIFACT_ROOT="$scratch/empty" "$policy"

find "$scratch" -mindepth 1 -depth -delete
mkdir -p "$scratch/repo/hedera-node/hedera-app/src/main/java" \
  "$scratch/repo/hedera-node/configuration/dev"
printf '%s\n' 'contracts.enabled=true' \
  >"$scratch/repo/hedera-node/configuration/dev/application.properties"
expect_rejection configuration env P07_REPO_ROOT="$scratch/repo" P07_ARTIFACT_ROOT="$scratch/empty" "$policy"

printf 'P07 executable-engine intentional-failure controls passed\n'
