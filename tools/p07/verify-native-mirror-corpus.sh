#!/bin/sh
# SPDX-License-Identifier: Apache-2.0
set -eu

manifest_sha=e6b231fc858ba8d3dd510c1a360874df07f7630091b4ffe4e980a8a5196bf1ef
root=${1:?usage: verify-native-mirror-corpus.sh EXTRACTED_CORPUS_ROOT}

fail() {
    echo "P07 native corpus verification: $1" >&2
    exit 1
}

manifest="$root/MANIFEST.sha256.tsv"
[ -f "$manifest" ] || fail "embedded manifest is missing"
[ "$(sha256sum "$manifest" | awk '{print $1}')" = "$manifest_sha" ] ||
    fail "manifest checksum mismatch"

while IFS='	' read -r expected_hash expected_size relative; do
    file="$root/$relative"
    [ -f "$file" ] || fail "manifest file is missing: $relative"
    [ "$(wc -c <"$file" | tr -d ' ')" = "$expected_size" ] ||
        fail "size mismatch: $relative"
    [ "$(sha256sum "$file" | awk '{print $1}')" = "$expected_hash" ] ||
        fail "checksum mismatch: $relative"
done <"$manifest"

actual_files=$(find "$root/recordStreams" "$root/test-results" -type f | wc -l | tr -d ' ')
manifest_files=$(wc -l <"$manifest" | tr -d ' ')
[ "$actual_files" = "$manifest_files" ] ||
    fail "unexpected or missing files: manifest=$manifest_files actual=$actual_files"
records=$(find "$root/recordStreams" -type f -name '*.rcd.gz' | wc -l | tr -d ' ')
signatures=$(find "$root/recordStreams" -type f -name '*.rcd_sig' | wc -l | tr -d ' ')
[ "$records" = 3639 ] || fail "expected 3639 record files, found $records"
[ "$signatures" = 3639 ] || fail "expected 3639 signatures, found $signatures"
[ -z "$(find "$root/recordStreams" -type f -path '*/sidecar/*' -print -quit)" ] ||
    fail "unexpected execution sidecar"
echo "P07 immutable native corpus verification: PASS"
