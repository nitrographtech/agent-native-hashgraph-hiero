# P07-11B Method-Level Classification

## Authority

This documentation-only classification is based on
`e52102bbd1fd701eae70c10f0d1888c29fcad9b7`. It excludes the two shared
P07-11C owners, `ContractUpdateSuite` and `ContractRecordsSanityCheckSuite`.
No production or test source was changed during this pass.

The inventory was produced from the Java syntax tree, not from line-oriented
matching. Each operation is therefore assigned to its enclosing method or
factory. Helper-generated operations were also traced to their callers.

Notation:

- operations: `C` = `contractCreate`, `K` = `contractCall`, `E` =
  `ethereumCall`, and `L` = executable `contractCallLocal`;
- form: `D` = direct, `H` = helper-generated, `F` = dynamic factory;
- dependencies: `contract` includes bytecode/ABI/deployment verbs, `result`
  includes live contract result, log, action, gas, sidecar, or child-record
  assertions.

## Accounts, aliases, and native assets

| Source | Class and method | Operations/form | Behavioral purpose and dependencies | Final disposition | Supporting evidence |
| --- | --- | --- | --- | --- | --- |
| `crypto/CryptoTransferSuite.java` | `CryptoTransferSuite.canUseEip1014AliasesForXfers` | C1 K2/D | subject-under-test; contract-created alias; contract | `REMOVE_P07_11B` | All three operations deploy/call CREATE2-oriented contracts; native alias coverage is owned by the other transfer methods. |
| `crypto/LeakyCryptoTestsSuite.java` | `LeakyCryptoTestsSuite.contractDeployAfterEthereumTransferLazyCreate` | C1/D | regression reproduction; Ethereum-created hollow account; contract | `REMOVE_P07_11B` | The asserted follow-up is successful contract deployment, a retired path. |
| same | `LeakyCryptoTestsSuite.contractCallAfterEthereumTransferLazyCreate` | C1 K1/D | regression reproduction; Ethereum-created hollow account; contract/result | `REMOVE_P07_11B` | Setup and assertion both require successful contract execution. |
| same | `LeakyCryptoTestsSuite.autoAssociationWorksForContracts` | C1/D | subject-under-test; contract account auto-association; contract | `REMOVE_P07_11B` | The associated account is a contract; native auto-association has independent coverage. |
| `hip1261/CryptoTransferSimpleFeesTest.java` | `CryptoTransferUnassociatedAndAutoAccountCreationNegativeTests.cryptoTransferFTAutoAccountCreationWithFailingHookFailsOnHandle` | C1/D | subject-under-test; executable hook failure; contract/result | `REMOVE_P07_11B` | Failure and charging depend on hook execution. |
| same | `CryptoTransferWithHooksSimpleFeesPositiveTests.cryptoTransferFTAutoAccountCreationWithOneHookExtraHookFullCharging` | C1/D | subject-under-test; executable hook fee; contract/result | `REMOVE_P07_11B` | Hook execution is the measured extra. |
| same | `CryptoTransferWithHooksSimpleFeesPositiveTests.cryptoTransferFTWithOneHookExecutedTwiceExtraHookFullCharging` | C1/D | subject-under-test; executable hook fee; contract/result | `REMOVE_P07_11B` | Hook execution is the measured extra. |
| same | `CryptoTransferWithHooksSimpleFeesPositiveTests.cryptoTransferFTWithOneHookExtraHookFullCharging` | C1/D | subject-under-test; executable hook fee; contract/result | `REMOVE_P07_11B` | Hook execution is the measured extra. |
| same | `CryptoTransferWithHooksSimpleFeesPositiveTests.cryptoTransferHBARAndFtAndNFTWithTwoHooksExtraHooksAndAccountsFullCharging` | C1/D | subject-under-test; executable hook fee; contract/result | `REMOVE_P07_11B` | Hook execution is the measured extra. |
| same | `CryptoTransferWithHooksSimpleFeesPositiveTests.cryptoTransferHBARAndFtAndNFTWithTwoHooksExtraHooksAndTokensAndAccountsFullCharging` | C1/D | subject-under-test; executable hook fee; contract/result | `REMOVE_P07_11B` | Hook execution is the measured extra. |
| same | `CryptoTransferWithHooksSimpleFeesPositiveTests.cryptoTransferHBARAndFtWithTwoHooksExtraHookFullCharging` | C1/D | subject-under-test; executable hook fee; contract/result | `REMOVE_P07_11B` | Hook execution is the measured extra. |
| same | `CryptoTransferWithHooksSimpleFeesPositiveTests.cryptoTransferHBARAndNFtWithOneHookExtraHookFullCharging` | C1/D | subject-under-test; executable hook fee; contract/result | `REMOVE_P07_11B` | Hook execution is the measured extra. |
| same | `CryptoTransferWithHooksSimpleFeesPositiveTests.cryptoTransferHBARWithOneHookExtraHookFullCharging` | C1/D | subject-under-test; executable hook fee; contract/result | `REMOVE_P07_11B` | Hook execution is the measured extra. |
| same | `CryptoTransferWithHooksSimpleFeesPositiveTests.cryptoTransferNFTWithOneHookExtraHookFullCharging` | C1/D | subject-under-test; executable hook fee; contract/result | `REMOVE_P07_11B` | Hook execution is the measured extra. |
| `hip1261/CryptoTransferWithCustomFeesSimpleFeesTest.java` | `AutoAssociationsAndHooksTests.cryptoTransferHBARAndFtAndNFTWithTwoHooksExtraHooksAndTokensAndAccountsFullCharging` | C1/D | subject-under-test; executable hook/custom fee; contract/result | `REMOVE_P07_11B` | Native custom-fee coverage does not require the hook. |
| same | `SimpleFeesNegativeTests.cryptoTransferHBARAndFtAndNFTWithCustomFeesAndWithFailingHookFailsOnHandle` | C1/D | rejection or negative-path coverage; hook failure; contract/result | `REMOVE_P07_11B` | The negative status is caused solely by retired hook execution. |
| `hip1261/CryptoUpdateSimpleFeesTest.java` | `CryptoUpdatePositiveTestCases.cryptoUpdateWithOneHookFullFeesWithExtraCharged` | C1/D | subject-under-test; executable hook fee; contract/result | `REMOVE_P07_11B` | Hook creation/execution is the fee extra. |
| same | `CryptoUpdatePositiveTestCases.cryptoUpdateWithThresholdKeyAndExtraHooksSignaturesKeysAndHooksExtrasCharged` | C1/D | mixed ownership; native key fee plus hook fee; contract/result | `SPLIT_REQUIRED` | Retain the native threshold/signature case and remove hook setup and hook-specific expectation. |
| `hip1261/TokenAirdropSimpleFeesTest.java` | `TokenAirdropSimpleFeesTest.createAccountsAndKeys` | C1/H | reusable setup; native airdrop setup plus mutable contract; contract | `SPLIT_REQUIRED` | The helper is called by the suite's native tests; remove only the contract creation and contract-dependent cases. |
| `hip1261/TokenClaimAndCancelAirdropSimpleFeesTest.java` | `TokenClaimAndCancelAirdropSimpleFeesTest.createAccountsAndKeys` | C1/H | reusable setup; native claim/cancel setup plus mutable contract; contract | `SPLIT_REQUIRED` | The helper has 26 callers in this suite; native account/key setup must remain. |
| `hip904/TokenAirdropBase.java` | `TokenAirdropBase.deployMutableContract` | C1/H | reusable setup; contract fee collector/airdrop participant; contract | `REMOVE_P07_11B` | The helper only returns executable contract deployment operations; callers testing contract participants must be removed. |

