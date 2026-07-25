#!/usr/bin/env bash
# SPDX-License-Identifier: Apache-2.0
set -euo pipefail

repo_root=${P07_REPO_ROOT:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}
artifact_root=${P07_ARTIFACT_ROOT:-"$repo_root/hedera-node/hedera-app/build/distributions"}
jar_bin=${P07_JAR_BIN:-${JAVA_HOME:+$JAVA_HOME/bin/jar}}
jar_bin=${jar_bin:-$(command -v jar || true)}
rg_bin=${P07_RG_BIN:-$(command -v rg || true)}
failures=0

fail() {
  printf 'P07 executable-engine removal policy failure: %s\n' "$*" >&2
  failures=$((failures + 1))
}

[[ -n "$jar_bin" && -x "$jar_bin" ]] || {
  printf 'P07 executable-engine policy requires the JDK jar tool\n' >&2
  exit 2
}

source_pattern='(class|interface|record)[[:space:]]+(ContractServiceImpl|FullContractRuntimeProvider|FullContractRuntimeProviderFactory|FullContractStoreFactory|HederaEVM|FrameRunner|RootProxyWorldUpdater|ProxyWorldUpdater|HederaWorldUpdater|DispatchingEvmFrameState|EvmFrameState|WritableContractStateStore|ContextTransactionProcessor|ContextQueryProcessor|ActionSidecarContentTracer|ActionSidecarContentTracerFactory|NoTracer)\b'
composition_pattern='(requires|uses|provides).*(contract\.impl\.exec|FullContractRuntimeProvider|FullContractStoreFactory|ActionSidecarContentTracer)'
class_pattern='(^|/)(ContractServiceImpl|FullContractRuntimeProvider|FullContractRuntimeProviderFactory|FullContractStoreFactory|HederaEVM|FrameRunner|RootProxyWorldUpdater|ProxyWorldUpdater|HederaWorldUpdater|DispatchingEvmFrameState|EvmFrameState|WritableContractStateStore|ContextTransactionProcessor|ContextQueryProcessor|ActionSidecarContentTracer|ActionSidecarContentTracerFactory|NoTracer)\.class$'

runtime_roots=(
  "$repo_root/hedera-node/hedera-app/src/main"
  "$repo_root/hedera-node/hedera-smart-contract-service-impl/src/main"
)

if [[ -n "$rg_bin" ]]; then
  source_hits=$("$rg_bin" -n "$source_pattern" "${runtime_roots[@]}" --glob '!**/build/**' || true)
  composition_hits=$("$rg_bin" -n "$composition_pattern" "${runtime_roots[@]}" \
    "$repo_root/hedera-node/hedera-app/build.gradle.kts" --glob '!**/build/**' || true)
  configuration_hits=$("$rg_bin" -n '^contracts\.enabled=true$' \
    "$repo_root/hedera-node/configuration" --glob '*.properties' || true)
else
  source_hits=$(find "${runtime_roots[@]}" -type f ! -path '*/build/*' -print0 |
    xargs -0 grep -En "$source_pattern" 2>/dev/null || true)
  composition_hits=$(find "${runtime_roots[@]}" -type f ! -path '*/build/*' -print0 |
    xargs -0 grep -En "$composition_pattern" 2>/dev/null || true)
  configuration_hits=$(grep -REn '^contracts\.enabled=true$' \
    "$repo_root/hedera-node/configuration" 2>/dev/null || true)
fi
[[ -z "$source_hits" ]] || fail "prohibited executable source remains:\n$source_hits"
[[ -z "$composition_hits" ]] || fail "prohibited runtime composition remains:\n$composition_hits"
[[ -z "$configuration_hits" ]] || fail "executable runtime configuration remains:\n$configuration_hits"

if [[ -d "$artifact_root" ]]; then
  while IFS= read -r -d '' jar_file; do
    class_hits=$("$jar_bin" tf "$jar_file" | grep -E "$class_pattern" || true)
    [[ -z "$class_hits" ]] || fail "prohibited executable class in $jar_file:\n$class_hits"
    service_entries=$("$jar_bin" tf "$jar_file" | grep -E '^META-INF/services/' || true)
    if [[ -n "$service_entries" ]]; then
      extract_dir=$(mktemp -d)
      (
        cd "$extract_dir"
        while IFS= read -r entry; do "$jar_bin" xf "$jar_file" "$entry"; done <<<"$service_entries"
      )
      service_hits=$(grep -REn 'FullContract|ContractServiceImpl|ActionSidecarContentTracer' \
        "$extract_dir/META-INF/services" || true)
      find "$extract_dir" -depth -delete
      [[ -z "$service_hits" ]] || fail "prohibited service provider in $jar_file:\n$service_hits"
    fi
  done < <(find "$artifact_root" -type f -name '*.jar' -print0)
fi

(( failures == 0 )) || exit 1
printf 'P07 executable contract engine removal policy passed\n'
