#!/usr/bin/env bash
# SPDX-License-Identifier: Apache-2.0
set -euo pipefail

repo_root=${P07_REPO_ROOT:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}
tooling_root="$repo_root/hedera-node/fixture-tooling"
artifact_roots=${P07_RUNTIME_ARTIFACT_ROOTS:-"$repo_root/hedera-node/hedera-app/build/distributions"}
jar_bin=${P07_JAR_BIN:-${JAVA_HOME:+$JAVA_HOME/bin/jar}}
rg_bin=${P07_RG_BIN:-$(command -v rg || true)}
failures=0

if [[ -z "$jar_bin" || ! -x "$jar_bin" ]]; then jar_bin=$(command -v jar || true); fi
[[ -n "$jar_bin" ]] || {
  printf 'P07 fixture action-tracing policy requires the JDK jar tool\n' >&2
  exit 2
}

fail() {
  printf 'P07 fixture action-tracing isolation failure: %s\n' "$*" >&2
  failures=$((failures + 1))
}

readonly tracer_source_pattern='class (EvmActionTracer|ActionStack|ActionWrapper|ActionsHelper|FixtureActionTracerFactory)'
readonly tracer_path_pattern='com/hedera/services/bdd/fixturetooling/tracing/'
readonly tooling_edge_pattern='(implementation|api|runtimeOnly|compileOnly|requires).*fixture[-.]tooling|fixture-execution-support'
readonly dagger_pattern='@(Binds|Provides).*(FixtureActionTracer|EvmActionTracer)|FixtureActionTracerFactory'
readonly service_pattern='com\.hedera\.services\.bdd\.fixturetooling\.tracing\.FixtureActionTracerFactory'

if [[ -n "$rg_bin" ]]; then
  source_hits=$("$rg_bin" -n "$tracer_source_pattern" "$repo_root" \
    --glob '**/src/main/**' --glob '!hedera-node/fixture-tooling/**' --glob '!**/build/**' || true)
  edge_hits=$("$rg_bin" -n "$tooling_edge_pattern" "$repo_root" \
    --glob '**/src/main/java/module-info.java' --glob '**/build.gradle.kts' \
    --glob '!hedera-node/fixture-tooling/**' --glob '!**/build/**' |
    grep -v 'P07-3A TEST_ONLY' || true)
  dagger_hits=$("$rg_bin" -n "$dagger_pattern" "$repo_root" \
    --glob '**/src/main/**' --glob '!hedera-node/fixture-tooling/**' --glob '!**/build/**' || true)
  service_hits=$("$rg_bin" -n "$service_pattern" "$repo_root" \
    --glob '**/src/main/resources/META-INF/services/**' \
    --glob '!hedera-node/fixture-tooling/**' --glob '!**/build/**' || true)
else
  source_hits=$(find "$repo_root" -path '*/src/main/*' -type f \
    ! -path "$tooling_root/*" ! -path '*/build/*' -print0 |
    xargs -0 grep -En "$tracer_source_pattern" 2>/dev/null || true)
  edge_hits=""
  dagger_hits=""
  service_hits=""
fi

[[ -z "$source_hits" ]] || fail "fixture tracer implementation outside fixture tooling:\n$source_hits"
[[ -z "$edge_hits" ]] || fail "runtime build or JPMS edge to fixture tracing:\n$edge_hits"
[[ -z "$dagger_hits" ]] || fail "runtime Dagger binding to fixture tracing:\n$dagger_hits"
[[ -z "$service_hits" ]] || fail "runtime service metadata exposes fixture tracing:\n$service_hits"

IFS=':' read -r -a roots <<<"$artifact_roots"
for artifact_root in "${roots[@]}"; do
  [[ -d "$artifact_root" ]] || continue
  while IFS= read -r -d '' jar_file; do
    jar_hits=$("$jar_bin" tf "$jar_file" 2>/dev/null | grep -E "$tracer_path_pattern" || true)
    [[ -z "$jar_hits" ]] || fail "fixture tracer packaged in runtime jar $jar_file:\n$jar_hits"

    service_hits=$(
      unzip -p "$jar_file" 'META-INF/services/*' 2>/dev/null |
        grep -En "$service_pattern" || true
    )
    [[ -z "$service_hits" ]] ||
      fail "fixture tracer service packaged in runtime jar $jar_file:\n$service_hits"
  done < <(find "$artifact_root" -type f -name '*.jar' -print0)
done

if [[ -d "$tooling_root/src/main/java" ]]; then
  tooling_sources=$(grep -REl "$tracer_source_pattern" "$tooling_root/src/main/java" 2>/dev/null || true)
  [[ -n "$tooling_sources" ]] || fail "fixture tooling does not own a live action tracer"
fi

if (( failures > 0 )); then exit 1; fi
printf 'P07 fixture action-tracing isolation policy passed\n'