## Topics and Coordination-related coverage

| Source | Class and method | Operations/form | Behavioral purpose and dependencies | Final disposition | Supporting evidence |
| --- | --- | --- | --- | --- | --- |
| `consensus/AtomicTopicCreateSuite.java` | `AtomicTopicCreateSuite.signingRequirementsEnforced` | C1/D | mixed ownership; native topic signature rules plus contract auto-renew account; contract | `SPLIT_REQUIRED` | Retain native payer/auto-renew signature cases; remove the contract-without-admin-key branch and its deployment. |
| `consensus/TopicCreateSuite.java` | `TopicCreateSuite.signingRequirementsEnforced` | C1/D | mixed ownership; native topic signature rules plus contract auto-renew account; contract | `SPLIT_REQUIRED` | Retain native signature cases; remove contract deployment and contract-account assertion. |
| `hip991/AtomicTopicCustomFeeCreateTest.java` | `AtomicTopicCustomFeeCreateTest.deployMutableContract` | C1/H | reusable setup; contract fee collector; contract | `REMOVE_P07_11B` | Sole caller is the contract-as-collector scenario; native collectors have separate cases. |
| `hip991/AtomicTopicCustomFeeUpdateTest.java` | `TopicCreatePositiveScenarios.updateToAddCustomFeeWithContractAsCollector` | C1/D | subject-under-test; contract custom-fee collector; contract | `REMOVE_P07_11B` | The tested collector type depends on retired contract creation. |
| `hip991/TopicCustomFeeCreateTest.java` | `TopicCustomFeeCreateTest.deployMutableContract` | C1/H | reusable setup; contract fee collector; contract | `REMOVE_P07_11B` | Sole caller is the contract-as-collector scenario. |
| `hip991/TopicCustomFeeUpdateTest.java` | `TopicCreatePositiveScenarios.updateToAddCustomFeeWithContractAsCollector` | C1/D | subject-under-test; contract custom-fee collector; contract | `REMOVE_P07_11B` | Native collector update cases remain. |

