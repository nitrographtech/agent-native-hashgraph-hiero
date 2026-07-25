#!/usr/bin/env sh
# SPDX-License-Identifier: Apache-2.0
set -eu

repo_root=${P07_REPO_ROOT:-$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)}
artifact_root=${P07_ARTIFACT_ROOT:-$repo_root}
failures=0

fail() {
  printf '%s\n' "P07 residual contract implementation policy failure: $*" >&2
  failures=$((failures + 1))
}

impl_dir="$repo_root/hedera-node/hedera-smart-contract-service-impl"
neutral_dir="$repo_root/hedera-node/hedera-smart-contract-service"
neutral_source="$neutral_dir/src/main/java"
schema_dir="$neutral_source/com/hedera/node/app/service/contract/impl/schemas"

[ ! -e "$impl_dir" ] || fail "removed Gradle project directory exists: $impl_dir"
[ -f "$schema_dir/V0490ContractSchema.java" ] ||
  fail "V0.49 legacy-FQN schema forwarder is not owned by app-service-contract"
[ -f "$schema_dir/V065ContractSchema.java" ] ||
  fail "V0.65 legacy-FQN schema forwarder is not owned by app-service-contract"

if [ -d "$neutral_source" ]; then
  besu_hits=$(find "$neutral_source" -type f -name '*.java' -exec grep -En \
    '^[[:space:]]*import[[:space:]]+org\.hyperledger\.besu\.' {} + 2>/dev/null || true)
  tuweni_hits=$(find "$neutral_source" -type f -name '*.java' -exec grep -En \
    '^[[:space:]]*import[[:space:]]+org\.apache\.tuweni\.' {} + 2>/dev/null || true)
  opcode_hits=$(find "$neutral_source" -type f -name '*.java' -exec grep -En \
    '(class|interface|record)[[:space:]]+OpcodeUtils([^[:alnum:]_]|$)' {} + 2>/dev/null || true)
  [ -z "$besu_hits" ] || fail "Besu import entered app-service-contract:$besu_hits"
  [ -z "$tuweni_hits" ] || fail "Tuweni import entered app-service-contract:$tuweni_hits"
  [ -z "$opcode_hits" ] || fail "OpcodeUtils was reintroduced:$opcode_hits"
fi

active_files=
for candidate in \
  "$repo_root/settings.gradle.kts" \
  "$repo_root/hedera-node/hedera-app/build.gradle.kts" \
  "$repo_root/hedera-node/test-clients/build.gradle.kts" \
  "$repo_root/hedera-node/test-clients/src/main/java/module-info.java" \
  "$repo_root/hedera-state-validator/build.gradle.kts" \
  "$repo_root/hedera-state-validator/src/main/java/module-info.java"
do
  [ ! -f "$candidate" ] || active_files="$active_files $candidate"
done

if [ -n "$active_files" ]; then
  active_hits=$(grep -En \
    'hedera-smart-contract-service-impl|app-service-contract-impl|requires[[:space:]]+com\.hedera\.node\.app\.service\.contract\.impl' \
    $active_files 2>/dev/null || true)
  [ -z "$active_hits" ] || fail "active build or JPMS reference remains:$active_hits"
fi

old_utility_hits=$(find "$repo_root" -path '*/src/main/*' -type f -name '*.java' \
  -exec grep -En \
  'com\.hedera\.node\.app\.service\.contract\.impl\.utils\.(ConversionUtils|OpcodeUtils)' {} + \
  2>/dev/null || true)
[ -z "$old_utility_hits" ] || fail "removed utility ownership remains:$old_utility_hits"

artifact_hits=$(find "$artifact_root" -path '*/build/*' -type f \
  \( -name 'app-service-contract-impl-*.jar' -o -name 'app-service-contract-impl.jar' \) \
  -print 2>/dev/null || true)
[ -z "$artifact_hits" ] || fail "removed implementation artifact exists:$artifact_hits"

[ "$failures" -eq 0 ] || exit 1
printf '%s\n' 'P07 residual contract implementation removal policy passed'
