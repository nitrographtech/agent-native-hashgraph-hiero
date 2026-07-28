#!/bin/sh
# SPDX-License-Identifier: Apache-2.0
set -eu

readonly_pin=834a7a1cb9204b02c192098c60654d08eb85bc2a
repo_root=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
harness="$repo_root/tools/p07/mirror/P06bFixtureRegressionTest.java"
expectations="$repo_root/tools/p07/mirror/expected-corpora.tsv"
importer=${MIRROR_IMPORTER_DIR:?set MIRROR_IMPORTER_DIR to a hiero-mirror-node checkout}
output_dir=${P07_MIRROR_OUTPUT_DIR:-"$repo_root/build/p07-mirror-regression"}

fixture_a_records=${P07_FIXTURE_A_RECORDS:?set P07_FIXTURE_A_RECORDS}
fixture_a_blocks=${P07_FIXTURE_A_BLOCKS:?set P07_FIXTURE_A_BLOCKS}
fixture_b_records=${P07_FIXTURE_B_RECORDS:?set P07_FIXTURE_B_RECORDS}
fixture_b_blocks=${P07_FIXTURE_B_BLOCKS:?set P07_FIXTURE_B_BLOCKS}
native_records=${P07_NATIVE_RECORDS:?set P07_NATIVE_RECORDS}

fail() {
    echo "P07 pinned mirror importer regression: $1" >&2
    exit 1
}

command -v git >/dev/null 2>&1 || fail "git is required"
command -v sha256sum >/dev/null 2>&1 || fail "sha256sum is required"
[ -x "$importer/gradlew" ] || fail "MIRROR_IMPORTER_DIR is not a mirror-node checkout"
[ "$(git -C "$importer" rev-parse HEAD)" = "$readonly_pin" ] ||
    fail "mirror importer must be checked out at $readonly_pin"

worktree=$(mktemp -d "${TMPDIR:-/tmp}/p07-mirror-importer.XXXXXX")
cleanup() {
    git -C "$importer" worktree remove --force "$worktree" >/dev/null 2>&1 || true
    rm -rf "$worktree"
}
trap cleanup EXIT HUP INT TERM
rm -rf "$worktree"
git -C "$importer" worktree add --detach "$worktree" "$readonly_pin" >/dev/null

target="$worktree/importer/src/test/java/org/hiero/mirror/importer/parser/record/P06bFixtureRegressionTest.java"
cp "$harness" "$target"
mkdir -p "$output_dir"
summary="$output_dir/summary.tsv"
: >"$summary"

expected_row() {
    corpus=$1
    awk -F '	' -v corpus="$corpus" '$1 == corpus { print; found=1 } END { if (!found) exit 1 }' "$expectations"
}

run_records() {
    corpus=$1
    records_path=$2
    row=$(expected_row "$corpus") || fail "missing expectation for $corpus"
    old_ifs=$IFS
    IFS='	'
    set -- $row
    IFS=$old_ifs
    expected_records=$2
    expected_transactions=$3
    expected_results=$4
    expected_logs=$5
    expected_actions=$6
    expected_sidecars=$7

    (
        cd "$worktree"
        P06B_RECORD_STREAMS="$records_path" \
        P07_EXPECTED_RECORDS="$expected_records" \
        P07_EXPECTED_TRANSACTIONS="$expected_transactions" \
        P07_EXPECTED_RESULTS="$expected_results" \
        P07_EXPECTED_LOGS="$expected_logs" \
        P07_EXPECTED_ACTIONS="$expected_actions" \
        P07_EXPECTED_SIDECARS="$expected_sidecars" \
        ./gradlew :importer:test \
            --tests org.hiero.mirror.importer.parser.record.P06bFixtureRegressionTest.importsRecordsAndSidecars \
            --no-daemon --rerun-tasks
    )
    report="$worktree/importer/build/test-results/test/TEST-org.hiero.mirror.importer.parser.record.P06bFixtureRegressionTest.xml"
    [ -f "$report" ] || fail "record importer XML is missing for $corpus"
    cp "$report" "$output_dir/$corpus-records.xml"
    line="P07_RECORDS=$expected_records TRANSACTIONS=$expected_transactions RESULTS=$expected_results LOGS=$expected_logs ACTIONS=$expected_actions SIDECARS=$expected_sidecars"
    grep -F "$line" "$report" >/dev/null || fail "record importer output mismatch for $corpus"
    printf '%s\t%s\n' "$corpus-records" "$line" >>"$summary"
}

run_blocks() {
    corpus=$1
    blocks_path=$2
    row=$(expected_row "$corpus") || fail "missing expectation for $corpus"
    expected_blocks=$(printf '%s\n' "$row" | awk -F '	' '{print $8}')
    (
        cd "$worktree"
        P06B_BLOCK_STREAMS="$blocks_path" \
        P07_EXPECTED_BLOCKS="$expected_blocks" \
        ./gradlew :importer:test \
            --tests org.hiero.mirror.importer.parser.record.P06bFixtureRegressionTest.importsBlocks \
            --no-daemon --rerun-tasks
    )
    report="$worktree/importer/build/test-results/test/TEST-org.hiero.mirror.importer.parser.record.P06bFixtureRegressionTest.xml"
    [ -f "$report" ] || fail "block importer XML is missing for $corpus"
    cp "$report" "$output_dir/$corpus-blocks.xml"
    line="P07_BLOCKS=$expected_blocks PERSISTED=$expected_blocks"
    grep -F "$line" "$report" >/dev/null || fail "block importer output mismatch for $corpus"
    printf '%s\t%s\n' "$corpus-blocks" "$line" >>"$summary"
}

run_records fixture-a "$fixture_a_records"
run_blocks fixture-a "$fixture_a_blocks"
run_records fixture-b "$fixture_b_records"
run_blocks fixture-b "$fixture_b_blocks"
run_records exact-head-native "$native_records"

"$repo_root/tools/p07/verify-mirror-importer-summary.sh" "$summary" "$expectations"
summary_sha=$(sha256sum "$summary" | awk '{print $1}')
printf 'PINNED_IMPORTER=%s\n' "$readonly_pin"
printf 'HARNESS_SHA256=%s\n' "$(sha256sum "$harness" | awk '{print $1}')"
printf 'OUTPUT_SUMMARY_SHA256=%s\n' "$summary_sha"
cat "$summary"
