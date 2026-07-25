#!/usr/bin/env bash
set -euo pipefail

repo_root="${P07_REPO_ROOT:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)}"
asset_root="$repo_root/hedera-node/test-clients/src/main/resources/contract/contracts"

readonly removed=(
  AssociateTryCatch AutoCreationModes ChildCallDataSizeCheck ClassicQueriesXTest
  CreateTokenVTwo DeleteTokenContract DirectPrecompileCallee DoTokenManagement
  ERC721ContractWithHTSCalls FreezeUnfreezeContract GracefullyFailingPrng HbarFeeCollector
  HtsApproveAllowance ImmediateChildAssociation InstantStorageHog MinimalTokenCreations
  MixedBurnToken MixedFramesScenarios MultiversionBurn NegativeAssociationsContract
  NegativeBurnContract NegativeDissociationsContract NegativeHtsTransferFrom NegativeMintContract
  NestedBurn NestedLazyCreateContract NewTokenCreateContract NonDelegateCryptoTransfer
  PauseUnpauseTokenAccount RedirectNullContract RedirectTestContract SafeOperations
  SomeERC20Scenarios SomeERC721Scenarios SpecialQueriesXTest TestApprover
  TokenDefaultKycAndFreezeStatus TokenExpiryContract TokenMiscOperations TransferAmountAndToken
  UpdateTokenFeeSchedules VersatileTransfers WorkingHours ZenosBank
)

for name in "${removed[@]}"; do
  if find "$asset_root/$name" -type f -print -quit 2>/dev/null | grep -q .; then
    echo "P07 dead Solidity asset policy violation: $asset_root/$name contains files" >&2
    exit 1
  fi
done

if find "$repo_root/hedera-node/hedera-app/build/distributions" \
    -type f \( -name '*.sol' -o -name '*.abi' \) -print -quit 2>/dev/null | grep -q .; then
  echo "P07 dead Solidity asset policy violation: runtime distribution packages compiler input" >&2
  exit 1
fi

echo "P07 dead Solidity asset removal policy passed"
