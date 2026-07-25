#!/usr/bin/env sh
# SPDX-License-Identifier: Apache-2.0
set -eu

repo_root=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
policy="$repo_root/tools/p07/verify-residual-contract-implementation-removed.sh"
scratch=$(mktemp -d)
trap 'rm -rf "$scratch"' EXIT HUP INT TERM

make_clean_tree() {
  case_root=$1
  mkdir -p \
    "$case_root/hedera-node/hedera-smart-contract-service/src/main/java/com/hedera/node/app/service/contract/impl/schemas" \
    "$case_root/hedera-node/hedera-app" \
    "$case_root/hedera-node/test-clients/src/main/java" \
    "$case_root/hedera-state-validator/src/main/java"
  printf '%s\n' 'class V0490ContractSchema {}' > \
    "$case_root/hedera-node/hedera-smart-contract-service/src/main/java/com/hedera/node/app/service/contract/impl/schemas/V0490ContractSchema.java"
  printf '%s\n' 'class V065ContractSchema {}' > \
    "$case_root/hedera-node/hedera-smart-contract-service/src/main/java/com/hedera/node/app/service/contract/impl/schemas/V065ContractSchema.java"
  : >"$case_root/settings.gradle.kts"
  : >"$case_root/hedera-node/hedera-app/build.gradle.kts"
  : >"$case_root/hedera-node/test-clients/build.gradle.kts"
  : >"$case_root/hedera-node/test-clients/src/main/java/module-info.java"
  : >"$case_root/hedera-state-validator/src/main/java/module-info.java"
}

expect_failure() {
  name=$1
  case_root="$scratch/$name"
  make_clean_tree "$case_root"
  shift
  "$@" "$case_root"
  if P07_REPO_ROOT="$case_root" P07_ARTIFACT_ROOT="$case_root" "$policy" >/dev/null 2>&1; then
    printf '%s\n' "Policy unexpectedly accepted $name injection" >&2
    exit 1
  fi
}

inject_project() {
  mkdir -p "$1/hedera-node/hedera-smart-contract-service-impl"
}
inject_settings() {
  printf '%s\n' 'module("hedera-smart-contract-service-impl") { artifact = "app-service-contract-impl" }' \
    >>"$1/settings.gradle.kts"
}
inject_dependency() {
  printf '%s\n' 'implementation(project(":app-service-contract-impl"))' \
    >>"$1/hedera-node/test-clients/build.gradle.kts"
}
inject_module() {
  printf '%s\n' 'requires com.hedera.node.app.service.contract.impl;' \
    >>"$1/hedera-node/test-clients/src/main/java/module-info.java"
}
inject_besu() {
  printf '%s\n' 'import org.hyperledger.besu.datatypes.Address; class Injected {}' \
    >"$1/hedera-node/hedera-smart-contract-service/src/main/java/Injected.java"
}
inject_tuweni() {
  printf '%s\n' 'import org.apache.tuweni.bytes.Bytes; class Injected {}' \
    >"$1/hedera-node/hedera-smart-contract-service/src/main/java/Injected.java"
}
inject_opcode() {
  printf '%s\n' 'class OpcodeUtils {}' \
    >"$1/hedera-node/hedera-smart-contract-service/src/main/java/OpcodeUtils.java"
}
inject_artifact() {
  mkdir -p "$1/hedera-node/hedera-app/build/libs"
  : >"$1/hedera-node/hedera-app/build/libs/app-service-contract-impl-injected.jar"
}
inject_packaging_exclusion() {
  printf '%s\n' 'exclude("data/lib/app-service-contract-impl-*")' \
    >>"$1/hedera-node/hedera-app/build.gradle.kts"
}

clean="$scratch/clean"
make_clean_tree "$clean"
P07_REPO_ROOT="$clean" P07_ARTIFACT_ROOT="$clean" "$policy" >/dev/null

expect_failure project-recreation inject_project
expect_failure settings inject_settings
expect_failure dependency inject_dependency
expect_failure jpms inject_module
expect_failure neutral-besu inject_besu
expect_failure neutral-tuweni inject_tuweni
expect_failure opcode inject_opcode
expect_failure artifact inject_artifact
expect_failure packaging-exclusion inject_packaging_exclusion

printf '%s\n' 'P07 residual contract removal intentional-failure controls passed'
