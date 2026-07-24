#!/usr/bin/env bash
# SPDX-License-Identifier: Apache-2.0
set -euo pipefail

repo_root=${P07_REPO_ROOT:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}
artifact_root=${P07_ARTIFACT_ROOT:-"$repo_root/hedera-node/hedera-app/build"}
failures=0
jar_bin=${P07_JAR_BIN:-}
rg_bin=${P07_RG_BIN-$(command -v rg || true)}
if [[ -z "$jar_bin" && -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/jar" ]]; then
  jar_bin="$JAVA_HOME/bin/jar"
fi
if [[ -z "$jar_bin" ]]; then
  jar_bin=$(command -v jar || true)
fi
[[ -n "$jar_bin" ]] || {
  printf 'P07 standalone-removal policy requires the JDK jar tool\n' >&2
  exit 2
}

fail() {
  printf 'P07 standalone-removal policy failure: %s\n' "$*" >&2
  failures=$((failures + 1))
}

readonly source_pattern='workflows\.standalone|class TransactionExecutors|interface TransactionExecutor|StandaloneFeeCalculatorImpl|StandaloneDispatchFactory|StandaloneModule|StandaloneNetworkInfo|NoopVerificationStrategies'
readonly module_pattern='(exports|opens|requires|uses|provides).*(workflows\.standalone|TransactionExecutors|StandaloneFeeCalculatorImpl)'
readonly class_pattern='(^|/)(TransactionExecutors|StandaloneFeeCalculatorImpl)[^/]*\.class$|com/hedera/node/app/workflows/standalone/'
readonly service_pattern='workflows\.standalone|TransactionExecutors|StandaloneFeeCalculatorImpl'

if [[ -n "$rg_bin" ]]; then
  production_hits=$("$rg_bin" -n "$source_pattern" "$repo_root" \
    --glob '**/src/main/**' --glob '!**/build/**' || true)
  module_hits=$("$rg_bin" -n "$module_pattern" "$repo_root" \
    --glob '**/src/main/java/module-info.java' || true)
else
  production_hits=$(find "$repo_root" -path '*/src/main/*' -type f ! -path '*/build/*' -print0 |
    xargs -0 grep -En "$source_pattern" 2>/dev/null || true)
  module_hits=$(find "$repo_root" -path '*/src/main/java/module-info.java' -type f -print0 |
    xargs -0 grep -En "$module_pattern" 2>/dev/null || true)
fi
[[ -z "$production_hits" ]] || fail "prohibited production source remains:\n$production_hits"

[[ -z "$module_hits" ]] || fail "prohibited JPMS edge remains:\n$module_hits"

if [[ -d "$artifact_root" ]]; then
  while IFS= read -r -d '' jar_file; do
    jar_hits=$("$jar_bin" tf "$jar_file" | grep -E "$class_pattern" || true)
    [[ -z "$jar_hits" ]] || fail "prohibited class in $jar_file:\n$jar_hits"

    service_entries=$("$jar_bin" tf "$jar_file" | grep -E '^META-INF/services/' || true)
    if [[ -n "$service_entries" ]]; then
      extract_dir=$(mktemp -d)
      (
        cd "$extract_dir"
        while IFS= read -r entry; do
          "$jar_bin" xf "$jar_file" "$entry"
        done <<<"$service_entries"
      )
      service_hits=$(grep -REn "$service_pattern" "$extract_dir/META-INF/services" || true)
      find "$extract_dir" -depth -delete
      [[ -z "$service_hits" ]] || fail "prohibited service provider in $jar_file:\n$service_hits"
    fi
  done < <(find "$artifact_root" -type f -name '*.jar' -print0)
fi

if (( failures > 0 )); then
  exit 1
fi

printf 'P07 standalone-removal policy passed\n'
