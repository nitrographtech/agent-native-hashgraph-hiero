#!/usr/bin/env bash
set -euo pipefail

repo_root=${REPO_OVERRIDE:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}
old_metadata="$repo_root/docs/nitrograph/fixtures/preactivation-v065.manifest.json"
new_metadata="$repo_root/docs/nitrograph/fixtures/postwrite-four-node-v065.metadata.json"
consumer="$repo_root/hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/reconnect/AuthenticatedHistoricalFixtureConsumerTest.java"

for required in \
  '"fixtureId": "p07-four-node-postwrite-v065-storage424242-v1"' \
  '"nodeCount": 4' \
  '"decodedValue": 424242' \
  '"replacesFixture": false' \
  '"privateMaterialIncluded": false'; do
  grep -Fq "$required" "$new_metadata"
done

test -f "$old_metadata"
grep -Rqs 'p06a-fixture-preactivation-v065-ff6490d-round4744' "$repo_root/docs/nitrograph"

for prohibited in ContractServiceImpl FullContractRuntimeProvider HederaEVM FrameRunner RootProxyWorldUpdater; do
  if grep -q "$prohibited" "$consumer"; then
    echo "Consumer harness imports executable infrastructure: $prohibited" >&2
    exit 1
  fi
done

if grep -Eq '\b(contractCreate|contractCall)\(' "$consumer"; then
  echo "Consumer harness must use explicit rejected bodies, never live deployment helpers" >&2
  exit 1
fi

echo "Authenticated fixture governance policy passed"
