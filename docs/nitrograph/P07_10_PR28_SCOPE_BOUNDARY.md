# P07-10 PR #28 Scope Boundary

## Decision

PR #28 is the bounded P07-10 test-client execution-residue cleanup and ownership-separation
change. Its implementation scope is frozen after commit
`c24bb9e0172ca2920c6b06e6932e2dce84ae2135`. Commit
`60387ac69a221bf5e2226fc0559c445a113ae6e3` adds only the P07-11 ownership census and
expectation-drift matrix.

The remaining executable HAPI closure is not implemented in PR #28. The census measures 93
directly affected suite files (3,182,123 bytes), 426 literal `contractCreate` operations, 305
literal `contractCall` operations, 74 literal `ethereumCall` operations, 15 executable/local-query
users, and hundreds of Solidity, ABI, and binary resources. Adding that closure would combine a
new multi-wave retirement with an already reviewable 85-file P07-10 change and violate linear
merge discipline.

## Included implementation

- Deleted 15 zero-reverse-consumer executable suites.
- Deleted two unused ABI helpers.
- Extracted historical result fixtures into `HistoricalContractResultFixtures`.
- Extracted historical sidecar fixtures into `HistoricalSidecarFixtures`.
- Extracted historical storage semantics into `HistoricalStorageEncoding`.
- Extracted system-account and alias vectors into `NativeAccountTestVectors`.
- Extracted immutable legacy resource identity into `LegacyTransactionVectors`.
- Extracted retained native signing into `NativeEcdsaSigning`.
- Deleted eight additional zero-consumer suite/helper files after ownership transfer.
- Removed live CREATE2 execution from mixed airdrop suites.
- Removed live HTS-precompile scenarios from mixed allowance, batch, and integration suites.
- Deleted `Create2OperationSuite`.
- Deleted `HTSPrecompileResult`.
- Deleted `AtomicBatchContractKeysHtsTest` and `AtomicBatchAddress167Test`.
- Deleted 18 unreferenced CREATE2 Solidity, ABI, and bytecode resources.
- Added the P07-11 executable-HAPI ownership census and linear roadmap.

Across the merged P07-9 baseline through the census head, PR #28 changes 85 files with 1,354
insertions and 23,861 deletions. No production runtime, platform-sdk, or base-crypto source is
changed.

## Explicitly excluded

- Broad `HapiContractCreate` retirement.
- Broad `HapiContractCall` retirement.
- `HapiEthereumCall` retirement.
- `HapiEthereumContractCreate` retirement.
- The 93 remaining affected executable HAPI suites.
- Remaining executable/local query infrastructure.
- Bulk Solidity, ABI, or compiled-binary deletion.
- Final Besu, Tuweni, or Headlong removal.
- P07 closure certification.

## Linear follow-up

The remaining ownership is assigned to:

1. **P07-11A** — execution-only suites and providers.
2. **P07-11B** — contract-based setup embedded in retained native suites.
3. **P07-11C** — minimal deterministic-rejection and historical-query boundary, then executable
   HAPI operation deletion.
4. **P07-11D** — dead resources, signing residue, Gradle/JPMS dependency cleanup, and final
   lifecycle certification.

Known successful-execution expectations outside the changed P07-10 closure are measured follow-up
ownership, not regressions introduced by PR #28. They are not globally suppressed or rewritten as
meaningless rejection assertions.
