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
  prohibited_jars="$(
    find "$native_dist/data/lib" -maxdepth 1 -type f \
      \( -name 'app-service-contract-impl-*' -o -name 'besu-*' -o \
         -name 'evm-*' -o -name 'tuweni-*' \) -printf '%f\n' |
      sort
  )"
  test -z "$prohibited_jars" ||
    fail "native distribution contains executable contract jars: $prohibited_jars"
fi

native_app_jar="${P06B_NATIVE_APP_JAR:-$native_dist/data/apps/HederaNode.jar}"
if test -f "$native_app_jar"; then
  jar_tool="${JAVA_HOME:+$JAVA_HOME/bin/}jar"
  command -v "$jar_tool" >/dev/null || fail "jar tool is unavailable"
  jar_entries="$("$jar_tool" tf "$native_app_jar")"
  prohibited_entries='(^|/)(FullContractRuntimeProvider[^/]*|FullContractStoreFactory|ContractServiceImpl[^/]*|TransactionExecutors[^/]*)\.class$|^com/hedera/node/app/workflows/standalone/|^com/hedera/node/app/service/contract/impl/|^org/hyperledger/besu/|^org/apache/tuweni/'
  if printf '%s\n' "$jar_entries" | rg -n "$prohibited_entries"; then
    fail "native application jar contains full-runtime, standalone, or executable contract classes"
  fi
  for required_entry in \
    com/hedera/node/app/ServicesMain.class \
    com/hedera/node/app/services/HistoricalContractRuntimeProvider.class \
    com/hedera/node/app/services/HistoricalContractRuntimeProviderFactory.class \
    com/hedera/node/app/store/HistoricalContractStoreFactory.class; do
    printf '%s\n' "$jar_entries" | grep -Fx "$required_entry" >/dev/null ||
      fail "native application jar is missing $required_entry"
  done
  if printf '%s\n' "$jar_entries" | grep -Fx 'module-info.class' >/dev/null; then
    fail "profile-filtered native jar must not retain the combined full-runtime module descriptor"
  fi

  inspect_dir="$(mktemp -d)"
  trap 'rm -rf -- "$inspect_dir"' EXIT
  (
    cd "$inspect_dir"
    "$jar_tool" xf "$native_app_jar" \
      META-INF/services/com.hedera.node.app.services.ContractRuntimeProviderFactory \
      META-INF/services/com.hedera.node.app.store.ContractStoreFactory
    grep -Fxq 'com.hedera.node.app.services.HistoricalContractRuntimeProviderFactory' \
      META-INF/services/com.hedera.node.app.services.ContractRuntimeProviderFactory ||
      fail "native application jar does not advertise the historical runtime provider factory"
    if rg -n '(FullContractRuntimeProviderFactory|FullContractStoreFactory)' META-INF/services; then
      fail "native application service metadata advertises a full-runtime factory"
    fi
    grep -Fxq 'com.hedera.node.app.store.HistoricalContractStoreFactory' \
      META-INF/services/com.hedera.node.app.store.ContractStoreFactory ||
      fail "native application jar does not advertise the historical store factory"
  )
fi

echo "P06B native dependency policy: PASS"
