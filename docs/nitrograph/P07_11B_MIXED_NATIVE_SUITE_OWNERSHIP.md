# P07-11B Mixed Native Suite Ownership

## Checkpoint

Baseline: merged P07-11A commit
`fafc26ce80576c896d385e3df51cecdd471e5f0d`.

This census covers the 59 `CONTRACT_BASED_TEST_SETUP` rows deferred by the P07-11 census plus
`ContractUpdateSuite` and `ContractRecordsSanityCheckSuite`, the two shared-owner suites explicitly
preserved by P07-11A. All 61 files exist at this checkpoint. Production, platform-sdk, base-crypto,
fixture, historical schema, Gradle, and JPMS source is outside this census.

The operation counts below are literal source occurrences. Test registrations count the repository
HAPI test annotations. A literal occurrence is a review locator, not by itself a deletion decision;
each containing method must be classified by the production behavior it tests.

## Exact suite census

| Suite | Tests | Create | Call | Ethereum | Local | Bytes |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| `consensus/AtomicTopicCreateSuite.java` | 25 | 1 | 0 | 0 | 0 | 38,551 |
| `consensus/TopicCreateSuite.java` | 26 | 1 | 0 | 0 | 0 | 30,395 |
| `contract/hapi/ContractUpdateSuite.java` | 19 | 20 | 7 | 0 | 1 | 35,027 |
| `contract/records/ContractRecordsSanityCheckSuite.java` | 6 | 4 | 1 | 0 | 0 | 12,064 |
| `crypto/CryptoTransferSuite.java` | 44 | 7 | 12 | 0 | 0 | 117,670 |
| `crypto/CryptoUpdateSuite.java` | 17 | 2 | 0 | 0 | 0 | 26,732 |
| `crypto/HollowAccountFinalizationSuite.java` | 18 | 4 | 3 | 0 | 0 | 59,727 |
| `crypto/LeakyCryptoTestsSuite.java` | 5 | 4 | 1 | 0 | 0 | 48,521 |
| `fees/CryptoSimpleFeesSuite.java` | 23 | 9 | 0 | 0 | 0 | 27,516 |
| `fees/CryptoTransferWithHooksSimpleFeesSuite.java` | 5 | 3 | 0 | 0 | 0 | 7,733 |
| `fees/KitchenSinkFeeComparisonSuite.java` | 4 | 9 | 0 | 0 | 0 | 164,458 |
| `fees/ScheduleServiceFeesSuite.java` | 1 | 1 | 1 | 0 | 0 | 6,416 |
| `fees/ScheduleServiceSimpleFeesTest.java` | 8 | 2 | 2 | 0 | 0 | 21,413 |
| `file/DiverseStateCreation.java` | 0 | 2 | 1 | 0 | 0 | 8,641 |
| `file/FileUpdateSuite.java` | 7 | 8 | 11 | 2 | 7 | 30,148 |
| `hip1195/Hip1195DisabledTest.java` | 6 | 1 | 0 | 0 | 0 | 19,156 |
| `hip1261/AtomicBatchCrossServiceSimpleFeesTest.java` | 32 | 1 | 0 | 0 | 0 | 119,400 |
| `hip1261/AtomicBatchNegativeSimpleFeesTest.java` | 36 | 2 | 1 | 1 | 0 | 80,701 |
| `hip1261/ContractServiceSimpleFeesTest.java` | 26 | 31 | 6 | 0 | 0 | 46,523 |
| `hip1261/CryptoCreateWithHooksSimpleFeesTest.java` | 5 | 3 | 0 | 0 | 0 | 6,448 |
| `hip1261/CryptoTransferSimpleFeesTest.java` | 82 | 10 | 0 | 0 | 0 | 245,138 |
| `hip1261/CryptoTransferWithCustomFeesAndHooksSimpleFeesTest.java` | 4 | 1 | 0 | 0 | 0 | 20,275 |
| `hip1261/CryptoTransferWithCustomFeesSimpleFeesTest.java` | 23 | 2 | 0 | 0 | 0 | 95,058 |
| `hip1261/CryptoTransferWithHooksSimpleFeesTest.java` | 12 | 1 | 0 | 0 | 0 | 28,017 |
| `hip1261/CryptoUpdateSimpleFeesTest.java` | 26 | 2 | 0 | 0 | 0 | 39,150 |
| `hip1261/ScheduleServiceSimpleFeesTest.java` | 31 | 1 | 1 | 0 | 0 | 51,640 |
| `hip1261/TokenAirdropSimpleFeesTest.java` | 34 | 1 | 0 | 0 | 0 | 94,783 |
| `hip1261/TokenClaimAndCancelAirdropSimpleFeesTest.java` | 24 | 1 | 0 | 0 | 0 | 88,484 |
| `hip423/ScheduleLongTermExecutionTest.java` | 9 | 1 | 2 | 0 | 0 | 10,334 |
| `hip551/AtomicBatchInvalidSignaturesTests.java` | 13 | 8 | 0 | 0 | 0 | 34,488 |
| `hip551/AtomicBatchNegativeTest.java` | 43 | 15 | 19 | 3 | 0 | 64,585 |
| `hip551/contracts/AtomicBatchContractSignatureValidationTest.java` | 11 | 12 | 4 | 0 | 0 | 28,054 |
| `hip551/contracts/AtomicBatchEndToEndSmartContractsHTSCallsAndAssociationsTest.java` | 26 | 37 | 66 | 0 | 0 | 135,607 |
| `hip551/contracts/AtomicBatchEthereumCallKeysTest.java` | 3 | 2 | 2 | 2 | 0 | 13,361 |
| `hip904/TokenAirdropBase.java` | 0 | 1 | 0 | 0 | 0 | 29,980 |
| `hip991/AtomicTopicCustomFeeCreateTest.java` | 37 | 1 | 0 | 0 | 0 | 51,077 |
| `hip991/AtomicTopicCustomFeeUpdateTest.java` | 52 | 1 | 0 | 0 | 0 | 67,885 |
| `hip991/TopicCustomFeeCreateTest.java` | 36 | 1 | 0 | 0 | 0 | 40,257 |
| `hip991/TopicCustomFeeUpdateTest.java` | 52 | 1 | 0 | 0 | 0 | 52,710 |
| `hip993/SystemFileExportsTest.java` | 0 | 1 | 1 | 0 | 0 | 38,150 |
| `integration/CongestionPricingTest.java` | 0 | 1 | 3 | 0 | 0 | 9,303 |
| `integration/RepeatableHip1215Tests.java` | 2 | 0 | 1 | 0 | 0 | 5,650 |
| `integration/RepeatableIntegrationTests.java` | 12 | 0 | 2 | 0 | 0 | 21,175 |
| `integration/RepeatableScheduleLongTermExecutionTest.java` | 18 | 2 | 1 | 0 | 0 | 49,141 |
| `integration/hip1195/Hip1195BasicTests.java` | 58 | 19 | 0 | 0 | 0 | 88,303 |
| `integration/hip1195/Hip1195StorageTest.java` | 18 | 1 | 0 | 0 | 0 | 27,650 |
| `integration/hip1259/Hip1259EnabledTests.java` | 24 | 3 | 2 | 0 | 0 | 54,089 |
| `issues/Issue305Spec.java` | 0 | 1 | 2 | 0 | 0 | 6,372 |
| `issues/IssueRegressionTests.java` | 17 | 4 | 0 | 0 | 0 | 18,571 |
| `misc/InvalidgRPCValuesTest.java` | 4 | 1 | 0 | 0 | 0 | 5,120 |
| `queries/AsNodeOperatorQueriesTestEmbedded.java` | 8 | 2 | 0 | 0 | 0 | 14,408 |
| `queries/DisabledNodeOperatorTest.java` | 10 | 2 | 2 | 0 | 2 | 13,443 |
| `queries/RepeatableOperatorQueryTests.java` | 14 | 1 | 0 | 0 | 0 | 15,442 |
| `regression/factories/AccountCompletionFuzzingFactory.java` | 0 | 1 | 0 | 0 | 0 | 6,164 |
| `regression/factories/HollowAccountCompletedFuzzingFactory.java` | 0 | 1 | 0 | 0 | 0 | 16,758 |
| `regression/factories/IdFuzzingProviderFactory.java` | 0 | 4 | 0 | 0 | 0 | 16,030 |
| `staking/StakingSuite.java` | 11 | 1 | 0 | 0 | 0 | 29,835 |
| `throttling/PrecompileMintThrottlingCheck.java` | 1 | 1 | 1 | 0 | 0 | 7,842 |
| `throttling/SteadyStateThrottlingTest.java` | 8 | 2 | 2 | 0 | 0 | 19,574 |
| `token/TokenAssociationSpecs.java` | 20 | 2 | 0 | 0 | 0 | 36,496 |
| `token/batch/AtomicTokenAssociationSpecs.java` | 26 | 2 | 0 | 0 | 0 | 39,162 |

