#!/bin/sh
# SPDX-License-Identifier: Apache-2.0
set -eu

summary=${1:?usage: verify-mirror-importer-summary.sh SUMMARY [EXPECTATIONS]}
repo_root=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
expectations=${2:-"$repo_root/tools/p07/mirror/expected-corpora.tsv"}
expected=$(mktemp "${TMPDIR:-/tmp}/p07-mirror-summary.XXXXXX")
trap 'rm -f "$expected"' EXIT HUP INT TERM

awk -F '	' '
    !/^#/ && NF {
        print $1 "-records\tP07_RECORDS=" $2 " TRANSACTIONS=" $3 \
            " RESULTS=" $4 " LOGS=" $5 " ACTIONS=" $6 " SIDECARS=" $7
        if ($8 != "-") {
            print $1 "-blocks\tP07_BLOCKS=" $8 " PERSISTED=" $8
        }
    }
' "$expectations" >"$expected"

cmp "$expected" "$summary"
echo "P07 pinned mirror importer summary matches committed corpus expectations"
