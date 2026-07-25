#!/usr/bin/env bash
set -euo pipefail

fixture_dir=${1:?usage: verify-four-node-postwrite-fixture.sh FIXTURE_DIR}
metadata="$fixture_dir/metadata.json"
manifest="$fixture_dir/SHA256SUMS"

test -f "$metadata"
test -f "$manifest"
(cd "$fixture_dir" && sha256sum --check --quiet SHA256SUMS)

for required in \
  '"fixtureId": "p07-four-node-postwrite-v065-storage424242-v1"' \
  '"class": "POST_WRITE_MULTI_NODE"' \
  '"round": 2143' \
  '"combinedInventorySha256": "5f74ae63338dfb5510d0dc09a7afafe64f5ee00f9d0c7ed5c040cb5fe82dfa48"' \
  '"decodedValue": 424242' \
  '"nodeCount": 4' \
  '"nodeIds": [0, 1, 2, 3]' \
  '"weights": [1, 1, 1, 1]' \
  '"privateMaterialIncluded": false'; do
  rg -Fq "$required" "$metadata"
done

if rg -l --hidden --glob '!SHA256SUMS' --glob '!verify.sh' \
    '(BEGIN (RSA |EC |ENCRYPTED )?PRIVATE KEY|PRIVATE KEY-----|AKIA[0-9A-Z]{16}|gh[pousr]_[A-Za-z0-9_]{20,})' \
    "$fixture_dir"; then
  echo "Fixture contains prohibited private material" >&2
  exit 1
fi

if find "$fixture_dir" -type f \( -name '*.pfx' -o -name '*.p12' -o -name '*.jks' -o -name '*private*.pem' \) -print -quit | grep -q .; then
  echo "Fixture contains a prohibited private-key container" >&2
  exit 1
fi

echo "Four-node post-write fixture metadata, hashes, topology, and secret policy passed"
