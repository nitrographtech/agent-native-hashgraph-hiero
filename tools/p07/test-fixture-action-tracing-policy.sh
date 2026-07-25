#!/usr/bin/env bash
# SPDX-License-Identifier: Apache-2.0
set -euo pipefail

repo_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
policy="$repo_root/tools/p07/verify-fixture-action-tracing-isolated.sh"
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
  mkdir -p "$tmp_root/repo/hedera-node/fixture-tooling/src/main/java" \
    "$tmp_root/repo/runtime/src/main/java" "$tmp_root/repo/docs/nitrograph" \
    "$tmp_root/artifacts" "$tmp_root/classes"
  printf 'p07-executable-fixture-compat-v1\n' \
    >"$tmp_root/repo/docs/nitrograph/P07_EXECUTABLE_FIXTURE_COMPATIBILITY_RELEASE.md"
}

reset_fixture
printf 'public class EvmActionTracer {}\n' \
  >"$tmp_root/repo/runtime/src/main/java/EvmActionTracer.java"
expect_rejection source

for target in native full; do
  reset_fixture
  mkdir -p "$tmp_root/classes/com/hedera/services/bdd/fixturetooling/tracing"
  printf 'x' >"$tmp_root/classes/com/hedera/services/bdd/fixturetooling/tracing/EvmActionTracer.class"
  (cd "$tmp_root/classes" && "$jar_bin" cf "$tmp_root/artifacts/$target.jar" .)
  expect_rejection "$target-class"
done

reset_fixture
printf 'implementation(project(":fixture-tooling"))\n' \
  >"$tmp_root/repo/runtime/build.gradle.kts"
expect_rejection dependency

reset_fixture
printf 'module runtime { requires com.hedera.node.fixture.tooling; }\n' \
  >"$tmp_root/repo/runtime/src/main/java/module-info.java"
expect_rejection module

reset_fixture
printf '@Provides FixtureActionTracerFactory tracer() { return null; }\n' \
  >"$tmp_root/repo/runtime/src/main/java/RuntimeModule.java"
expect_rejection dagger

reset_fixture
mkdir -p "$tmp_root/classes/META-INF/services"
printf 'com.hedera.services.bdd.fixturetooling.tracing.FixtureActionTracerFactory\n' \
  >"$tmp_root/classes/META-INF/services/example.Factory"
(cd "$tmp_root/classes" && "$jar_bin" cf "$tmp_root/artifacts/service.jar" .)
expect_rejection service

printf 'P07 fixture action-tracing intentional-failure controls passed\n'
