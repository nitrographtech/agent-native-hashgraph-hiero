#!/bin/sh
# SPDX-License-Identifier: Apache-2.0
set -eu

repo_root=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
verifier="$repo_root/tools/p07/verify-execution-only-hapi-suites-removed.sh"
tmp_root=$(mktemp -d "${TMPDIR:-/tmp}/p07-final-execution-policy.XXXXXX")
trap 'rm -rf "$tmp_root"' EXIT HUP INT TERM
src="$tmp_root/hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites"
real_src="$repo_root/hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites"

expect_failure() {
    name=$1
    if "$verifier" "$tmp_root" >/dev/null 2>&1; then
        echo "Expected P07 policy failure for $name" >&2
        exit 1
    fi
    echo "Expected failure confirmed: $name"
}

reset_fixture() {
    rm -rf "$src"
    mkdir -p "$src/reconnect" "$src/file" "$src/issues"
    cp "$real_src/reconnect/AuthenticatedHistoricalFixtureConsumerTest.java" "$src/reconnect/"
    cp "$real_src/file/HistoricalContractExecutionRejection.java" "$src/file/"
    cp "$real_src/issues/Issue1765Suite.java" "$src/issues/"
}

"$verifier" "$repo_root"
reset_fixture
"$verifier" "$tmp_root" >/dev/null
echo "Allowed-boundary success confirmed: eight intentional rejection probes"

mkdir -p "$src/contract/hapi"
: >"$src/contract/hapi/ContractUpdateSuite.java"
expect_failure "restored ContractUpdateSuite"

reset_fixture
mkdir -p "$src/contract/records"
: >"$src/contract/records/ContractRecordsSanityCheckSuite.java"
expect_failure "restored ContractRecordsSanityCheckSuite"

reset_fixture
mkdir -p "$src/contract/classiccalls"
: >"$src/contract/classiccalls/FailureCharacterizationSuite.java"
expect_failure "restored classic-call package"

for mutation in contract-create contract-call ethereum-execution local-query
do
    reset_fixture
    mkdir -p "$src/injected"
    case "$mutation" in
        contract-create) body='class Injected { void test() { contractCreate("x"); } }' ;;
        contract-call) body='class Injected { void test() { contractCall("x"); } }' ;;
        ethereum-execution) body='class Injected { void test() { explicitEthereumTransaction(null, null); } }' ;;
        local-query) body='class Injected { void test() { contractCallLocal("x"); } }' ;;
    esac
    printf '%s\n' "$body" >"$src/injected/Injected.java"
    expect_failure "prohibited $mutation site"
done

reset_fixture
sed 's/INVALID_TRANSACTION_BODY/SUCCESS/g' \
    "$src/reconnect/AuthenticatedHistoricalFixtureConsumerTest.java" \
    >"$src/reconnect/AuthenticatedHistoricalFixtureConsumerTest.java.changed"
mv "$src/reconnect/AuthenticatedHistoricalFixtureConsumerTest.java.changed" \
    "$src/reconnect/AuthenticatedHistoricalFixtureConsumerTest.java"
expect_failure "misclassified allowed rejection boundary"

echo "P07 final executable HAPI policy positive and intentional-failure controls passed"
