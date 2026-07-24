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

native_dist="${P06B_NATIVE_DIST:-$repo_root/hedera-node/hedera-app/build/distributions/distribution-native-agent}"
if test -d "$native_dist"; then
  grep -Fxq 'contracts.enabled=false' "$native_dist/data/config/application.properties" ||
    fail "native distribution does not disable executable contracts"
  prohibited_jars="$(
    find "$native_dist/data/lib" -maxdepth 1 -type f \
      \( -name 'app-service-contract-impl-*' -o -name 'besu-*' -o \
         -name 'evm-*' -o -name 'tuweni-*' -o -name 'algorithms-*' -o \
         -name 'arithmetic-*' -o -name 'blake2bf-*' -o -name 'gnark-*' -o \
         -name 'jc-kzg-*' -o -name 'rlp-*' -o -name 'secp256k1-*' -o \
         -name 'secp256r1-*' \) -printf '%f\n' |
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
  rm -rf -- "$inspect_dir"
fi

if test -d "$native_dist"; then
  jar_tool="${JAVA_HOME:+$JAVA_HOME/bin/}jar"
  command -v "$jar_tool" >/dev/null || fail "jar tool is unavailable"
  inspect_root="$(mktemp -d)"
  trap 'rm -rf -- "$inspect_root"' EXIT
  prohibited_entries='^org/hyperledger/besu/|^org/apache/tuweni/|(^|/)(ContractServiceImpl|FullContractRuntimeProvider|FullContractRuntimeProviderFactory|FullContractStoreFactory|TransactionExecutors)[^/]*\.class$|^com/hedera/node/app/service/contract/impl/exec/|^com/hedera/node/app/service/contract/impl/state/'
  while IFS= read -r archive; do
    entries="$("$jar_tool" tf "$archive")"
    if printf '%s\n' "$entries" | rg -n "$prohibited_entries"; then
      fail "native archive $(basename "$archive") contains a prohibited class or package"
    fi
    archive_dir="$inspect_root/$(basename "$archive").d"
    mkdir -p "$archive_dir"
    (
      cd "$archive_dir"
      while IFS= read -r service_entry; do
        "$jar_tool" xf "$archive" "$service_entry"
      done < <(printf '%s\n' "$entries" | rg '^META-INF/services/' || true)
    )
    if test -d "$archive_dir/META-INF/services" &&
      rg -n '(FullContractRuntimeProvider|FullContractStoreFactory|ContractServiceImpl|TransactionExecutors|service\.contract\.impl\.exec)' \
        "$archive_dir/META-INF/services"; then
      fail "native archive $(basename "$archive") advertises an executable service"
    fi
    if printf '%s\n' "$entries" | grep -Fxq 'module-info.class'; then
      module_description="$("$jar_tool" --describe-module --file "$archive" 2>/dev/null || true)"
      if printf '%s\n' "$module_description" |
        rg -n '^requires (org\.hyperledger\.besu|tuweni\.|com\.hedera\.node\.app\.service\.contract\.impl)'; then
        fail "native archive $(basename "$archive") has a prohibited module requirement"
      fi
    fi
    if printf '%s\n' "$entries" |
      rg -n '(^|/)(lib)?(besu|evm|secp256k1|secp256r1|ckzg)[^/]*\.(so|dll|dylib)$'; then
      fail "native archive $(basename "$archive") contains an executable-only native library"
    fi
  done < <(
    find "$native_dist/data/apps" "$native_dist/data/lib" -maxdepth 1 -type f -name '*.jar' |
      sort
  )
  if find "$native_dist" -type f \
    \( -name '*besu*.so' -o -name '*evm*.so' -o -name '*secp256k1*.so' -o \
       -name '*secp256r1*.so' -o -name '*ckzg*.so' -o -name '*besu*.dll' -o \
       -name '*evm*.dll' -o -name '*secp256k1*.dll' -o -name '*secp256r1*.dll' -o \
       -name '*ckzg*.dll' -o -name '*besu*.dylib' -o -name '*evm*.dylib' -o \
       -name '*secp256k1*.dylib' -o -name '*secp256r1*.dylib' -o -name '*ckzg*.dylib' \) |
    grep -q .; then
    fail "native distribution contains an executable-only extracted native library"
  fi
fi

echo "P06B native dependency policy: PASS"
