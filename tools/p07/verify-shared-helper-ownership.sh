#!/bin/sh
# SPDX-License-Identifier: Apache-2.0
set -eu

repo_root=${1:-$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)}
test_src="$repo_root/hedera-node/test-clients/src/main/java"
production_src="$repo_root/hedera-node"

fail() {
    echo "P07-10A shared-helper policy: $1" >&2
    exit 1
}

find "$test_src" -type f -name '*.java' -exec grep -H -E \
    '^import( static)? com\.hedera\.services\.bdd\.suites\.contract\.(records\.RecordsSuite|traceability\.TraceabilitySuite|evm\.Evm46ValidationSuite|hapi\.ContractCreateSuite)' \
    {} + >"$repo_root/.p07-10a-suite-imports.tmp" 2>/dev/null || true
if [ -s "$repo_root/.p07-10a-suite-imports.tmp" ]; then
    rm -f "$repo_root/.p07-10a-suite-imports.tmp"
    fail "shared consumer imports a retired execution suite"
fi
rm -f "$repo_root/.p07-10a-suite-imports.tmp"

for removed in \
    suites/contract/records/RecordsSuite.java \
    suites/contract/traceability/TraceabilitySuite.java \
    suites/contract/evm/Evm46ValidationSuite.java \
    suites/contract/hapi/ContractCreateSuite.java
do
    [ ! -e "$test_src/com/hedera/services/bdd/$removed" ] ||
        fail "retired suite owner was reintroduced: $removed"
done

find "$production_src" -path '*/src/main/java/*.java' -type f \
    ! -path '*/build/*' \
    ! -path "$test_src/*" -exec grep -H -E \
    'com\.hedera\.services\.bdd\.(junit\.support\.fixtures|spec\.assertions\.HistoricalStorageEncoding|spec\.utilops\.HistoricalSidecarFixtures|suites\.utils\.(LegacyTransactionVectors|NativeAccountTestVectors|NativeEcdsaSigning))' \
    {} + >"$repo_root/.p07-10a-production-imports.tmp" 2>/dev/null || true
if [ -s "$repo_root/.p07-10a-production-imports.tmp" ]; then
    rm -f "$repo_root/.p07-10a-production-imports.tmp"
    fail "production source imports a test-client helper owner"
fi
rm -f "$repo_root/.p07-10a-production-imports.tmp"

echo "P07-10A shared-helper ownership verified"
