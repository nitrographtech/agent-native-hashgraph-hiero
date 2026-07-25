#!/usr/bin/env bash
set -euo pipefail

repo_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
verifier="$repo_root/tools/p07/verify-authenticated-fixture-governance.sh"
scratch=$(mktemp -d)
trap 'rm -rf "$scratch"' EXIT

mkdir -p "$scratch/docs/nitrograph/fixtures" \
  "$scratch/hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/reconnect"
cp "$repo_root/docs/nitrograph/fixtures/preactivation-v065.manifest.json" \
  "$repo_root/docs/nitrograph/fixtures/postwrite-four-node-v065.metadata.json" \
  "$scratch/docs/nitrograph/fixtures/"
cp "$repo_root/hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/reconnect/AuthenticatedHistoricalFixtureConsumerTest.java" \
  "$scratch/hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/reconnect/"
cp "$repo_root/docs/nitrograph/P07_8_FIXTURE_BOUNDARY_DECISION.md" "$scratch/docs/nitrograph/"

expect_failure() {
  local label=$1
  shift
  if "$@" >/dev/null 2>&1; then
    echo "Intentional failure was accepted: $label" >&2
    exit 1
  fi
}

metadata="$scratch/docs/nitrograph/fixtures/postwrite-four-node-v065.metadata.json"
sed -i 's/"nodeCount": 4/"nodeCount": 1/' "$metadata"
expect_failure wrong-roster env REPO_OVERRIDE="$scratch" "$verifier"

cp "$repo_root/docs/nitrograph/fixtures/postwrite-four-node-v065.metadata.json" "$metadata"
sed -i 's/"decodedValue": 424242/"decodedValue": 7/' "$metadata"
expect_failure wrong-storage env REPO_OVERRIDE="$scratch" "$verifier"

cp "$repo_root/docs/nitrograph/fixtures/postwrite-four-node-v065.metadata.json" "$metadata"
sed -i 's/p07-four-node-postwrite-v065-storage424242-v1/p06a-fixture-preactivation-v065-ff6490d-round4744/' "$metadata"
expect_failure duplicate-tag env REPO_OVERRIDE="$scratch" "$verifier"

consumer="$scratch/hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/reconnect/AuthenticatedHistoricalFixtureConsumerTest.java"
printf '\n// forbidden probe: contractCreate("probe")\n' >>"$consumer"
expect_failure live-contract-create env REPO_OVERRIDE="$scratch" "$verifier"

archive_scratch=$(mktemp -d)
cp "$repo_root/docs/nitrograph/fixtures/postwrite-four-node-v065.metadata.json" "$archive_scratch/metadata.json"
cp "$repo_root/tools/p07/verify-four-node-postwrite-fixture.sh" "$archive_scratch/verify.sh"
printf 'payload\n' >"$archive_scratch/payload"
(cd "$archive_scratch" && sha256sum metadata.json payload verify.sh >SHA256SUMS)
"$archive_scratch/verify.sh" "$archive_scratch" >/dev/null
printf 'corrupt\n' >>"$archive_scratch/payload"
expect_failure archive-hash "$archive_scratch/verify.sh" "$archive_scratch"
(cd "$archive_scratch" && sha256sum metadata.json payload verify.sh >SHA256SUMS)
printf '%s\n' '-----BEGIN PRIVATE KEY-----' >>"$archive_scratch/payload"
(cd "$archive_scratch" && sha256sum metadata.json payload verify.sh >SHA256SUMS)
expect_failure private-key-marker "$archive_scratch/verify.sh" "$archive_scratch"

echo "Authenticated fixture governance intentional-failure controls passed"