Full paths share the prefix
`hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/`.

## Responsibility groups and method gate

### Accounts and aliases

`CryptoTransferSuite`, `CryptoUpdateSuite`, `HollowAccountFinalizationSuite`,
`LeakyCryptoTestsSuite`, the three regression factories, and the account-related portions of the
atomic-batch suites.

- Native account, alias, hollow-account, transfer, and ECDSA paths:
  `PURE_NATIVE_RETAIN`.
- Methods whose account state is created by contract create/call/Ethereum execution:
  `EXECUTION_ONLY_DELETE`, unless the same invariant can be initialized with native account verbs,
  in which case `NATIVE_WITH_CONTRACT_SETUP_REFACTOR`.

### Native Asset Service

`CryptoTransferSuite`, `TokenAirdropBase`, both token-association suites, the token/transfer/airdrop
fee suites, and token portions of atomic-batch and integration suites.

- Direct token bodies: `PURE_NATIVE_RETAIN`.
- Contract-mediated token or precompile behavior: `EXECUTION_ONLY_DELETE`.
- Native behavior with incidental executable setup:
  `NATIVE_WITH_CONTRACT_SETUP_REFACTOR`.

### Scheduling and atomic batch

Both schedule fee suites, `ScheduleLongTermExecutionTest`,
`RepeatableScheduleLongTermExecutionTest`, the six `hip551` batch suites, and both `hip1261`
atomic-fee suites.