## Atomic batch

| Source | Class and method | Operations/form | Behavioral purpose and dependencies | Final disposition | Supporting evidence |
| --- | --- | --- | --- | --- | --- |
| `hip1261/AtomicBatchCrossServiceSimpleFeesTest.java` | `ComplexLogicScenarios.multipleInnerTxnAcrossAllServicesEachWithDifferentInnerPayerFullFeesCharged` | C1/D | mixed ownership; cross-service fee matrix includes contract creation; contract/result | `SPLIT_REQUIRED` | Retain native inner bodies and remove the contract inner body and its fee expectation. |
| `hip1261/AtomicBatchNegativeSimpleFeesTest.java` | `SmartContractAtomicBatchInnerFailures.contractCallInsufficientGasFailsOnPreHandle` | C1 K1/D | subject-under-test; contract gas failure; contract/result | `REMOVE_P07_11B` | Both setup and failure primitive are execution-specific. |
| same | `SmartContractAtomicBatchInnerFailures.ethereumTransactionWrongNonceFailsOnHandle` | C1 E1/D | subject-under-test; Ethereum nonce failure; contract/Ethereum/result | `REMOVE_P07_11B` | Dynamic Ethereum execution and nonce handling are retired. |
| `hip551/AtomicBatchInvalidSignaturesTests.java` | `AtomicBatchInvalidSignaturesTests.deletedTokenAssociationWithContract` | C1/D | subject-under-test; contract account association; contract | `REMOVE_P07_11B` | Native deleted-token association coverage is independent. |
| same | `ContractAssociationBatch.complexContractAssociationPatterns` | C3/D | subject-under-test; contract associations; contract | `REMOVE_P07_11B` | All created entities are contracts. |
| same | `ContractAssociationBatch.contractDeletionAndAssociationAttempt` | C1/D | subject-under-test; deleted contract association; contract | `REMOVE_P07_11B` | Failure relies on contract lifecycle. |
| same | `ContractAssociationBatch.fullBatchTokenContractAssociationWithoutAdminKey` | C1/D | subject-under-test; contract association authorization; contract | `REMOVE_P07_11B` | Contract key semantics are retired. |
| same | `ContractAssociationBatch.mixedContractAssociationScenarios` | C2/D | subject-under-test; contract associations; contract | `REMOVE_P07_11B` | Native association cases remain elsewhere. |
| `hip551/AtomicBatchNegativeTest.java` | `AtomicBatchNegativeTest.rollbackContractDelete` | C1/D | subject-under-test; rollback of contract deletion; contract | `REMOVE_P07_11B` | The affected entity and rollback are contract-specific. |
| same | `AtomicBatchNegativeTest.rollbackContractDeleteWithEvmAddress` | C1/D | subject-under-test; EVM-address contract rollback; contract | `REMOVE_P07_11B` | The entity exists only through retired contract creation. |
| same | `AtomicBatchNegativeTest.rollbackLogs` | C1 K1/D | assertion support; EVM logs under rollback; contract/result | `REMOVE_P07_11B` | Logs are generated only by executable calls. |
| same | `AtomicBatchNegativeTest.systemDeleteWillFail` | C1/D | rejection or negative-path coverage; contract system-delete; contract | `REMOVE_P07_11B` | Rejection is tied to a contract entity, not retained native behavior. |
| same | `AtomicBatchNegativeTest.systemUndeleteWillFail` | C1/D | rejection or negative-path coverage; contract system-undelete; contract | `REMOVE_P07_11B` | Rejection is tied to a contract entity. |
| same | `BatchConstraintsNegative.exceedsGasLimit` | C1 K1/D | subject-under-test; batch EVM gas limit; contract/result | `REMOVE_P07_11B` | Constraint is execution-gas specific. |
| same | `BatchConstraintsNegative.exceedsTxnSizeLimit` | C1 K1/D | mixed ownership; batch size limit triggered with contract bodies; contract | `SPLIT_REQUIRED` | Preserve the generic size-limit invariant with retained native bodies. |
| same | `NonceTests.nonceNotUpdatedWhenIntrinsicGasHandlerCheckFailed` | C1 E1/D | subject-under-test; Ethereum nonce/gas; contract/Ethereum | `REMOVE_P07_11B` | Ethereum execution ownership only. |
| same | `NonceTests.nonceUpdatedAfterEvmReversionDueContractLogic` | C1 E1/D | subject-under-test; EVM revert and nonce; contract/Ethereum/result | `REMOVE_P07_11B` | EVM nonce/reversion ownership only. |
| same | `NonceTests.nonceUpdatedAfterSuccessfulInternalCall` | C1 E1/D | subject-under-test; successful Ethereum/internal call; contract/Ethereum | `REMOVE_P07_11B` | Successful executable processing is retired. |
| same | `ThrottlesNegative.contractCallMoreThanTPSLimit` | C1 K2/D | subject-under-test; contract-call throttle; contract | `REMOVE_P07_11B` | Throttle bucket is contract-service specific. |
| same | `ThrottlesNegative.frontEndThrottleLeaksCapacity` | C2 K4/D | mixed ownership; batch throttle accounting using contract load; contract | `SPLIT_REQUIRED` | Preserve generic capacity accounting with native batch bodies. |
| same | `ThrottlesNegative.innerBatchGetsGasThrottledAndLeaksCapacity` | C2 K4/D | subject-under-test; EVM gas throttle; contract/result | `REMOVE_P07_11B` | Failure primitive and capacity metric are EVM-gas specific. |
| same | `ThrottlesNegative.notThrottleExemptIfTheBatchOperatorIsPrivileged` | C1 K2/D | mixed ownership; privilege/throttle rule using contract calls; contract | `SPLIT_REQUIRED` | Preserve privilege rule with retained native batch traffic. |
| same | `ThrottlesNegative.privilegedAccountsAreThrottleExempt` | C2 K4/D | mixed ownership; privilege/throttle rule using contract calls; contract | `SPLIT_REQUIRED` | Preserve exemption rule with retained native batch traffic. |

