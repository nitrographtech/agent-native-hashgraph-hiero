#!/usr/bin/env bash
# SPDX-License-Identifier: Apache-2.0
set -euo pipefail

repo_root="${P06B_SOURCE_ROOT:-$(git rev-parse --show-toplevel)}"
native_dist="$repo_root/hedera-node/hedera-app/build/distributions/distribution-native-agent"
full_dist="$repo_root/hedera-node/hedera-app/build/distributions/distribution-full"
verifier="$repo_root/tools/p06b/verify-native-runtime-dependencies.sh"
jar_tool="${JAVA_HOME:+$JAVA_HOME/bin/}jar"
javac_tool="${JAVA_HOME:+$JAVA_HOME/bin/}javac"

test -d "$native_dist" || {
  echo "Missing native distribution: $native_dist" >&2
  exit 1
}
test -d "$full_dist" || {
  echo "Missing full distribution: $full_dist" >&2
  exit 1
}

scratch="$(mktemp -d)"
trap 'rm -rf -- "$scratch"' EXIT

expect_rejection() {
  local name="$1"
  local candidate="$2"
  if P06B_NATIVE_DIST="$candidate" "$verifier" >"$scratch/$name.out" 2>&1; then
    echo "Binary policy accepted prohibited $name injection" >&2
    exit 1
  fi
  echo "P06B intentional $name injection: rejected"
}

fresh_copy() {
  local target="$1"
  mkdir -p "$target"
  cp -a "$native_dist/." "$target/"
}

jar_case="$scratch/jar"
fresh_copy "$jar_case"
cp "$full_dist"/data/lib/app-service-contract-impl-*.jar "$jar_case/data/lib/"
expect_rejection "jar" "$jar_case"

class_case="$scratch/class"
fresh_copy "$class_case"
class_extract="$scratch/class-extract"
mkdir -p "$class_extract"
(
  cd "$class_extract"
  "$jar_tool" xf "$full_dist/data/apps/HederaNode.jar" \
    com/hedera/node/app/services/FullContractRuntimeProvider.class
  "$jar_tool" uf "$class_case/data/apps/HederaNode.jar" \
    com/hedera/node/app/services/FullContractRuntimeProvider.class
)
expect_rejection "class" "$class_case"

service_case="$scratch/service"
fresh_copy "$service_case"
service_extract="$scratch/service-extract"
mkdir -p "$service_extract/META-INF/services"
printf '%s\n' 'com.hedera.node.app.services.FullContractRuntimeProviderFactory' \
  >"$service_extract/META-INF/services/com.hedera.node.app.services.InjectedProvider"
(
  cd "$service_extract"
  "$jar_tool" uf "$service_case/data/apps/HederaNode.jar" \
    META-INF/services/com.hedera.node.app.services.InjectedProvider
)
expect_rejection "service" "$service_case"

module_case="$scratch/module"
fresh_copy "$module_case"
module_source="$scratch/module-source"
module_classes="$scratch/module-classes"
mkdir -p "$module_source" "$module_classes"
printf '%s\n' \
  'module p06b.prohibited.module {' \
  '    requires org.hyperledger.besu.evm;' \
  '}' >"$module_source/module-info.java"
"$javac_tool" --module-path "$full_dist/data/lib" -d "$module_classes" \
  "$module_source/module-info.java"
"$jar_tool" --create --file "$module_case/data/lib/p06b-prohibited-module.jar" \
  -C "$module_classes" module-info.class
expect_rejection "module" "$module_case"

native_case="$scratch/native-library"
fresh_copy "$native_case"
: >"$native_case/data/lib/libevm-injected.so"
expect_rejection "native-library" "$native_case"

echo "P06B native binary policy intentional failures: PASS"
