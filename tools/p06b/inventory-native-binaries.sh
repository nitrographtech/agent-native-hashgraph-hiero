#!/usr/bin/env bash
# SPDX-License-Identifier: Apache-2.0
set -euo pipefail

readonly DIST=${1:?usage: inventory-native-binaries.sh NATIVE_DISTRIBUTION}
readonly JAVA_HOME=${JAVA_HOME:?JAVA_HOME must identify the validated JDK}
readonly LIB="$DIST/data/lib"
readonly APPS="$DIST/data/apps"

test -d "$LIB"
test -d "$APPS"
test -x "$JAVA_HOME/bin/jar"

classify() {
  case "$1" in
    app-service-contract-[0-9]*) echo HISTORICAL_COMPATIBILITY_REQUIRED ;;
    app-service-contract-impl-*) echo FULL_RUNTIME_ONLY ;;
    besu-*|evm-*|tuweni-*) echo EXECUTABLE_EVM_ONLY ;;
    headlong-*) echo RETAIN_TEMPORARILY_WITH_JUSTIFICATION ;;
    secp256k1-*|secp256r1-*) echo UNKNOWN_REQUIRES_REVIEW ;;
    *) echo NATIVE_REQUIRED ;;
  esac
}

coordinate() {
  case "$1" in
    app-service-contract-[0-9]*) echo project::app-service-contract ;;
    app-service-contract-impl-*) echo project::app-service-contract-impl ;;
    besu-*|evm-*) echo org.hyperledger.besu:inferred-from-artifact ;;
    tuweni-*) echo org.apache.tuweni:inferred-from-artifact ;;
    headlong-*) echo com.esaulpaugh:headlong ;;
    secp256k1-*|secp256r1-*) echo org.hyperledger.besu:inferred-native-crypto ;;
    *) echo unresolved ;;
  esac
}

printf '{\n'
printf '  "schema": "nitrograph-p06b-native-binary-inventory-before-v1",\n'
printf '  "distribution": "%s",\n' "$DIST"
printf '  "artifacts": [\n'
first=true
while IFS= read -r jar_path; do
  name=$(basename "$jar_path")
  size=$(stat -c %s "$jar_path")
  sha=$(sha256sum "$jar_path" | cut -d' ' -f1)
  module_info=false
  services=false
  native_content=false
  "$JAVA_HOME/bin/jar" tf "$jar_path" | rg -q '(^|/)module-info\.class$' && module_info=true || true
  "$JAVA_HOME/bin/jar" tf "$jar_path" | rg -q '^META-INF/services/.+' && services=true || true
  "$JAVA_HOME/bin/jar" tf "$jar_path" | rg -q '\.(so|dll|dylib)$' && native_content=true || true
  if $first; then first=false; else printf ',\n'; fi
  printf '    {"name":"%s","bytes":%s,"sha256":"%s","coordinate":"%s","origin":"runtimeClasspath","dependency_status":"resolved runtime artifact","classification":"%s","module_info":%s,"service_loader_entries":%s,"native_content":%s}' \
    "$name" "$size" "$sha" "$(coordinate "$name")" "$(classify "$name")" \
    "$module_info" "$services" "$native_content"
done < <(find "$LIB" "$APPS" -maxdepth 1 -type f -name '*.jar' | sort)
printf '\n  ]\n'
printf '}\n'
