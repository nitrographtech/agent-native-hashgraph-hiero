#!/bin/sh
# SPDX-License-Identifier: Apache-2.0
#
# Enforces the final P07-11C through P07-11E suite boundary. Successful
# executable HAPI coverage is retired; exactly eight minimal rejection
# constructions remain in three named owners.
set -eu

repo_root=${1:-$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)}
src="$repo_root/hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites"

fail() {
    echo "P07 final executable HAPI policy: $1" >&2
    exit 1
}

[ -d "$src" ] || fail "test-client suite source root is missing"

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
contract/hapi/ContractUpdateSuite.java
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
contract/records/ContractRecordsSanityCheckSuite.java
contract/records/LogsSuite.java
contract/records/batch/AtomicLogsSuite.java
contract/validation/EvmValidationTest.java
contract/validation/batch/AtomicEvmValidationTest.java
'

for relative_path in $removed_suites
do
    [ ! -e "$src/$relative_path" ] ||
        fail "retired execution suite was reintroduced: $relative_path"
done

fixture_owner="$src/reconnect/AuthenticatedHistoricalFixtureConsumerTest.java"
historical_owner="$src/file/HistoricalContractExecutionRejection.java"
record_owner="$src/issues/Issue1765Suite.java"

for owner in "$fixture_owner" "$historical_owner" "$record_owner"
do
    [ -f "$owner" ] || fail "intentional rejection owner is missing: ${owner#"$src/"}"
done

count_fixed() {
    pattern=$1
    file=$2
    grep -F "$pattern" "$file" 2>/dev/null | wc -l | tr -d ' '
}

[ "$(count_fixed 'explicitContractCreate(' "$fixture_owner")" -eq 1 ] ||
    fail "authenticated fixture owner must contain one rejected contract create"
[ "$(count_fixed 'new HapiContractCall(' "$fixture_owner")" -eq 1 ] ||
    fail "authenticated fixture owner must contain one rejected contract call"
[ "$(count_fixed '.hasPrecheck(INVALID_TRANSACTION_BODY)' "$fixture_owner")" -eq 2 ] ||
    fail "authenticated fixture probes must remain INVALID_TRANSACTION_BODY rejections"

[ "$(count_fixed 'explicitContractCreate(' "$historical_owner")" -eq 1 ] ||
    fail "historical rejection owner must contain one rejected contract create"
[ "$(count_fixed 'contractCall(' "$historical_owner")" -eq 1 ] ||
    fail "historical rejection owner must contain one rejected contract call"
[ "$(count_fixed 'contractUpdate(' "$historical_owner")" -eq 1 ] ||
    fail "historical rejection owner must contain one rejected contract update"
[ "$(count_fixed 'contractDelete(' "$historical_owner")" -eq 1 ] ||
    fail "historical rejection owner must contain one rejected contract delete"
[ "$(count_fixed 'explicitEthereumTransaction(' "$historical_owner")" -eq 1 ] ||
    fail "historical rejection owner must contain one rejected Ethereum transaction"
[ "$(count_fixed '.hasPrecheck(INVALID_TRANSACTION_BODY)' "$historical_owner")" -eq 5 ] ||
    fail "historical execution probes must remain INVALID_TRANSACTION_BODY rejections"

[ "$(count_fixed 'contractUpdate(INVALID_CONTRACT)' "$record_owner")" -eq 1 ] ||
    fail "record rejection owner must contain one invalid contract update"
[ "$(count_fixed '.hasKnownStatus(ResponseCodeEnum.INVALID_CONTRACT_ID)' "$record_owner")" -eq 1 ] ||
    fail "record rejection probe must remain an INVALID_CONTRACT_ID rejection"

scan_list=$(mktemp "${TMPDIR:-/tmp}/p07-executable-sites.XXXXXX")
trap 'rm -f "$scan_list"' EXIT HUP INT TERM
find "$src" -type f -name '*.java' \
    ! -path "$fixture_owner" \
    ! -path "$historical_owner" \
    ! -path "$record_owner" \
    -print >"$scan_list"

if xargs grep -nE \
    '(^|[^[:alnum:]_])(contractCreate|createDefaultContract|contractCustomCreate|explicitContractCreate|contractCall|contractCallWith[A-Za-z0-9_]*|ethereumCall|explicitEthereumTransaction|contractCallLocal)[[:space:]]*\(|new[[:space:]]+HapiContractCall[[:space:]]*\(|@Contract[[:space:]]*\(' \
    <"$scan_list" 2>/dev/null
then
    fail "a successful or unclassified executable suite site was introduced"
fi

echo "P07 final executable HAPI boundary verified: retired suites absent; eight rejection probes intentional"
