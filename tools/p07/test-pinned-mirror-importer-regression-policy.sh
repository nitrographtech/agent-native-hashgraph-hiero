#!/bin/sh
# SPDX-License-Identifier: Apache-2.0
set -eu

repo_root=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
expectations="$repo_root/tools/p07/mirror/expected-corpora.tsv"
tmp_file=$(mktemp "${TMPDIR:-/tmp}/p07-mirror-expectations.XXXXXX")
summary=$(mktemp "${TMPDIR:-/tmp}/p07-mirror-summary.XXXXXX")
trap 'rm -f "$tmp_file" "$summary"' EXIT HUP INT TERM

cp "$expectations" "$tmp_file"
awk -F '	' '
    !/^#/ && NF {
        print $1 "-records\tP07_RECORDS=" $2 " TRANSACTIONS=" $3 \
            " RESULTS=" $4 " LOGS=" $5 " ACTIONS=" $6 " SIDECARS=" $7
        if ($8 != "-") print $1 "-blocks\tP07_BLOCKS=" $8 " PERSISTED=" $8
    }
' "$expectations" >"$summary"
"$repo_root/tools/p07/verify-mirror-importer-summary.sh" "$summary" "$expectations" >/dev/null

# An intentionally corrupted transaction expectation must not validate.
sed 's/^fixture-a	11	754	/fixture-a	11	755	/' "$tmp_file" >"$tmp_file.changed"
mv "$tmp_file.changed" "$tmp_file"
if "$repo_root/tools/p07/verify-mirror-importer-summary.sh" "$summary" "$tmp_file" >/dev/null 2>&1; then
    echo "Expected corrupted mirror corpus assertion to fail" >&2
    exit 1
fi

echo "P07 pinned mirror importer expectation corruption control passed"
