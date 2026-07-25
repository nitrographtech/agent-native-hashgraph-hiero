#!/bin/sh
# SPDX-License-Identifier: Apache-2.0
set -eu

repo_root=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
verifier="$repo_root/tools/p07/verify-test-client-evm-residue-removed.sh"
tmp_root=$(mktemp -d "${TMPDIR:-/tmp}/p07-10b-policy.XXXXXX")
trap 'rm -rf "$tmp_root"' EXIT HUP INT TERM
src="$tmp_root/hedera-node/test-clients/src/main/java"

expect_failure() {
    name=$1
    if "$verifier" "$tmp_root" >/dev/null 2>&1; then
        echo "Expected policy failure for $name" >&2
        exit 1
    fi
}

mkdir -p "$src/com/hedera/services/bdd/suites/hip904"
cat >"$src/com/hedera/services/bdd/suites/hip904/TokenAirdropTest.java" <<'EOF'
class TokenAirdropTest { Object op = contractCreate("Injected"); }
EOF
expect_failure "contract deployment in retained native suite"

cat >"$src/com/hedera/services/bdd/suites/hip904/TokenAirdropTest.java" <<'EOF'
class TokenAirdropTest { Object op = contractCall("Injected"); }
EOF
expect_failure "contract call in retained native suite"
rm "$src/com/hedera/services/bdd/suites/hip904/TokenAirdropTest.java"

mkdir -p "$src/com/hedera/services/bdd/suites/contract/opcodes"
: >"$src/com/hedera/services/bdd/suites/contract/opcodes/Create2OperationSuite.java"
expect_failure "CREATE2 suite recreation"
rm "$src/com/hedera/services/bdd/suites/contract/opcodes/Create2OperationSuite.java"

mkdir -p "$src/com/hedera/services/bdd/suites/utils/contracts/precompile"
: >"$src/com/hedera/services/bdd/suites/utils/contracts/precompile/HTSPrecompileResult.java"
expect_failure "HTS precompile result recreation"
rm "$src/com/hedera/services/bdd/suites/utils/contracts/precompile/HTSPrecompileResult.java"

mkdir -p "$src/example"
cat >"$src/example/Injected.java" <<'EOF'
import com.hedera.services.bdd.suites.utils.contracts.precompile.HTSPrecompileResult;
EOF
expect_failure "HTS precompile helper import"

echo "P07-10B mixed-suite intentional-failure controls passed"
