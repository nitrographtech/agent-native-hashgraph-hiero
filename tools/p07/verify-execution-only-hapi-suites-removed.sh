#!/bin/sh
# SPDX-License-Identifier: Apache-2.0
set -eu

repo_root=${1:-$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)}
src="$repo_root/hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites"

fail() {
    echo "P07-11A execution-only HAPI suite policy: $1" >&2
    exit 1
}

if [ -d "$src/contract/classiccalls" ] &&
    find "$src/contract/classiccalls" -type f -name '*.java' -print -quit |
        grep . >/dev/null 2>&1
then
    fail "the retired classic-call execution package was reintroduced"
fi

removed_suites='
contract/ethereum/HelloWorldEthereumSuite.java
contract/ethereum/JumboTransactionsEnabledTest.java
contract/ethereum/NonceSuite.java
contract/ethereum/batch/AtomicHelloWorldEthereumSuite.java
contract/evm/Evm50ValidationSuite.java
contract/evm/batch/AtomicEvm50ValidationSuite.java
contract/fees/AtomicSmartContractServiceFeesTest.java
contract/fees/ContractServiceQueriesSimpleFeesTest.java
contract/fees/SimpleSmartContractServiceFeesTest.java
contract/fees/SmartContractServiceFeesTest.java
contract/hapi/ContractCallHapiOnlySuite.java
contract/hapi/ContractStateSuite.java
contract/hapi/batch/AtomicContractUpdateSuite.java
contract/hip906/HbarAllowanceApprovalTest.java
contract/hips/batch/AtomicAliasTest.java
contract/hips/batch/AtomicIsAuthorizedTest.java
contract/hips/hip632/AliasTest.java
contract/hips/hip632/IsAuthorizedTest.java
contract/leaky/LeakyEthereumTestsSuite.java
contract/leaky/batch/AtomicLeakyEthereumTestsSuite.java
contract/opcodes/CreateOperationSuite.java
contract/opcodes/DelegateCallOperationSuite.java
contract/opcodes/GasPriceSuite.java
contract/opcodes/GlobalPropertiesSuite.java
contract/opcodes/PrngSeedOperationSuite.java
contract/opcodes/PushZeroOperationSuite.java
contract/openzeppelin/ERC1155ContractInteractions.java
contract/openzeppelin/ERC721ContractInteractions.java
contract/opsduration/OpsDurationThrottleTest.java
contract/records/LogsSuite.java
contract/records/batch/AtomicLogsSuite.java
contract/validation/EvmValidationTest.java
contract/validation/batch/AtomicEvmValidationTest.java
'

for relative_path in $removed_suites
do
    [ ! -e "$src/$relative_path" ] ||
        fail "retired execution-only suite was reintroduced: $relative_path"
done

# The two shared-owner suites and all mixed/rejection boundaries are deliberately
# outside P07-11A. This verifier therefore targets only the measured closure.
for allowed in \
    contract/hapi/ContractUpdateSuite.java \
    contract/records/ContractRecordsSanityCheckSuite.java
do
    [ -f "$src/$allowed" ] ||
        fail "a P07-11B shared-owner boundary disappeared: $allowed"
done

echo "P07-11A execution-only HAPI suite removal verified"