## Fees, utilization, and state creation

| Source | Class and method | Operations/form | Behavioral purpose and dependencies | Final disposition | Supporting evidence |
| --- | --- | --- | --- | --- | --- |
| `fees/CryptoSimpleFeesSuite.java` | `cryptoCreateWithSingleHook`; `cryptoCreateWithTwoHooks`; `cryptoCreateWithFiveHooks`; `cryptoCreateWithHooksAndKeys`; `cryptoUpdateWithSingleHook`; `cryptoUpdateWithMultipleHooks`; `cryptoUpdateWithHookDeletion`; `cryptoUpdateWithHookCreationAndDeletion`; `cryptoUpdateWithHookAndKey` | each C1/D | subject-under-test; hook creation/update charging; contract/result | `REMOVE_P07_11B` | Each method's fee delta is executable hook ownership; native create/update fee methods remain. |
| `fees/KitchenSinkFeeComparisonSuite.java` | `cryptoTransactions` | C2/F | dynamic-operation factory; native crypto matrix plus hook setup; contract | `SPLIT_REQUIRED` | Called by crypto-only and full runs; retain native operations and remove hook contract setup. |
| same | `scheduleTransactions` | C1/F | dynamic-operation factory; native schedules plus scheduled contract setup; contract | `SPLIT_REQUIRED` | Retain native schedule matrix and remove executable schedule segment. |
| same | `hookTransactions` | C3/F | dynamic-operation factory; executable hook fee matrix; contract/result | `REMOVE_P07_11B` | Called only by `runAllTransactions`; complete category is retired. |
| same | `contractTransactions` | C3/F | dynamic-operation factory; contract-service fee matrix; contract/result | `REMOVE_P07_11B` | Called only by `runAllTransactions`; complete category is retired. |
| `file/DiverseStateCreation.java` | `DiverseStateCreation.createDiverseState` | C2 K1/D | mixed ownership; native diverse file/state creation plus live contract state; contract/bytecode/query | `SPLIT_REQUIRED` | Retain file and metadata creation; remove contract deployment/call, contract deletion, bytecode query, and related serialization entries. |
| `hip993/SystemFileExportsTest.java` | `SystemFileExportsTest.syntheticFeeSchedulesUpdateHappensAtUpgradeBoundary` | C1 K1/D | mixed ownership; system-file upgrade behavior plus contract fee probe; contract/result | `SPLIT_REQUIRED` | Preserve system-file export/update assertions using retained native fee probes. |
| `integration/hip1259/Hip1259EnabledTests.java` | `variousTransactionTypesFeesGoToFeeCollector` | C1 K2/D | mixed ownership; fee-collector behavior across native and contract bodies; contract | `SPLIT_REQUIRED` | Retain native transaction types and remove contract entries. |
| same | `evmTransferToFeeCollectionAccountFails` | C1 K1/D | subject-under-test; EVM transfer restriction; contract/result | `REMOVE_P07_11B` | Originating pathway is executable EVM. |
| same | `selfDestructCannotSendFundsToFeeCollectionAccount` | C1 K1/D | subject-under-test; self-destruct restriction; contract/result | `REMOVE_P07_11B` | Self-destruct execution is retired. |
| `issues/Issue305Spec.java` | `Issue305Spec.congestionMultipliersRefreshOnPropertyUpdate` | C1 K2/D | mixed ownership; congestion configuration refresh using contract gas load; contract/result | `SPLIT_REQUIRED` | Preserve configuration refresh with native throttle/congestion traffic. |
| `issues/IssueRegressionTests.java` | `IssueRegressionTests.canSwitchSimpleFeesFromFalseToTrueWithoutException` | C2/D | mixed ownership; fee-mode switch smoke test using contract creates; contract | `SPLIT_REQUIRED` | Preserve toggle regression with native fee-bearing operations. |
| same | `IssueRegressionTests.transferAccountCannotBeDeletedForContractTarget` | C2/D | subject-under-test; contract transfer-account restriction; contract | `REMOVE_P07_11B` | Target entities are contracts and the rule is contract-specific. |
| `staking/StakingSuite.java` | `StakingSuite.stakingMetadataUpdateIsRewardOpportunity` | C1/D | mixed ownership; staking metadata/reward behavior with incidental contract account; contract | `SPLIT_REQUIRED` | Preserve staking invariant with a native account. |
| `throttling/SteadyStateThrottlingTest.java` | `SteadyStateThrottlingTest.competingClientFor` | C1 K1/F | dynamic-operation factory; selects load provider by transaction type; contract | `SPLIT_REQUIRED` | Called by `runWithConfig`; remove contract provider branches while retaining native providers. |
| same | `SteadyStateThrottlingTest.get` | C1/F | dynamic-operation factory; contract-create operation provider; contract | `REMOVE_P07_11B` | Factory branch produces only retired contract creation. |
| same | anonymous `suggestedInitializers` in contract-call provider | C1/F | reusable setup; initializes contract-call load provider; contract | `REMOVE_P07_11B` | Provider is selected only for contract-call steady-state load. |