- Native scheduled/batched bodies and native rollback: `PURE_NATIVE_RETAIN`.
- Scheduled or batched contract/Ethereum success: `EXECUTION_ONLY_DELETE`.
- Atomicity requiring only a failure trigger:
  `NATIVE_WITH_CONTRACT_SETUP_REFACTOR` to a retained native failure.

### Fees, congestion, and throttling

All `fees` suites, all `hip1261` fee suites, `CongestionPricingTest`,
`PrecompileMintThrottlingCheck`, and `SteadyStateThrottlingTest`.

- General framework behavior reachable with native bodies:
  `NATIVE_WITH_CONTRACT_SETUP_REFACTOR`.
- Contract gas, precompile, or contract-service fee behavior:
  `EXECUTION_ONLY_DELETE`.
- `CongestionPricingTest` must retain native transfer congestion and delete contract-gas
  congestion.

### Topics and Coordination Layer

Both topic-create suites and the four HIP-991 custom-fee suites.

- Native topic/custom-fee and authorization methods: `PURE_NATIVE_RETAIN`.
- Contract-based payer, collector, or setup: `NATIVE_WITH_CONTRACT_SETUP_REFACTOR`.
- Contract-originated behavior with no retained pathway: `EXECUTION_ONLY_DELETE`.

### Integration, file, query, staking, and issue coverage

`DiverseStateCreation`, `FileUpdateSuite`, `SystemFileExportsTest`, all `integration` suites, the
three query suites, `StakingSuite`, both issue suites, `InvalidgRPCValuesTest`, and
`Hip1195DisabledTest`.

- Retained service behavior with executable setup:
  `NATIVE_WITH_CONTRACT_SETUP_REFACTOR`.
- Live contract storage/hook/execution behavior:
  `EXECUTION_ONLY_DELETE`.
- Executable/local query interpretation and any historical fixture ownership:
  `HISTORICAL_INTERPRETATION_DEFER_TO_11C`.

### Shared preserved owners

- `ContractUpdateSuite`: successful executable updates are `EXECUTION_ONLY_DELETE`; its shared
  admin-key constants must move to a narrow test-vector owner before the suite can be deleted.
  Deterministic rejection belongs to P07-11C.
- `ContractRecordsSanityCheckSuite`: live record generation is `EXECUTION_ONLY_DELETE`; immutable
  historical record and mirror interpretation belongs to P07-11C. `PAYABLE_CONTRACT` consumers
  must be removed or re-owned before deletion.

## Resource and dependency ownership

Each file must be rescanned after method deletion for Solidity, ABI, bytecode, property, and JSON
references. A resource is deletable in P07-11B only when it has zero remaining source consumer.
Core operation resources, immutable fixtures, historical corpora, and P07-11C operation/query
resources are protected.

The current module-wide import baseline remains:

- Besu: 7 files;
- Tuweni: 13 files;
- Headlong: 46 files;
- fixture-tooling Besu/Tuweni: 0 files;
- protected base-crypto Besu: 1 file.

P07-11B targets suite ownership, not importer counts. Core HAPI operation classes and their
dependencies remain P07-11C/D even if this wave reduces their reverse consumers.

## Census decision

No production feature is proven to require executable contract setup. A bounded method-level
P07-11B implementation is architecturally possible, but it must proceed by responsibility group.
Unknown occurrences remain `SHARED_OWNER_REQUIRES_REVIEW` until their complete containing method,
lifecycle hooks, static setup, and reverse resource ownership are inspected. No broad exception
and no automatic SUCCESS-to-rejection conversion is authorized.
