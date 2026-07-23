#!/usr/bin/env bash
set -euo pipefail

readonly FIXTURE_DIR=${1:?usage: fingerprint-fixture.sh FIXTURE_DIR}
readonly OUTPUT_MANIFEST=${2:?usage: fingerprint-fixture.sh FIXTURE_DIR OUTPUT_MANIFEST}

test -d "$FIXTURE_DIR"
test ! -e "$OUTPUT_MANIFEST"

(
  cd "$FIXTURE_DIR"
  find . -type f -print0 \
    | sort -z \
    | xargs -0 sha256sum
) >"$OUTPUT_MANIFEST"

sha256sum "$OUTPUT_MANIFEST"
