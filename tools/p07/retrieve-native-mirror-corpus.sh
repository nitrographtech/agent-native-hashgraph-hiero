#!/bin/sh
# SPDX-License-Identifier: Apache-2.0
set -eu

repo=nitrographtech/agent-native-hashgraph-hiero
tag=p07-native-corpus-689e32ac-v1
asset=p07-native-corpus-689e32ac-v1.tar.gz
archive_sha=f33b98c822a6f32e6c783fd338337df7bd31ca030b4c780369a01e848a4850e0
manifest_sha=e6b231fc858ba8d3dd510c1a360874df07f7630091b4ffe4e980a8a5196bf1ef
repo_root=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
output=${1:?usage: retrieve-native-mirror-corpus.sh EMPTY_OUTPUT_DIRECTORY}

fail() {
    echo "P07 native corpus retrieval: $1" >&2
    exit 1
}

[ -d "$output" ] || fail "output directory must already exist"
[ -z "$(find "$output" -mindepth 1 -maxdepth 1 -print -quit)" ] ||
    fail "output directory must be empty"
command -v curl >/dev/null 2>&1 || fail "curl is required"
command -v sha256sum >/dev/null 2>&1 || fail "sha256sum is required"

archive="$output/$asset"
url="https://github.com/$repo/releases/download/$tag/$asset"
curl --fail --location --silent --show-error "$url" --output "$archive"
[ "$(sha256sum "$archive" | awk '{print $1}')" = "$archive_sha" ] ||
    fail "archive checksum mismatch"
tar -xzf "$archive" -C "$output"
root="$output/p07-native-corpus-689e32ac-v1"
"$repo_root/tools/p07/verify-native-mirror-corpus.sh" "$root"
printf 'P07_NATIVE_CORPUS=%s\n' "$root"
printf 'P07_NATIVE_ARCHIVE_SHA256=%s\n' "$archive_sha"
printf 'P07_NATIVE_MANIFEST_SHA256=%s\n' "$manifest_sha"
