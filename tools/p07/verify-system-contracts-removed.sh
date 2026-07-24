#!/usr/bin/env bash
# SPDX-License-Identifier: Apache-2.0
set -euo pipefail

repo_root=${P07_REPO_ROOT:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}
artifact_root=${P07_ARTIFACT_ROOT:-"$repo_root/hedera-node"}
jar_bin=${P07_JAR_BIN:-${JAVA_HOME:+$JAVA_HOME/bin/jar}}
jar_bin=${jar_bin:-$(command -v jar || true)}
rg_bin=${P07_RG_BIN:-$(command -v rg || true)}
failures=0

[[ -n "$jar_bin" && -x "$jar_bin" ]] || {
  printf 'P07 system-contract policy requires the JDK jar tool\n' >&2
  exit 2
}

fail() {
  printf 'P07 system-contract removal policy failure: %s\n' "$*" >&2
  failures=$((failures + 1))
}

source_pattern='exec\.systemcontracts|SystemContractMethodRegistry|SystemContractOpsDurationMetric|class (Hedera|Hts|Has|Hss|Prng|ExchangeRate)SystemContract|class (Hts|Has|Hss)CallFactory|class (Hts|Has|Hss)CallAttempt'
module_pattern='(exports|opens|requires|uses|provides).*(exec\.systemcontracts|SystemContractMethodRegistry|HtsSystemContract|HasSystemContract|HssSystemContract)'
class_pattern='com/hedera/node/app/service/contract/impl/exec/systemcontracts/|(^|/)(SystemContractMethodRegistry|SystemContractOpsDurationMetric|(Hedera|Hts|Has|Hss|Prng|ExchangeRate)SystemContract)\.class$'
service_pattern='exec\.systemcontracts|SystemContractMethodRegistry|HtsSystemContract|HasSystemContract|HssSystemContract'

if [[ -n "$rg_bin" ]]; then
  source_hits=$("$rg_bin" -n "$source_pattern" "$repo_root" --glob '**/src/main/**' --glob '!**/build/**' || true)
  module_hits=$("$rg_bin" -n "$module_pattern" "$repo_root" --glob '**/src/main/java/module-info.java' || true)
else
  source_hits=$(find "$repo_root" -path '*/src/main/*' -type f ! -path '*/build/*' -print0 |
    xargs -0 grep -En "$source_pattern" 2>/dev/null || true)
  module_hits=$(find "$repo_root" -path '*/src/main/java/module-info.java' -type f -print0 |
    xargs -0 grep -En "$module_pattern" 2>/dev/null || true)
fi
[[ -z "$source_hits" ]] || fail "prohibited production source remains:\n$source_hits"
[[ -z "$module_hits" ]] || fail "prohibited JPMS edge remains:\n$module_hits"

if [[ -d "$artifact_root" ]]; then
  while IFS= read -r -d '' jar_file; do
    class_hits=$("$jar_bin" tf "$jar_file" | grep -E "$class_pattern" || true)
    [[ -z "$class_hits" ]] || fail "prohibited class in $jar_file:\n$class_hits"

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

(( failures == 0 )) || exit 1
printf 'P07 system-contract removal policy passed\n'
