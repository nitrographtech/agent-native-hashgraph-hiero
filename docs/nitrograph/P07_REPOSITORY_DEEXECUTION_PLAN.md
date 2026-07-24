# P07 Repository De-execution Plan

Status: plan complete; no deletion authorized or performed  
Source census: `P07_REPOSITORY_DEEXECUTION_CENSUS.md`  
Protected inventory: `P07_PROTECTED_COMPATIBILITY_INVENTORY.md`

## Objective

Physically remove legacy executable contract infrastructure from repository ownership after P06B
made the Nitrograph native artifact mechanically non-executable. Preserve historical saved states,
PBJ and protobuf models, schemas, identifiers, neutral APIs, records, sidecars, blocks, and mirror
interpretation.

Every removal wave must compile independently and pass its bounded tests. A wave is not allowed to
borrow success from a later cleanup.

## Architecture and dependency graph

### Forward execution graph

```mermaid
flowchart TD
  FULL[Full application profile] --> FP[FullContractRuntimeProvider]
  STAND[Standalone TransactionExecutors] --> FP
  FIX[Fixture-generation suites] --> FULL
  FP --> CSI[ContractServiceImpl]
  CSI --> HANDLERS[Executable transaction/query handlers]
  HANDLERS --> ETH[Ethereum transaction hydration and execution]
  HANDLERS --> EVM[EVM processors and custom operations]
  EVM --> BESU[Besu EVM and datatypes]
  EVM --> TUWENI[Tuweni bytes and units]
  EVM --> WORLD[Mutable world state and stores]
  EVM --> TRACE[Execution tracers]
  EVM --> SYSTEM[HAS/HSS/HTS system contracts]
  SYSTEM --> TOKEN[Token, schedule, account service APIs]
  HANDLERS --> BUILDERS[Neutral contract stream-builder interfaces]
  TRACE --> PBJ[PBJ results/actions/state changes/sidecars]
  WORLD --> MAPS[Persisted contract maps]
  BUILDERS --> RECORDS[Records and block streams]
  PBJ --> RECORDS

  HIST[HistoricalContractRuntimeProvider] --> FAIL[Fail-closed legacy handlers]
  HIST --> HSTATE[HistoricalContractStateService]
  HSTATE --> MAPS
  MAPS --> ADAPTERS[Read-only historical adapters]
  ADAPTERS --> RECORDS
  RECORDS --> MIRROR[Official mirror ingestion]
  PLATFORM[Hashgraph platform] --> HIST
```

The deletion cut is above the neutral builders/PBJ boundary and beside, not through, the historical
provider/state-service path.

### Reverse graph: executable-module consumers

```mermaid
flowchart LR
  IMPL[app-service-contract-impl]
  APP[hedera-app full and standalone sources] --> IMPL
  CLIENT[test-clients: 34 direct importers] --> IMPL
  VALIDATOR[state-validator: 2 direct importers] --> IMPL
  APPJPMS[hedera-app module-info] --> IMPL
  CLIENTJPMS[test-clients module-info] --> IMPL
  VALIDATORJPMS[state-validator module-info] --> IMPL
  FULLDIST[distributionFull] --> IMPL
  HAPICI[smart-contract HAPI CI] --> CLIENT
  PERF[smart-contract performance CI] --> CLIENT
  FIXTURE[P06 fixture generation] --> CLIENT

  IMPL --> BESU[Besu EVM/datatypes/crypto]
  IMPL --> TUWENI[Tuweni bytes/units]
  BASE[platform-sdk base-crypto] --> SECP[Besu-native secp256k1]
```

Exact direct import ownership is `test-clients=34`, `hedera-app=16`, and
`hedera-state-validator=2`. Some app imports are the six neutral builder interfaces whose fully
qualified legacy packages are now owned by `app-service-contract`; those are protected and are not
implementation edges.

## Strategy

1. Freeze P06B and its authenticated corpus as the semantic oracle.
2. Remove construction roots and external consumers before implementation internals.
3. Externalize executable fixture generation before removing its runtime.
4. Remove leaf executable producers from the monolithic implementation while keeping each wave
   compiling.
