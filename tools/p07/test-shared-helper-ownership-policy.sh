#!/bin/sh
# SPDX-License-Identifier: Apache-2.0
set -eu

repo_root=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
verifier="$repo_root/tools/p07/verify-shared-helper-ownership.sh"
tmp_root=$(mktemp -d "${TMPDIR:-/tmp}/p07-10a-policy.XXXXXX")
trap 'rm -rf "$tmp_root"' EXIT HUP INT TERM

mkdir -p "$tmp_root/hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/crypto"
mkdir -p "$tmp_root/hedera-node/hedera-smart-contract-service/src/main/java/example"

expect_failure() {
    name=$1
    if "$verifier" "$tmp_root" >/dev/null 2>&1; then
        echo "Expected policy failure for $name" >&2
        exit 1
    fi
}

cat >"$tmp_root/hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/crypto/Injected.java" <<'EOF'
import static com.hedera.services.bdd.suites.contract.records.RecordsSuite.VALUE;
EOF
expect_failure "translator/native suite import of retired suite"
rm "$tmp_root/hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/crypto/Injected.java"

cat >"$tmp_root/hedera-node/hedera-smart-contract-service/src/main/java/example/Injected.java" <<'EOF'
import com.hedera.services.bdd.suites.utils.NativeEcdsaSigning;
EOF
expect_failure "production import of test helper"
rm "$tmp_root/hedera-node/hedera-smart-contract-service/src/main/java/example/Injected.java"

mkdir -p "$tmp_root/hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/records"
: >"$tmp_root/hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/records/RecordsSuite.java"
expect_failure "retired suite recreation"

echo "P07-10A shared-helper intentional-failure controls passed"