## Files, queries, integration, and regression factories

| Source | Class and method | Operations/form | Behavioral purpose and dependencies | Final disposition | Supporting evidence |
| --- | --- | --- | --- | --- | --- |
| `file/FileUpdateSuite.java` | `allUnusedGasIsRefundedIfSoConfigured` | C1 K2/D | subject-under-test; EVM gas refund config; contract/result | `REMOVE_P07_11B` | Behavior is execution-gas specific. |
| same | `chainIdChangesDynamically` | C2 K4 E2/D | subject-under-test; live EVM chain ID; contract/Ethereum/result | `REMOVE_P07_11B` | All assertions require executable contract/Ethereum processing. |
| same | `entitiesNotCreatableAfterUsageLimitsReached` | C1/D | mixed ownership; entity limit uses contract entity as one case; contract | `SPLIT_REQUIRED` | Retain native entity-limit cases and remove contract creation. |
| same | `gasLimitOverMaxGasLimitFailsPrecheck` | C1 K1/D | rejection or negative-path coverage; contract gas precheck; contract | `DEFER_P07_11C` | This is an explicit legacy-body rejection boundary, not native setup. |
| same | `kvLimitsEnforced` | C1 K6/D | subject-under-test; mutable contract storage limits; contract/result | `REMOVE_P07_11B` | KV state mutation is retired execution behavior. |
| same | `maxRefundIsEnforced` | C1 K2/D | subject-under-test; EVM gas refund; contract/result | `REMOVE_P07_11B` | Behavior is execution-gas specific. |
| same | `serviceFeeRefundedIfConsGasExhausted` | C1 K3/D | subject-under-test; EVM gas/service-fee refund; contract/result | `REMOVE_P07_11B` | Behavior is execution-gas specific. |
| `integration/RepeatableHip1215Tests.java` | `RepeatableHip1215Tests.hasCapacityUntilFullyScheduled` | K1/D | subject-under-test; scheduled contract-call capacity; contract | `REMOVE_P07_11B` | Load body is retired execution; native schedule capacity has separate ownership. |
| `integration/RepeatableIntegrationTests.java` | `addEcdsaSigViaContractAndExpect`; `addEd25519SigViaContractAndExpect` | each K1/D | subject-under-test; contract-mediated signature addition; contract/result | `REMOVE_P07_11B` | Both methods invoke a contract to create the asserted behavior. |
| `issues/Issue305Spec.java` | covered above | — | — | — | — |
| `misc/InvalidgRPCValuesTest.java` | `InvalidgRPCValuesTest.transactionsWithOnlySigMap` | C1/D | mixed ownership; malformed native and contract transaction-body matrix; contract | `SPLIT_REQUIRED` | Retain malformed native vectors; remove contract-create vector or move an intentional legacy rejection to P07-11C. |
| `queries/AsNodeOperatorQueriesTestEmbedded.java` | `getContractBytecodeQueryNoSigRequired`; `getSmartContractQuerySigNotRequired` | each C1/D | executable-query ownership; contract setup/query | `DEFER_P07_11C` | Both create a contract solely to exercise retained/retired contract query boundaries. |
| `queries/DisabledNodeOperatorTest.java` | `nodeOperatorQueryPortNotAccessibleForContractBytecode` | C2/D | executable-query ownership; contract bytecode query | `DEFER_P07_11C` | Query boundary and its setup belong to P07-11C. |
| same | `nodeOperatorQueryPortNotAccessibleForContractCall` | C2 K2 L2/D | executable-query ownership; local contract simulation | `DEFER_P07_11C` | Contains the explicit executable local-query boundary. |
| `queries/RepeatableOperatorQueryTests.java` | `getSmartContractQueryNodeOperatorNotCharged` | C1/D | executable-query ownership; contract query charging | `DEFER_P07_11C` | Contract query retirement is assigned to P07-11C. |
| `regression/factories/AccountCompletionFuzzingFactory.java` | `AccountCompletionFuzzingFactory.initOperations` | C1/F | dynamic-operation factory; native hollow completion pool plus contract operation | `SPLIT_REQUIRED` | Called by `HollowAccountCompletionFuzzing`; remove the contract operation and retain native fuzz inputs. |
| `regression/factories/HollowAccountCompletedFuzzingFactory.java` | `HollowAccountCompletedFuzzingFactory.initOperations` | C1/F | dynamic-operation factory; completed-hollow-account operation pool | `SPLIT_REQUIRED` | Called by `CompletedHollowAccountOperationsFuzzing`; remove contract operation only. |
| `regression/factories/IdFuzzingProviderFactory.java` | `initOpHbarTransfer`; `initOpFungibleTransfer`; `initOpERC20Transfer`; `initOpERC721Transfer` | each C1/F | dynamic-operation factory; contract/precompile transfer setup | `REMOVE_P07_11B` | All four feed `initOperations`, which feeds `AtomicBatchFuzzing` and `AddressAliasIdFuzzing`; replace/remove executable provider entries while retaining native fuzz providers. |
| `SystemFileExportsTest.java` | covered above | — | — | — | — |