5. Delete the empty implementation module only after all reverse edges are gone.
6. Remove third-party dependencies, JPMS edges, CI, licenses, and notices only after source absence
   proves they are unused.
7. Re-run the full P06 compatibility matrix after every high-risk wave and at final exit.

## Compile-safe removal schedule

The suggested Besu-before-EVM order is not compile-safe: production EVM sources import Besu. This
plan removes the EVM integration source first, then removes the now-unused Besu/Tuweni dependencies.

| Wave | Removal | Compile fallout and required moves | Tests / fixtures / docs / CI | Blast radius and gate |
|---:|---|---|---|---|
| 1 | Standalone execution | Delete `workflows/standalone/**`; remove `StandaloneFeeCalculatorImpl` dependency on `TransactionExecutors`; remove standalone Dagger component and app exclusions | Remove or replace standalone-only tests; full node and fixture generator remain | Medium. App compile, native/full build, historical lifecycle |
| 2 | Fixture-only executable tooling | Dedicated `fixture-tooling` module owns the P06A historical population entry point and guarded PEM-writing command; general HAPI/migration infrastructure remains in `test-clients`; retrieval/verification remains permanent | Published source commit, toolchain, transaction sequence, identity, hashes, and regeneration contract remain frozen | COMPLETE after exact-head lifecycle/mirror/CI gates; existing fixtures remain independently consumable |
| 3A | Isolate concrete fixture tracing | **COMPLETE.** `EvmActionTracer`, action stack, wrappers, helpers, and factory are fixture-tooling-only. | Preserve authenticated three-action/seven-sidecar reproduction. | Complete |
| 3B | Remove runtime tracer aggregation and concrete producers | **COMPLETE.** Normal runtime uses `NoTracer`; concrete fixture tracers are absent from native and full runtime artifacts. | Preserve neutral historical action interpretation. | Complete |
| 3C | Remove residual callback protocol | **DEFERRED TO WAVES 4–8.** `ActionSidecarContentTracer`, factory seam, `NoTracer`, and propagation remain only in executable legacy source. | Delete each callback with its owning system-contract, Ethereum, world-state, Bonneville, or EVM processor. No fixture-specific EVM processor duplication. | `LEGACY_EXECUTION_SEAM_DEFERRED_TO_ENGINE_REMOVAL` |
| 4 | System contracts | Remove `exec/systemcontracts/**`, its Dagger bindings, executable precompile tests, and full-only service hooks | Retain token/account/schedule native APIs; remove smart-contract precompile CI partitions | Very high. Native token/account suites and protected historical streams must pass |
| 5 | Solidity execution assets | Remove full-only Solidity execution/query handlers and compiler/source fixtures no longer needed after Wave 2 | Retain wire names such as `getBySolidityID`, address widths, PBJ fields, and historical query rejection | Medium. Do not cosmetically rename historical fields |
| 6 | Ethereum execution | Remove Ethereum handler, hydration, transaction factories/processors, signature cache used only for execution, and executable Ethereum suites | Retain Ethereum PBJ bodies/results for decoding, rejection, and mirror | High. Historical Ethereum mirror counts and five-body rejection must pass |
| 7 | World-state implementation | Remove mutable stores, `WorldUpdater`, frame state, proxy accounts, writable EVM hook stores, and `FullContractStoreFactory` | First move any remaining validator schema references to neutral history API; retain historical read-only adapters | Very high. All four retained map inventories must be unchanged |
| 8 | EVM engine integration | Remove HEVM, frame runners, processors, custom operations, versioned EVM modules, gas/executable metrics, Bonneville, and native-lib verifier | Remove associated implementation tests and full provider construction | Very high. This retires the full executable application profile |
| 9 | Besu/Tuweni and executable native dependencies | Remove app/test-client JPMS edges and version-catalog/dependency metadata after source scans are zero | Remove notices/SBOM components only when no retained artifact carries them | Blocked at `platform-sdk/base-crypto` without upstream neutral verifier or separate platform authorization |
| 10 | `app-service-contract-impl` module | Delete the now-empty source/test tree, Gradle project mapping, full provider/factory, and remaining implementation tests | `RecordStreamBuilder`, six builder interfaces, historical schemas/stores remain in neutral API | High structural gate. Repository-wide implementation import and class scans must be zero |
| 11 | Gradle cleanup | Remove full distribution task/profile, implementation dependency edges, fixture-only configurations, unused versions/plugins | Preserve native distribution and neutral API publications | Medium. Run all builds and dependency analysis |
| 12 | JPMS/service cleanup | Remove implementation/Besu/EVM/Tuweni requires, full providers, executable `uses/provides`, reflection strings, Dagger generated roots | Preserve historical factories and retained legacy builder packages | Medium. Module graph and isolated classpath policy must pass |
| 13 | CI cleanup | Remove smart-contract execution, standalone, full-runtime, Solidity, and performance jobs; retain P06 historical lifecycle/mirror gates | Update change detection and required-check policy explicitly | High governance risk. Never remove a required check before replacement is active |
| 14 | Documentation/licensing cleanup | Remove executable-operation docs and obsolete notices; mark full runtime retired | Preserve all P06/P07 evidence, fixture provenance, historical field documentation, unresolved findings | Low source risk, high audit importance |

