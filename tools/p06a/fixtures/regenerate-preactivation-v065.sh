#!/usr/bin/env bash
set -euo pipefail

# THIS IDENTITY IS PUBLIC, COMPROMISED BY DESIGN, AND MUST NEVER BE USED OUTSIDE
# THE P06A FIXTURE HARNESS.

readonly SOURCE_ROOT=${1:?usage: regenerate-preactivation-v065.sh PINNED_SOURCE HARNESS_ROOT EMPTY_WORKSPACE ENABLE_FLAG ACK_FLAG}
readonly HARNESS_ROOT=${2:?usage: regenerate-preactivation-v065.sh PINNED_SOURCE HARNESS_ROOT EMPTY_WORKSPACE ENABLE_FLAG ACK_FLAG}
readonly WORKSPACE=${3:?usage: regenerate-preactivation-v065.sh PINNED_SOURCE HARNESS_ROOT EMPTY_WORKSPACE ENABLE_FLAG ACK_FLAG}
readonly ENABLE_FLAG=${4:-}
readonly ACK_FLAG=${5:-}
readonly EXPECTED_SOURCE=ff6490d66994da11af72e1d2f185ec7874fa383a

test "$ENABLE_FLAG" = "--enable-public-p06a-fixture-identity"
test "$ACK_FLAG" = "--acknowledge-compromised-test-key"
test "$(git -C "$SOURCE_ROOT" rev-parse HEAD)" = "$EXPECTED_SOURCE"
test -z "$(git -C "$SOURCE_ROOT" status --porcelain)"
test -d "$HARNESS_ROOT"
test ! -e "$WORKSPACE"
mkdir -m 700 "$WORKSPACE"
mkdir -m 700 "$WORKSPACE/identity"

"$HARNESS_ROOT/scripts/p06a/generate-public-fixture-identity.sh" \
  "$HARNESS_ROOT" \
  "$WORKSPACE/identity" \
  "$ENABLE_FLAG" \
  "$ACK_FLAG"

printf '%s\n' \
  'Identity generated into the restricted workspace.' \
  'Build the pinned full runtime and place this public certificate in both genesis-network certificate fields.' \
  'Run the transaction sequence documented in docs/nitrograph/P06A_HISTORICAL_FIXTURE_GENERATION_SPEC.md.' \
  'After clean freeze, construct the corpus without the generated private PEM and destroy the workspace identity.'
