#!/usr/bin/env bash
# SPDX-License-Identifier: Apache-2.0
set -euo pipefail

repo_root="${P06B_SOURCE_ROOT:-$(git rev-parse --show-toplevel)}"
java_home="${JAVA_HOME:?JAVA_HOME must identify the validated JDK}"
native_jar="$repo_root/hedera-node/hedera-app/build/distributions/distribution-native-agent/data/apps/HederaNode.jar"
full_jar="$repo_root/hedera-node/hedera-app/build/distributions/distribution-full/data/apps/HederaNode.jar"
verifier="$repo_root/tools/p06b/verify-native-runtime-dependencies.sh"

test -f "$native_jar"
test -f "$full_jar"
test -x "$java_home/bin/jar"

workspace="$(mktemp -d)"
trap 'rm -rf -- "$workspace"' EXIT
(
  mkdir "$workspace/classes"
  cd "$workspace/classes"
  "$java_home/bin/jar" xf "$native_jar"
  rm -f module-info.class
  "$java_home/bin/jar" xf "$full_jar" \
    com/hedera/node/app/services/FullContractRuntimeProvider.class
  "$java_home/bin/jar" cf "$workspace/HederaNode.jar" .
)

if P06B_NATIVE_APP_JAR="$workspace/HederaNode.jar" "$verifier" >"$workspace/output" 2>&1; then
  echo "Expected the native application policy to reject an injected full-runtime class" >&2
  exit 1
fi
grep -Fq "native application jar contains full-runtime" "$workspace/output"
echo "P06B native application intentional-failure proof: PASS"
