#!/usr/bin/env bash
set -euo pipefail

# THIS IDENTITY IS PUBLIC, COMPROMISED BY DESIGN, AND MUST NEVER BE USED OUTSIDE
# THE P06A FIXTURE HARNESS.

readonly SOURCE_ROOT=${1:?usage: generate-public-fixture-identity.sh SOURCE_ROOT OUTPUT_DIRECTORY --enable-public-p06a-fixture-identity --acknowledge-compromised-test-key}
readonly OUTPUT_DIRECTORY=${2:?usage: generate-public-fixture-identity.sh SOURCE_ROOT OUTPUT_DIRECTORY --enable-public-p06a-fixture-identity --acknowledge-compromised-test-key}
readonly ENABLE_FLAG=${3:-}
readonly ACK_FLAG=${4:-}

test "$ENABLE_FLAG" = "--enable-public-p06a-fixture-identity"
test "$ACK_FLAG" = "--acknowledge-compromised-test-key"
test -d "$SOURCE_ROOT"
test -d "$OUTPUT_DIRECTORY"
test ! -e "$OUTPUT_DIRECTORY/s-private-node1.pem"
test ! -e "$OUTPUT_DIRECTORY/s-public-node1.pem"

P06A_PUBLIC_FIXTURE_WORKFLOW=explicitly-enabled \
  "$SOURCE_ROOT/gradlew" \
  -p "$SOURCE_ROOT" \
  :fixture-tooling:generateP06aFixtureIdentity \
  -Pp06aFixtureNetworkId=123 \
  -Pp06aFixtureNodeId=0 \
  -Pp06aFixtureIdentityOutput="$OUTPUT_DIRECTORY" \
  --no-daemon

chmod 600 "$OUTPUT_DIRECTORY/s-private-node1.pem"
