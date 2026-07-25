#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
tmp_root="$(mktemp -d)"
trap 'rm -rf "$tmp_root"' EXIT

mkdir -p "$tmp_root/hedera-node/test-clients/src/main/resources/contract/contracts/AssociateTryCatch"
mkdir -p "$tmp_root/hedera-node/hedera-app/build/distributions"
touch "$tmp_root/hedera-node/test-clients/src/main/resources/contract/contracts/AssociateTryCatch/AssociateTryCatch.sol"

if P07_REPO_ROOT="$tmp_root" "$repo_root/tools/p07/verify-dead-solidity-assets-removed.sh" \
    >/dev/null 2>&1; then
  echo "P07 dead Solidity intentional source injection was not rejected" >&2
  exit 1
fi

rm -rf "$tmp_root/hedera-node/test-clients/src/main/resources/contract/contracts/AssociateTryCatch"
mkdir -p "$tmp_root/hedera-node/hedera-app/build/distributions/injected"
touch "$tmp_root/hedera-node/hedera-app/build/distributions/injected/SystemContractInterface.sol"

if P07_REPO_ROOT="$tmp_root" "$repo_root/tools/p07/verify-dead-solidity-assets-removed.sh" \
    >/dev/null 2>&1; then
  echo "P07 dead Solidity intentional distribution injection was not rejected" >&2
  exit 1
fi

echo "P07 dead Solidity intentional-failure controls passed"

