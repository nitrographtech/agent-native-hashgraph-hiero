#!/bin/sh
# SPDX-License-Identifier: Apache-2.0
set -eu

repo_root=${1:-$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)}
src="$repo_root/hedera-node/test-clients/src/main/java"

fail() {
    echo "P07-10B mixed-suite policy: $1" >&2
    exit 1
}

for removed in \
    com/hedera/services/bdd/suites/contract/opcodes/Create2OperationSuite.java \
    com/hedera/services/bdd/suites/utils/contracts/precompile/HTSPrecompileResult.java \
    com/hedera/services/bdd/suites/hip551/contracts/AtomicBatchContractKeysHtsTest.java \
    com/hedera/services/bdd/suites/hip551/contracts/precompile/AtomicBatchAddress167Test.java
do
    [ ! -e "$src/$removed" ] || fail "retired executable owner was reintroduced: $removed"
done

mixed_files="
$src/com/hedera/services/bdd/suites/crypto/CryptoApproveAllowanceSuite.java
$src/com/hedera/services/bdd/suites/hip551/allowance/AtomicBatchApproveAllowanceTest.java
$src/com/hedera/services/bdd/suites/hip904/AirdropsDisabledTest.java
$src/com/hedera/services/bdd/suites/hip904/TokenAirdropTest.java
$src/com/hedera/services/bdd/suites/integration/RepeatableHip423Tests.java
"
for file in $mixed_files
do
    [ -f "$file" ] || continue
    if grep -E 'contractCreate\(|contractCall\(|ethereumCall\(|uploadInitCode\(' "$file" >/dev/null 2>&1; then
        fail "retained native suite contains executable contract setup: $file"
    fi
done

if find "$src" -type f -name '*.java' -exec grep -H -E \
    'Create2OperationSuite|HTSPrecompileResult' {} + \
    >"$repo_root/.p07-10b-retired-imports.tmp" 2>/dev/null &&
    [ -s "$repo_root/.p07-10b-retired-imports.tmp" ]; then
    rm -f "$repo_root/.p07-10b-retired-imports.tmp"
    fail "source references retired CREATE2 or HTS precompile helper"
fi
rm -f "$repo_root/.p07-10b-retired-imports.tmp"

echo "P07-10B mixed-suite execution ownership verified"
