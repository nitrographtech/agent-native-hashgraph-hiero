#!/usr/bin/env bash
# SPDX-License-Identifier: Apache-2.0
set -euo pipefail

repo_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
policy="$repo_root/tools/p07/verify-standalone-execution-removed.sh"
jar_bin=${JAVA_HOME:+$JAVA_HOME/bin/jar}
if [[ -z "$jar_bin" || ! -x "$jar_bin" ]]; then
  jar_bin=$(command -v jar)
fi
tmp_root=$(mktemp -d)
trap 'rm -rf "$tmp_root"' EXIT

expect_rejection() {
  local label=$1
  if P07_REPO_ROOT="$tmp_root/repo" P07_ARTIFACT_ROOT="$tmp_root/artifacts" P07_JAR_BIN="$jar_bin" \
    "$policy" >/dev/null 2>&1; then
    printf 'intentional-failure control was not rejected: %s\n' "$label" >&2
    exit 1
  fi
}

reset_fixture() {
  rm -rf "$tmp_root/repo" "$tmp_root/artifacts"
  mkdir -p "$tmp_root/repo/module/src/main/java" "$tmp_root/artifacts"
}

reset_fixture
mkdir -p "$tmp_root/repo/module/src/main/java/com/hedera/node/app/workflows/standalone"
printf 'package com.hedera.node.app.workflows.standalone; public class TransactionExecutors {}\n' \
  >"$tmp_root/repo/module/src/main/java/com/hedera/node/app/workflows/standalone/TransactionExecutors.java"
expect_rejection source

reset_fixture
mkdir -p "$tmp_root/classes/com/hedera/node/app/workflows/standalone"
printf 'x' >"$tmp_root/classes/com/hedera/node/app/workflows/standalone/TransactionExecutors.class"
(cd "$tmp_root/classes" && "$jar_bin" cf "$tmp_root/artifacts/injected.jar" .)
expect_rejection class
rm -rf "$tmp_root/classes"

reset_fixture
mkdir -p "$tmp_root/services/META-INF/services"
printf 'com.hedera.node.app.workflows.standalone.TransactionExecutors\n' \
  >"$tmp_root/services/META-INF/services/example.Service"
(cd "$tmp_root/services" && "$jar_bin" cf "$tmp_root/artifacts/service.jar" .)
expect_rejection service
rm -rf "$tmp_root/services"

reset_fixture
printf 'module injected { exports com.hedera.node.app.workflows.standalone; }\n' \
  >"$tmp_root/repo/module/src/main/java/module-info.java"
expect_rejection module

printf 'P07 standalone-removal intentional-failure controls passed\n'