## Wave validation levels

### Every wave

- Changed modules compile.
- Native distribution builds and passes binary dependency policy.
- No protected file, state key, PBJ/protobuf definition, codec, state ID, schema version, or service
  identifier changes.
- Secret and developer-path audits pass.

### Waves 3–10

- Native startup and historical provider/store selection.
- Authenticated historical reopen, save, restart, and replay.
- Five legacy bodies reject with `INVALID_TRANSACTION_BODY`.
- `STORAGE`, `BYTECODE`, `EVM_HOOK_STATES`, and `LAMBDA_STORAGE` remain unchanged.
- Record, sidecar, and block golden fingerprints remain unchanged.

### Waves 4, 6, 7, 8, 9, and 10

- Real four-node reconnect and state synchronization.
- Post-reconnect account, asset, and Coordination Layer processing.
- Official mirror record, sidecar, and block ingestion.
- Exact native classpath absence policy.

## Recommended Wave 1 implementation

Implementation status: **COMPLETE** on
`p07/remove-standalone-execution`. Production standalone sources and their
standalone-only tests have been removed. The authenticated fixture path was
confirmed to use the pinned full node rather than `TransactionExecutors`.
The exact-head lifecycle, reconnect, synchronization, official mirror, and CI
gates pass.

Wave 1 should be a bounded PR titled:

`P07-1: remove standalone executable transaction infrastructure`

Allowed production scope:

- `hedera-node/hedera-app/src/main/java/com/hedera/node/app/workflows/standalone/**`
- `hedera-node/hedera-app/src/main/java/com/hedera/node/app/fees/StandaloneFeeCalculatorImpl.java`
- exact Dagger/JPMS/build references required by those classes

Expected deletions are seven production standalone files (45,423 source bytes) plus three direct
standalone tests and dependent fee/record tests that cannot be expressed without the executor.

Before deletion, trace callers of `StandaloneFeeCalculatorImpl`. If it supports a permanent native
fee API, extract only its neutral calculator contract; do not retain `TransactionExecutors`.

Wave 1 acceptance:

- `TransactionExecutors` and `ActionSidecarContentTracer` are absent from app production source.
- Native and full application compilation succeeds.
- No standalone Dagger component or service entry remains.
- Fixture generation still uses the full node path, not standalone execution.
- All P06 native lifecycle, map, rejection, reconnect, synchronization, and mirror gates remain
  valid.

## Wave 2 implementation

Status: **COMPLETE** on `p07/externalize-fixture-execution-tooling`.

- `HistoricalContractStateFixtureCreation` moved from the test-client production artifact to the
  dedicated `fixture-tooling` module.
- PEM-writing and command-line parsing moved from the reconnect identity utility into
  `P06aFixtureIdentityGenerator` in the tooling module.
- `P06aPublicFixtureIdentity.fixtureKeysAndCerts()` remains in `test-clients` because it is shared
  by the real four-node reconnect harness and does not write or publish identity material.
- `DiverseStateCreation` remains paired with `DiverseStateValidation` as general full-node migration
  test infrastructure.