## Reconciliation

The syntax-tree census outside the two shared P07-11C owners is:

| Operation | Direct/helper/factory occurrences |
| --- | ---: |
| `contractCreate` | 105 |
| `contractCall` | 47 |
| `ethereumCall` | 6 |
| executable local query | 9 |

The excluded shared owners contribute:

| Owner | Create | Call | Ethereum | Local |
| --- | ---: | ---: | ---: | ---: |
| `ContractUpdateSuite` | 20 | 7 | 0 | 1 |
| `ContractRecordsSanityCheckSuite` | 4 | 1 | 0 | 0 |

Thus the authoritative 61-file totals reconcile exactly:

- `contractCreate`: 105 + 24 = **129**;
- `contractCall`: 47 + 8 = **55**;
- `ethereumCall`: 6 + 0 = **6**;
- executable local queries: 9 + 1 = **10**.

There are 97 enclosing methods/factories in 37 files outside the shared owners.
There are no `UNRESOLVED` dispositions.

## Subsequent bounded deletion scope

The next P07-11B implementation pass may:

1. delete every method or helper marked `REMOVE_P07_11B`;
2. remove only the executable segment of every method/factory marked
   `SPLIT_REQUIRED`, preserving the documented native invariant and caller
   chain;
3. leave every `DEFER_P07_11C` method unchanged;
4. leave `ContractUpdateSuite`, `ContractRecordsSanityCheckSuite`, and core
   HAPI operation/query classes unchanged.

