#!/usr/bin/env bash
# SPDX-License-Identifier: Apache-2.0
set -euo pipefail

repo_root="${P06B_SOURCE_ROOT:-$(git rev-parse --show-toplevel)}"
app_main="$repo_root/hedera-node/hedera-app/src/main/java"

fail() {
  echo "P06B native dependency policy violation: $*" >&2
  exit 1
}

test -d "$app_main" || fail "missing hedera-app production source tree"

if rg -n 'ContractServiceImpl' "$app_main/com/hedera/node/app/Hedera.java"; then
  fail "Hedera startup directly references ContractServiceImpl"
fi

if rg -n 'service\.contract\.impl\.utils\.ConversionUtils' \
  "$app_main/com/hedera/node/app/authorization"; then
  fail "native authorization uses executable contract conversion utilities"
fi

shared_translation=(
  "$app_main/com/hedera/node/app/blocks/historical"
  "$app_main/com/hedera/node/app/blocks/BlockItemsTranslator.java"
  "$app_main/com/hedera/node/app/workflows/handle/record/RecordStreamBuilder.java"
)
if rg -n '^import (org\.hyperledger\.besu|org\.apache\.tuweni)' "${shared_translation[@]}"; then
  fail "shared historical translation imports Besu or Tuweni"
fi

dagger_sources=(
  "$app_main/com/hedera/node/app/HederaInjectionComponent.java"
  "$app_main/com/hedera/node/app/workflows/FacilityInitModule.java"
  "$app_main/com/hedera/node/app/workflows/handle/HandleWorkflowModule.java"
  "$app_main/com/hedera/node/app/workflows/query/QueryWorkflowInjectionModule.java"
)
if rg -n '(ContractServiceImpl|FullContractRuntimeProvider|service\.contract\.impl\.exec\.systemcontracts)' \
  "${dagger_sources[@]}"; then
  fail "native Dagger graph binds an executable contract implementation"
fi

unexpected_contract_impl_refs="$(
  rg -l 'ContractServiceImpl' "$app_main" |
    sed "s#^$repo_root/##" |
    rg -v '^hedera-node/hedera-app/src/main/java/com/hedera/node/app/services/FullContractRuntimeProvider\.java$|^hedera-node/hedera-app/src/main/java/com/hedera/node/app/workflows/standalone/TransactionExecutors\.java$' ||
    true
)"
test -z "$unexpected_contract_impl_refs" ||
  fail "unexpected ContractServiceImpl references: $unexpected_contract_impl_refs"

native_dist="$repo_root/hedera-node/hedera-app/build/distributions/distribution-native-agent"
if test -d "$native_dist"; then
  grep -Fxq 'contracts.enabled=false' "$native_dist/data/config/application.properties" ||
    fail "native distribution does not disable executable contracts"
fi

echo "P06B native dependency policy: PASS"