- No Solidity corpus was broadly moved or deleted.
- Runtime distributions have no dependency on or packaged class from `fixture-tooling`.
- The exact-head four-node reconnect, state synchronization, historical-map comparison, official
  mirror record/sidecar/block ingestion, full-runtime tests, and isolation policies pass.

Wave 3A (fixture tracer isolation) and Wave 3B (runtime aggregation/concrete-producer removal) are
**COMPLETE**. Wave 3C, the residual callback protocol, is
`LEGACY_EXECUTION_SEAM_DEFERRED_TO_ENGINE_REMOVAL`: duplicating frame, message, HEVM, Bonneville,
and result processors in fixture tooling was rejected. Callback paths now collapse with their
owning processors in Waves 4–8.

Wave 4 (system-contract removal) is implemented on `p07/remove-system-contracts` and awaits the
exact-head lifecycle, reconnect, and mirror gates. It deletes the EVM-facing registries,
translators, calls, redirects, ABI method registry, Dagger composition, and secondary metrics for
addresses `0x167`–`0x16c`. Native token, account, schedule, exchange-rate, randomness, and hook
services remain. Generic EVM precompiles and the ordinary `0x16d` account-hook execution seam are
deferred to their owning engine waves.

## Risk assessment

| Risk | Severity | Evidence | Control |
|---|---|---|---|
| Persisted schema or identifier accidentally deleted with implementation schemas | Critical | Implementation still contains duplicate/full V0.49/V0.65 schemas while neutral history schemas are authoritative | Protected inventory plus state-key/codecs diff guard |
| Test-client utilities mix generic address behavior with executable helpers | High | 34 direct implementation importers and cross-suite static imports | Classify each utility; move only proven generic PBJ/byte behavior |
| System-contract removal damages native token/account tests | High | 183 production system-contract files coupled to token/schedule/account APIs | Remove callers, not native APIs; run native service suites |
| Fixture authenticity becomes non-reproducible | Critical | Current generation needs pinned full runtime | Freeze/externalize tooling before executable source removal |
| Tracer deletion changes fixture reproduction | Critical | Live PBJ actions have no producer other than `EvmActionTracer`/`ActionStack`; authenticated corpus contains three actions | Stop Wave 3; authorize fixture-only producer ownership or revise the fixture contract |
| Platform crypto prevents total Besu removal | Critical | One protected `platform-sdk/base-crypto` Besu verifier and JPMS edge | Stop Wave 9; require upstream neutral release or separate approval |
| CI deletion hides regressions | High | Smart-contract jobs are transitively enabled by several workflows | Establish replacement historical gates before changing required checks |
| Historical “EVM/Solidity/gas” names mistaken for execution | High | Permanent PBJ, schema, and mirror fields retain legacy terminology | Protected inventory; prohibit cosmetic removal or renaming |
| Full distribution retirement changes app composition broadly | High | Full and native profiles share common app source | Remove full-only factories after consumers, keep common lifecycle unchanged |
| Licensing claims become inaccurate | Medium | Full distribution currently retains all P06B-removed components | Recompute exact source/binary SBOM; do not infer license removal from one jar |

## Estimated repository reduction

Directly measured candidate material:

- Contract implementation source: 686 files / 4,185,454 bytes.
- Contract HAPI suite Java: 191 files / 4,384,846 bytes.
- Contract test resources: 1,017 files / 4,326,334 bytes.
- Standalone production source: 7 files / 45,423 bytes.

This establishes a conservative direct range of approximately **1,700–1,900 files** and
**12–15 MB** of checkout source/resources, before counting cross-suite executable tests, CI YAML,
generated dependency metadata, or documentation. The larger benefit is dependency and licensing
surface reduction: the 14 executable artifacts already removed from native packaging become
removable from full/repository build graphs after Wave 10.

No Git pack-size, clone-time, or build-time reduction is claimed until a deletion branch measures it
against this base.

## Stop gates

Stop the affected route immediately if a wave requires:

- changing PBJ/protobuf definitions, persisted keys, codecs, state IDs, schema versions, or service
  identifiers;
- modifying historical output or official mirror semantics;
- redesigning the neutral compatibility API;
- changing consensus;
- changing `platform-sdk` without separate authorization;
- retaining executable behavior to interpret historical data.
