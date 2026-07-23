#!/usr/bin/env bash
set -euo pipefail

readonly REPOSITORY_ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)
readonly TAG=p06a-fixture-preactivation-v065-ff6490d-round4744
readonly ASSET=nitrograph-p06a-preactivation-v065-ff6490d-round4744.tar.gz
readonly DOWNLOAD_DIRECTORY=${1:?usage: fetch-preactivation-v065.sh DOWNLOAD_DIRECTORY EXTRACTION_DIRECTORY}
readonly EXTRACTION_DIRECTORY=${2:?usage: fetch-preactivation-v065.sh DOWNLOAD_DIRECTORY EXTRACTION_DIRECTORY}

test ! -e "$DOWNLOAD_DIRECTORY"
test ! -e "$EXTRACTION_DIRECTORY"
mkdir -p "$DOWNLOAD_DIRECTORY"
gh release download "$TAG" \
  --repo nitrographtech/agent-native-hashgraph-hiero \
  --pattern "$ASSET" \
  --dir "$DOWNLOAD_DIRECTORY"
test -f "$DOWNLOAD_DIRECTORY/$ASSET"
"$REPOSITORY_ROOT/tools/p06a/fixtures/verify-preactivation-v065.sh" \
  "$DOWNLOAD_DIRECTORY/$ASSET" \
  "$EXTRACTION_DIRECTORY"
