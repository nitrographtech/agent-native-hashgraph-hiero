#!/bin/sh
# SPDX-License-Identifier: Apache-2.0
set -eu

repo_root=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
verifier="$repo_root/tools/p07/verify-execution-only-hapi-suites-removed.sh"
tmp_root=$(mktemp -d "${TMPDIR:-/tmp}/p07-11a-policy.XXXXXX")
trap 'rm -rf "$tmp_root"' EXIT HUP INT TERM
src="$tmp_root/hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites"

mkdir -p "$src/contract/hapi" "$src/contract/records"
: >"$src/contract/hapi/ContractUpdateSuite.java"
: >"$src/contract/records/ContractRecordsSanityCheckSuite.java"

expect_failure() {
    name=$1
    if "$verifier" "$tmp_root" >/dev/null 2>&1; then
        echo "Expected policy failure for $name" >&2
        exit 1
    fi
}

mkdir -p "$src/contract/classiccalls"
: >"$src/contract/classiccalls/FailureCharacterizationSuite.java"
expect_failure "classic-call package recreation"
rm -rf "$src/contract/classiccalls"

mkdir -p "$src/contract/ethereum"
: >"$src/contract/ethereum/HelloWorldEthereumSuite.java"
expect_failure "Ethereum execution suite recreation"
rm "$src/contract/ethereum/HelloWorldEthereumSuite.java"

mkdir -p "$src/contract/fees"
: >"$src/contract/fees/SmartContractServiceFeesTest.java"
expect_failure "contract fee suite recreation"
rm "$src/contract/fees/SmartContractServiceFeesTest.java"

mkdir -p "$src/contract/opcodes"
: >"$src/contract/opcodes/GasPriceSuite.java"
expect_failure "opcode execution suite recreation"
rm "$src/contract/opcodes/GasPriceSuite.java"

mkdir -p "$src/contract/hips/hip632"
: >"$src/contract/hips/hip632/AliasTest.java"
expect_failure "annotation-driven execution suite recreation"

echo "P07-11A execution-only HAPI suite intentional-failure controls passed"
