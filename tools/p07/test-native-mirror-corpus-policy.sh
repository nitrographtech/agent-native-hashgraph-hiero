#!/bin/sh
# SPDX-License-Identifier: Apache-2.0
set -eu

repo_root=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
retriever="$repo_root/tools/p07/retrieve-native-mirror-corpus.sh"

fail() {
    echo "P07 native corpus controls: $1" >&2
    exit 1
}

first=$(mktemp -d "${TMPDIR:-/tmp}/p07-native-corpus-first.XXXXXX")
second=$(mktemp -d "${TMPDIR:-/tmp}/p07-native-corpus-second.XXXXXX")
cleanup() {
    rm -rf "$first" "$second"
}
trap cleanup EXIT HUP INT TERM

"$retriever" "$first" >/dev/null
"$retriever" "$second" >/dev/null
first_archive=$(sha256sum "$first/p07-native-corpus-689e32ac-v1.tar.gz" | awk '{print $1}')
second_archive=$(sha256sum "$second/p07-native-corpus-689e32ac-v1.tar.gz" | awk '{print $1}')
[ "$first_archive" = "$second_archive" ] || fail "independent retrievals differ"
cmp "$first/p07-native-corpus-689e32ac-v1/MANIFEST.sha256.tsv" \
    "$second/p07-native-corpus-689e32ac-v1/MANIFEST.sha256.tsv" >/dev/null ||
    fail "independent manifests differ"

root="$first/p07-native-corpus-689e32ac-v1"
victim=$(find "$root/recordStreams" -type f -name '*.rcd.gz' | LC_ALL=C sort | sed -n '1p')
missing="$victim.missing-control"
mv "$victim" "$missing"
if "$repo_root/tools/p07/verify-native-mirror-corpus.sh" "$root" >/dev/null 2>&1; then
    fail "missing-file control unexpectedly passed"
fi
mv "$missing" "$victim"

mkdir -p "$root/recordStreams/record0.0.3/sidecar"
: >"$root/recordStreams/record0.0.3/sidecar/prohibited.rcd"
if "$repo_root/tools/p07/verify-native-mirror-corpus.sh" "$root" >/dev/null 2>&1; then
    fail "prohibited execution sidecar control unexpectedly passed"
fi
echo "P07 native corpus prohibited-output control: expected failure observed"

echo "P07 native corpus retrieval, manifest, missing-file, and prohibited-output controls: PASS"
