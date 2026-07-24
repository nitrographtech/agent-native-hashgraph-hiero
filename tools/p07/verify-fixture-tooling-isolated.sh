#!/usr/bin/env bash
# SPDX-License-Identifier: Apache-2.0
set -euo pipefail

repo_root=${P07_REPO_ROOT:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}
fixture_root="$repo_root/hedera-node/fixture-tooling"
artifact_roots=${P07_RUNTIME_ARTIFACT_ROOTS:-"$repo_root/hedera-node/hedera-app/build"}
custom_artifact_roots=${P07_RUNTIME_ARTIFACT_ROOTS:+true}
jar_bin=${P07_JAR_BIN:-}
failures=0

if [[ -z "$jar_bin" && -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/jar" ]]; then
  jar_bin="$JAVA_HOME/bin/jar"
fi
if [[ -z "$jar_bin" ]]; then
  jar_bin=$(command -v jar || true)
fi
[[ -n "$jar_bin" ]] || {
  printf 'P07 fixture-tooling policy requires the JDK jar tool\n' >&2
  exit 2
}
unzip_bin=$(command -v unzip || true)

list_jar() {
  if [[ -n "$unzip_bin" ]]; then
    "$unzip_bin" -Z1 "$1"
  else
    "$jar_bin" tf "$1"
  fi
}

read_jar_entry() {
  local jar_file=$1
  local entry=$2
  if [[ -n "$unzip_bin" ]]; then
    "$unzip_bin" -p "$jar_file" "$entry"
  else
    local extract_dir
    extract_dir=$(mktemp -d)
    (cd "$extract_dir" && "$jar_bin" xf "$jar_file" "$entry")
    command cat "$extract_dir/$entry"
    find "$extract_dir" -depth -delete
  fi
}

fail() {
  printf 'P07 fixture-tooling policy failure: %s\n' "$*" >&2
  failures=$((failures + 1))
}

production_hits=$(rg -n \
  'class (HistoricalContractStateFixtureCreation|P06aFixtureIdentityGenerator)|fixturetooling\.p06a' \
  "$repo_root" \
  --glob '**/src/main/**' \
  --glob '!hedera-node/fixture-tooling/**' \
  --glob '!**/build/**' || true)
[[ -z "$production_hits" ]] || fail "fixture-only entry point outside fixture-tooling:\n$production_hits"

module_hits=$(rg -n \
  '(requires|uses|provides).*(fixture\.tooling|fixturetooling)' \
  "$repo_root" \
  --glob '**/src/main/java/module-info.java' \
  --glob '!hedera-node/fixture-tooling/**' || true)
[[ -z "$module_hits" ]] || fail "production JPMS edge to fixture tooling:\n$module_hits"

service_hits=$(rg -n \
  'HistoricalContractStateFixtureCreation|P06aFixtureIdentityGenerator|fixturetooling\.p06a' \
  "$repo_root" \
  --glob '**/src/main/resources/META-INF/services/**' \
  --glob '!hedera-node/fixture-tooling/**' || true)
[[ -z "$service_hits" ]] || fail "runtime service metadata exposes fixture tooling:\n$service_hits"

if [[ -d "$fixture_root/src/main/java" ]]; then
  runtime_edges=$(rg -n \
    'com\\.hedera\\.node\\.app\\.(Hedera|HederaNode)|HistoricalContractRuntimeProvider|FullContractRuntimeProvider|ContractServiceImpl' \
    "$fixture_root/src/main/java" || true)
  [[ -z "$runtime_edges" ]] || fail "fixture tooling embeds node/runtime implementation:\n$runtime_edges"
fi

IFS=':' read -r -a roots <<<"$artifact_roots"
for artifact_root in "${roots[@]}"; do
  [[ -d "$artifact_root" ]] || continue
  while IFS= read -r -d '' jar_file; do
    case "$jar_file" in
      "$fixture_root"/*) continue ;;
    esac
    jar_hits=$(list_jar "$jar_file" | rg \
      'com/hedera/services/bdd/fixturetooling/|HistoricalContractStateFixtureCreation|P06aFixtureIdentityGenerator' || true)
    [[ -z "$jar_hits" ]] || fail "fixture tooling packaged in runtime jar $jar_file:\n$jar_hits"

    entries=$(list_jar "$jar_file" | rg '^META-INF/services/[^/]+$' || true)
    if [[ -n "$entries" ]]; then
      packaged_services=$(
        while IFS= read -r entry; do read_jar_entry "$jar_file" "$entry"; done <<<"$entries" |
          rg -n 'HistoricalContractStateFixtureCreation|P06aFixtureIdentityGenerator|fixturetooling\.p06a' ||
          true
      )
      [[ -z "$packaged_services" ]] ||
        fail "fixture service provider packaged in $jar_file:\n$packaged_services"
    fi
  done < <(
    if [[ "$custom_artifact_roots" == true ]]; then
      find "$artifact_root" -type f -name '*.jar' -print0
    else
      find "$artifact_root/libs" "$artifact_root/distributions" \
        -type f \
        \( -path '*/build/libs/*.jar' -o -path '*/data/apps/*.jar' -o -iname '*fixture*tooling*.jar' \) \
        -print0 2>/dev/null
    fi
  )
done

if (( failures > 0 )); then
  exit 1
fi

printf 'P07 fixture-tooling isolation policy passed\n'
