# P07-10B Mixed-Suite Execution Ownership

## Boundary

P07-10B separates retained native transaction coverage from executable EVM and
HTS-precompile scenarios embedded in the same test-client suites. A test is
retained only when its exercised production path still exists after P07-8.
Contract-mediated access to a native service is executable coverage, not native
coverage.

The checkpoint before this work was commit
`eb18adca5fba9aba122193c01af8ca108f3af3cd`; exact-head CI run
`30178250675` passed.

## Mixed-suite census

| Owner | Executable responsibility | Retained responsibility | Decision |
| --- | --- | --- | --- |
| `AirdropsDisabledTest` | Three CREATE2 deployment/call scenarios | Native disabled-airdrop status and fee behavior | Delete executable methods; retain native tests |
| `TokenAirdropTest` | Contract recipients, contract-mediated airdrops, and CREATE2 recipient scenarios | Native airdrop, claim, cancel, association, allowance, and fee behavior | Delete the contract method and `ToContracts` nested scenario group |
| `CryptoApproveAllowanceSuite` | ERC-20 contract approval/transfer and contract-account payer setup | Native crypto allowance behavior | Delete two executable methods |
| `AtomicBatchApproveAllowanceTest` | ERC-20 contract approval/transfer and contract-account payer setup | Native atomic-batch allowance behavior | Delete two executable methods |
| `RepeatableHip423Tests` | Scheduled contract create/call/update/delete, scheduled HTS precompile, delegate call, and gas execution | Native schedule creation, signing, expiry, token, account, and transfer behavior | Delete eight executable methods and the execution-only setup/assertion helpers |
| `AtomicBatchContractKeysHtsTest` | HTS precompile execution through contract keys | None | Delete class |
| `AtomicBatchAddress167Test` | HTS precompile execution through address `0x167` | None | Delete class |

The removed methods used `ContractCreate`, `ContractCall`, Solidity init code,
CREATE2, HTS precompile ABI results, execution gas assertions, or execution-only
child-record assertions. Native equivalents already remain in the same feature
areas; no mock execution path was introduced.

## Helper disposition

| Helper | Classification | Disposition |
| --- | --- | --- |
| `Create2OperationSuite` | `CREATE2_EXECUTION` | Deleted after reverse-consumer count reached zero |
| `HTSPrecompileResult` | `EXECUTABLE_RESULT_DECODING` | Deleted after live precompile consumers were removed; no historical consumer existed |
| `Signing` | Ethereum transaction construction | Retained temporarily with `HapiEthereumCall` and `HapiEthereumContractCreate`; not misclassified as native signing |
| `NativeEcdsaSigning` | Native authorization support | Retained |
| `HistoricalContractResultFixtures` | Historical result interpretation | Retained |
| `HistoricalSidecarFixtures` | Historical sidecar interpretation | Retained |
| `HistoricalStorageEncoding` | Historical storage validation | Retained |
| `LegacyTransactionVectors` | Deterministic rejection vectors | Retained |

Six unreferenced CREATE2 resource triples were removed. `Create2OpHook` remains
owned by HIP-1195 historical storage validation, and `VariousCreate2Calls`
remains owned by a separately measured contract-update suite; neither was
silently included in this bounded deletion.

## Dependency result

Test-client direct importers at this checkpoint:

- Besu: 7 (unchanged by P07-10B);
- Tuweni: 13 (down from 15);
- fixture-tooling Besu/Tuweni importers: 0;
- protected platform/base-crypto Besu importers: 1.

The remaining Besu/Tuweni closure is not HTS-precompile or CREATE2 mixed-suite
ownership. It consists of historical translation/conversion, native ECDSA and
alias support, generic test-client transaction infrastructure, and residual
Ethereum transaction construction. It requires a fresh bounded ownership gate
before further deletion.