Whole-file deletion is safe only after reverse-consumer verification for a
file whose remaining methods are all `REMOVE_P07_11B`. Mixed files and dynamic
factories require method- or branch-level edits.

### Exact file boundary

The following files are safe for deletion of the classified executable
methods/helpers without first splitting a retained enclosing method:

- `crypto/CryptoTransferSuite.java`;
- `crypto/LeakyCryptoTestsSuite.java`;
- `hip1261/CryptoTransferSimpleFeesTest.java`;
- `hip1261/CryptoTransferWithCustomFeesSimpleFeesTest.java`;
- `hip1261/TokenAirdropSimpleFeesTest.java` (helper branch only);
- `hip1261/TokenClaimAndCancelAirdropSimpleFeesTest.java` (helper branch only);
- `hip904/TokenAirdropBase.java` (the zero-caller deployment helper only);
- `hip991/AtomicTopicCustomFeeCreateTest.java`;
- `hip991/AtomicTopicCustomFeeUpdateTest.java`;
- `hip991/TopicCustomFeeCreateTest.java`;
- `hip991/TopicCustomFeeUpdateTest.java`;
- `hip1261/AtomicBatchNegativeSimpleFeesTest.java`;
- `hip551/AtomicBatchInvalidSignaturesTests.java`;
- `integration/RepeatableHip1215Tests.java`;
- `integration/RepeatableIntegrationTests.java`;
- the execution-only methods of `integration/hip1259/Hip1259EnabledTests.java`;
- `issues/IssueRegressionTests.java` method
  `transferAccountCannotBeDeletedForContractTarget`.

