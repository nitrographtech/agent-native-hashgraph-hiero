#!/usr/bin/env bash
# SPDX-License-Identifier: Apache-2.0
set -euo pipefail

repo_root=${P07_REPO_ROOT:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}
artifact_root=${P07_ARTIFACT_ROOT:-"$repo_root/hedera-node/hedera-app/build/distributions"}
jar_bin=${P07_JAR_BIN:-${JAVA_HOME:+$JAVA_HOME/bin/jar}}
jar_bin=${jar_bin:-$(command -v jar || true)}
rg_bin=${P07_RG_BIN:-$(command -v rg || true)}
failures=0

[[ -n "$jar_bin" && -x "$jar_bin" ]] || {
  printf 'P07 Ethereum-execution policy requires the JDK jar tool\n' >&2
  exit 2
}

fail() {
  printf 'P07 Ethereum-execution removal policy failure: %s\n' "$*" >&2
  failures=$((failures + 1))
}

# PBJ/protobuf models, neutral stream builders, historical translators, and the
# test-client corpus are intentionally outside this live runtime policy.
source_pattern='class EthereumTransactionHandler|class EthereumFeeCalculator|class EthereumCallDataHydration|class EthTxSigsCache|record HydratedEthTxData|EthereumTransactionHandlerFacade|populateEthTxData|recoverSignatures'
module_pattern='exports com\.hedera\.node\.app\.hapi\.utils\.ethereum|requires.*ethereum.execution|uses.*EthereumTransactionHandler|provides.*EthereumTransactionHandler'
class_pattern='(^|/)(EthereumTransactionHandler|EthereumFeeCalculator|EthereumCallDataHydration|EthTxSigsCache|HydratedEthTxData|EthereumTransactionHandlerFacade)\.class$|com/hedera/node/app/hapi/utils/ethereum/(EthTxData|EthTxSigs)\.class$'
service_pattern='EthereumTransactionHandler|EthereumCallDataHydration|EthTxSigsCache|HydratedEthTxData'

runtime_roots=(
  "$repo_root/hedera-node/hedera-app/src/main"
  "$repo_root/hedera-node/hedera-smart-contract-service-impl/src/main"
  "$repo_root/hedera-node/hapi-utils/src/main"
)

if [[ -n "$rg_bin" ]]; then
  source_hits=$("$rg_bin" -n "$source_pattern" "${runtime_roots[@]}" --glob '!**/build/**' || true)
  module_hits=$("$rg_bin" -n "$module_pattern" "$repo_root" --glob '**/src/main/java/module-info.java' || true)
else
  source_hits=$(find "${runtime_roots[@]}" -type f ! -path '*/build/*' -print0 |
    xargs -0 grep -En "$source_pattern" 2>/dev/null || true)
  module_hits=$(find "$repo_root" -path '*/src/main/java/module-info.java' -type f -print0 |
    xargs -0 grep -En "$module_pattern" 2>/dev/null || true)
fi
[[ -z "$source_hits" ]] || fail "prohibited live runtime source remains:\n$source_hits"
[[ -z "$module_hits" ]] || fail "prohibited JPMS edge remains:\n$module_hits"

if [[ -d "$artifact_root" ]]; then
  while IFS= read -r -d '' jar_file; do
    # Test-client/tooling jars retain historical corpus construction helpers.
    case "$jar_file" in
      *test-clients*|*fixture-tooling*) continue ;;
      *HederaNode.jar|*app-service-contract-impl-*.jar|*app-hapi-utils-*.jar) ;;
      *) continue ;;
    esac
    class_hits=$("$jar_bin" tf "$jar_file" | grep -E "$class_pattern" || true)
    [[ -z "$class_hits" ]] || fail "prohibited runtime class in $jar_file:\n$class_hits"

    service_entries=$("$jar_bin" tf "$jar_file" | grep -E '^META-INF/services/' || true)
    if [[ -n "$service_entries" ]]; then
      extract_dir=$(mktemp -d)
      (
        cd "$extract_dir"
        while IFS= read -r entry; do "$jar_bin" xf "$jar_file" "$entry"; done <<<"$service_entries"
      )
      service_hits=$(grep -REn "$service_pattern" "$extract_dir/META-INF/services" || true)
      find "$extract_dir" -depth -delete
      [[ -z "$service_hits" ]] || fail "prohibited service provider in $jar_file:\n$service_hits"
    fi
  done < <(find "$artifact_root" -type f -name '*.jar' -print0)
fi

(( failures == 0 )) || exit 1
printf 'P07 Ethereum-execution removal policy passed\n'
