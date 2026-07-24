#!/usr/bin/env bash
# SPDX-License-Identifier: Apache-2.0
set -euo pipefail

repo_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
policy="$repo_root/tools/p07/verify-fixture-tooling-isolated.sh"
jar_bin=${JAVA_HOME:+$JAVA_HOME/bin/jar}
if [[ -z "$jar_bin" || ! -x "$jar_bin" ]]; then jar_bin=$(command -v jar); fi
tmp_root=$(mktemp -d)
trap 'rm -rf "$tmp_root"' EXIT

expect_rejection() {
  local label=$1
  if P07_REPO_ROOT="$tmp_root/repo" \
    P07_RUNTIME_ARTIFACT_ROOTS="$tmp_root/artifacts" \
    P07_JAR_BIN="$jar_bin" "$policy" >/dev/null 2>&1; then
    printf 'intentional-failure control was not rejected: %s\n' "$label" >&2
    exit 1
  fi
}

reset_fixture() {
  rm -rf "$tmp_root/repo" "$tmp_root/artifacts" "$tmp_root/classes"
  mkdir -p "$tmp_root/repo/runtime/src/main/java" "$tmp_root/artifacts"
}

reset_fixture
printf 'package runtime; public class HistoricalContractStateFixtureCreation {}\n' \
  >"$tmp_root/repo/runtime/src/main/java/HistoricalContractStateFixtureCreation.java"
expect_rejection source

for target in native full; do
  reset_fixture
  mkdir -p "$tmp_root/classes/com/hedera/services/bdd/fixturetooling/p06a"
  printf 'x' >"$tmp_root/classes/com/hedera/services/bdd/fixturetooling/p06a/P06aFixtureIdentityGenerator.class"
  (cd "$tmp_root/classes" && "$jar_bin" cf "$tmp_root/artifacts/$target.jar" .)
  expect_rejection "$target-class"
done

reset_fixture
mkdir -p "$tmp_root/classes/META-INF/services"
printf 'com.hedera.services.bdd.fixturetooling.p06a.P06aFixtureIdentityGenerator\n' \
  >"$tmp_root/classes/META-INF/services/example.Service"
(cd "$tmp_root/classes" && "$jar_bin" cf "$tmp_root/artifacts/service.jar" .)
expect_rejection service

reset_fixture
printf 'module injected { requires com.hedera.node.fixture.tooling; }\n' \
  >"$tmp_root/repo/runtime/src/main/java/module-info.java"
expect_rejection module

printf 'P07 fixture-tooling intentional-failure controls passed\n'