The following files are safe to edit only as bounded splits; their retained
native method/factory behavior must remain:

- `consensus/AtomicTopicCreateSuite.java`;
- `consensus/TopicCreateSuite.java`;
- `hip1261/CryptoUpdateSimpleFeesTest.java`;
- `hip1261/AtomicBatchCrossServiceSimpleFeesTest.java`;
- `hip551/AtomicBatchNegativeTest.java`;
- `fees/KitchenSinkFeeComparisonSuite.java`;
- `file/DiverseStateCreation.java`;
- `hip993/SystemFileExportsTest.java`;
- `integration/hip1259/Hip1259EnabledTests.java`;
- `issues/Issue305Spec.java`;
- `issues/IssueRegressionTests.java`;
- `misc/InvalidgRPCValuesTest.java`;
- `regression/factories/AccountCompletionFuzzingFactory.java`;
- `regression/factories/HollowAccountCompletedFuzzingFactory.java`;
- `regression/factories/IdFuzzingProviderFactory.java`;
- `staking/StakingSuite.java`;
- `throttling/SteadyStateThrottlingTest.java`;
- `file/FileUpdateSuite.java` method
  `entitiesNotCreatableAfterUsageLimitsReached`.

The following files are not in the P07-11B deletion scope and must remain
unchanged for P07-11C:

- `queries/AsNodeOperatorQueriesTestEmbedded.java`;
- `queries/DisabledNodeOperatorTest.java`;
- `queries/RepeatableOperatorQueryTests.java`;
- `file/FileUpdateSuite.java` method
  `gasLimitOverMaxGasLimitFailsPrecheck`;
- `contract/hapi/ContractUpdateSuite.java`;
- `contract/records/ContractRecordsSanityCheckSuite.java`.

No file in this classification is approved for wholesale deletion merely
because every executable occurrence in it is removable: each file can also
contain native methods that have no executable occurrence. Whole-file deletion
still requires a fresh reverse-consumer and retained-test registration check.
