# P07-11B Method-Level Classification

## Authority

This documentation-only classification is based on
`e52102bbd1fd701eae70c10f0d1888c29fcad9b7`. It excludes the two shared
P07-11C owners, `ContractUpdateSuite` and `ContractRecordsSanityCheckSuite`.
No production or test source was changed during this pass.

The inventory was repaired at
`0e767eab6a20b787c2c39b862f333330b410dbcd` by deriving the complete
operation taxonomy from `TxnVerbs` and `QueryVerbs`, then scanning the Java
syntax tree. Each operation is assigned to its enclosing method or factory.
Helper-generated operations are traced to their callers. The wrapper-aware
taxonomy and reconciliation below supersede the earlier literal-name counts.

At `e5742461345b2b425c2d5e6ed78a2ecd5e12ed51`, the same syntax-tree
inventory was audited in the reverse direction from every construction site
through providers, intermediate callers, and registration roots. The
ownership-closure audit below is authoritative for enclosing ownership. It
does not add executable constructions to the operation census.

Notation:

- operations: `C` = `contractCreate`, `K` = `contractCall`, `E` =
  `ethereumCall`, and `L` = executable `contractCallLocal`;
- form: `D` = direct, `H` = helper-generated, `F` = dynamic factory;
- dependencies: `contract` includes bytecode/ABI/deployment verbs, `result`
  includes live contract result, log, action, gas, sidecar, or child-record
  assertions.

## Complete operation taxonomy

| Aggregate family | Concrete factories and logical wrappers |
| --- | --- |
| `contractCreate` | `contractCreate`, `createDefaultContract`, `contractCustomCreate`; zero scoped `explicitContractCreate` occurrences |
| `contractCall` | `contractCall`, `contractCallWithFunctionAbi`, `contractCallWithTuple`, and the logical `contractCallWithSendRecordSanityChecks` wrapper; zero scoped `contractCallFrom` or `explicitContractCall` occurrences |
| `ethereumCall` | `ethereumCall`, `ethereumCryptoTransfer`, `ethereumCryptoTransferToAlias`; zero scoped `ethereumCallWithFunctionAbi`, `ethereumCryptoTransferToExplicit`, `ethereumCryptoTransferToAddress`, `ethereumContractCreate`, or `explicitEthereumTransaction` occurrences |
| executable local query | `contractCallLocal`, `contractCallLocalWithFunctionAbi`; zero scoped `explicitContractCallLocal` or `contractCallLocalFrom` occurrences |

`contractCallWithSendRecordSanityChecks` is a suite-local logical wrapper: its
single internal `contractCall` construction is reported under that subtype
instead of being counted twice. No scoped source directly constructs
`HapiContractCreate`, `HapiContractCall`, `HapiEthereumCall`,
`HapiEthereumContractCreate`, or `HapiContractCallLocal` with `new`.

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
| same | `AtomicTopicCreateSuite.topicCreateWithContractWithAdminKeyForAutoRenewAccount` | `createDefaultContract` 1/D | subject-under-test; contract auto-renew account | `REMOVE_P07_11B` | The auto-renew account is created only as a contract; native account coverage is separate. |
| same | `AtomicTopicCreateSuite.topicCreateWithContractWithoutAdminKeyForAutoRenewAccountFails` | `createDefaultContract` 1/D | rejection or negative-path coverage; contract auto-renew account | `REMOVE_P07_11B` | The rejected signer is specifically a contract account. |
| `consensus/TopicCreateSuite.java` | `TopicCreateSuite.signingRequirementsEnforced` | C1/D | mixed ownership; native topic signature rules plus contract auto-renew account; contract | `SPLIT_REQUIRED` | Retain native signature cases; remove contract deployment and contract-account assertion. |
| same | `TopicCreateSuite.topicCreateWithContractWithAdminKeyForAutoRenewAccount` | `createDefaultContract` 1/D | subject-under-test; contract auto-renew account | `REMOVE_P07_11B` | The auto-renew account is created only as a contract. |
| same | `TopicCreateSuite.topicCreateWithContractWithoutAdminKeyForAutoRenewAccountFails` | `createDefaultContract` 1/D | rejection or negative-path coverage; contract auto-renew account | `REMOVE_P07_11B` | The rejected signer is specifically a contract account. |
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
| same | `scheduleTransactions` | C1 plus `contractCallWithFunctionAbi` 2/F | dynamic-operation factory; native schedules plus scheduled contract setup; contract | `SPLIT_REQUIRED` | Retain native schedule matrix and remove both executable schedule-call variants and their setup. |
| same | `hookTransactions` | C3/F | dynamic-operation factory; executable hook fee matrix; contract/result | `REMOVE_P07_11B` | Called only by `runAllTransactions`; complete category is retired. |
| same | `contractTransactions` | C3, `contractCallWithFunctionAbi` 3, `contractCallLocalWithFunctionAbi` 1/F | dynamic-operation factory; contract-service fee and local-query matrix; contract/result | `REMOVE_P07_11B` | Called only by `runAllTransactions`; complete category is retired. |
| same | `ethereumTransactions` | `ethereumCryptoTransfer` 1/F | dynamic-operation factory; Ethereum transaction fee matrix | `REMOVE_P07_11B` | Called only by `runAllTransactions`; its complete category requires retired Ethereum execution. |
| `file/DiverseStateCreation.java` | `DiverseStateCreation.createDiverseState` | C2 K1/D | mixed ownership; native diverse file/state creation plus live contract state; contract/bytecode/query | `SPLIT_REQUIRED` | Retain file and metadata creation; remove contract deployment/call, contract deletion, bytecode query, and related serialization entries. |
| `hip993/SystemFileExportsTest.java` | `SystemFileExportsTest.syntheticFeeSchedulesUpdateHappensAtUpgradeBoundary` | C1 K1/D | mixed ownership; system-file upgrade behavior plus contract fee probe; contract/result | `SPLIT_REQUIRED` | Preserve system-file export/update assertions using retained native fee probes. |
| `integration/hip1259/Hip1259EnabledTests.java` | `variousTransactionTypesFeesGoToFeeCollector` | C1 K2/D | mixed ownership; fee-collector behavior across native and contract bodies; contract | `SPLIT_REQUIRED` | Retain native transaction types and remove contract entries. |
| same | `evmTransferToFeeCollectionAccountFails` | C1 K1/D | subject-under-test; EVM transfer restriction; contract/result | `REMOVE_P07_11B` | Originating pathway is executable EVM. |
| same | `selfDestructCannotSendFundsToFeeCollectionAccount` | C1 K1/D | subject-under-test; self-destruct restriction; contract/result | `REMOVE_P07_11B` | Self-destruct execution is retired. |
| `issues/Issue305Spec.java` | `Issue305Spec.congestionMultipliersRefreshOnPropertyUpdate` | C1 K2/D | mixed ownership; congestion configuration refresh using contract gas load; contract/result | `SPLIT_REQUIRED` | Preserve configuration refresh with native throttle/congestion traffic. |
| `issues/IssueRegressionTests.java` | `IssueRegressionTests.canSwitchSimpleFeesFromFalseToTrueWithoutException` | C2/D | mixed ownership; fee-mode switch smoke test using contract creates; contract | `SPLIT_REQUIRED` | Preserve toggle regression with native fee-bearing operations. |
| same | `IssueRegressionTests.transferAccountCannotBeDeletedForContractTarget` | C2/D | subject-under-test; contract transfer-account restriction; contract | `REMOVE_P07_11B` | Target entities are contracts and the rule is contract-specific. |
| `staking/StakingSuite.java` | `StakingSuite.stakingMetadataUpdateIsRewardOpportunity` | C1/D | mixed ownership; staking metadata/reward behavior with incidental contract account; contract | `SPLIT_REQUIRED` | Preserve staking invariant with a native account. |
| `throttling/SteadyStateThrottlingTest.java` | `SteadyStateThrottlingTest.competingClientFor` | C1 K1/F | dynamic-operation factory; selects load provider by transaction type; contract | `SPLIT_REQUIRED` | Called by `checkCustomNetworkTps`; remove its `ContractCalls` branch while retaining the native default branch. |
| same | anonymous `get` in `scCallOps` | K1/F | anonymous operation implementation; contract-call load operation | `REMOVE_P07_11B` | Reachable only from the removable `checkContractCallsTps` registration through `scCallOps`. |
| same | anonymous `suggestedInitializers` in `scCallOps` | C1/F | anonymous operation implementation; initializes contract-call load provider; contract | `REMOVE_P07_11B` | Reachable only from the removable `checkContractCallsTps` registration through `scCallOps`. |

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
| `integration/RepeatableIntegrationTests.java` | `signSystemContractAppendsFromAddressSignature` | `contractCallWithFunctionAbi` 1/D | subject-under-test; successful system-contract schedule signing | `REMOVE_P07_11B` | The method directly invokes executable schedule-signing behavior; its native schedule setup does not provide an independent assertion after the call is removed. |
| same | `unrelatedCallToSignSystemContractIsNoop` | `contractCallWithFunctionAbi` 1/D | rejection or negative-path coverage; unrelated executable system-contract call | `REMOVE_P07_11B` | The asserted no-op is caused solely by an executable ABI transaction call. |
| same | `signScheduleRevertsOnUnrelatedEd25519SigAndAppendsRelatedEcdsaSig` | K2/H | subject-under-test; helper-generated contract schedule signing | `REMOVE_P07_11B` | Calls `addEd25519SigViaContractAndExpect` and `addEcdsaSigViaContractAndExpect` once each; both assertions require executable calls. |
| same | `signScheduleRevertsOnUnrelatedEcdsaSigAndAppendsRelatedEd25519Sig` | K2/H | subject-under-test; helper-generated contract schedule signing | `REMOVE_P07_11B` | Calls the same two helpers once each in reverse order; both assertions require executable calls. |
| same | `addEd25519SigViaContractAndExpect` | `contractCall` 1/H | assertion support; constructs ED25519 signature map and executable call | `REMOVE_P07_11B` | Exactly two callers, both approved for removal above; no retained or deferred caller exists. |
| same | `addEcdsaSigViaContractAndExpect` | `contractCall` 1/H | assertion support; native signing feeds executable contract call | `REMOVE_P07_11B` | Exactly two callers, both approved for removal above; `NativeEcdsaSigning` ownership outside this helper is unaffected. |
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
| `token/TokenAssociationSpecs.java` | `associatedContractsMustHaveAdminKeys`; `contractInfoQueriesAsExpected` | each `createDefaultContract` 1/D | subject-under-test; contract association and contract-info behavior | `REMOVE_P07_11B` | Both methods require a successfully created contract account; native association coverage is independent. |
| `token/batch/AtomicTokenAssociationSpecs.java` | `associatedContractsMustHaveAdminKeys`; `contractInfoQueriesAsExpected` | each `createDefaultContract` 1/D | subject-under-test; batched contract association and contract-info behavior | `REMOVE_P07_11B` | Both methods require a successfully created contract account; native atomic association coverage remains. |

## Complete ownership-closure audit

### Closure model

An executable construction remains counted exactly once in the operation
census. Enclosing ownership is recorded separately with these structural
roles:

- **registered executable test** or **suite registration root**: the nearest
  JUnit/HAPI registration or externally reachable suite entry;
- **provider factory**: a method that assembles, returns, or selects an
  operation provider, operation array, or dynamic operation closure;
- **intermediate delegator**: a caller between a registration root and a
  provider;
- **helper**: reusable setup or assertion support containing a construction;
- **branch owner**: a conditional branch selecting an executable provider;
- **anonymous operation implementation**: an overridden supplier/provider
  method containing the concrete construction.

The audit followed each of the 225 constructions to its nearest registered or
externally reachable root. The 110 construction-bearing registered methods
already listed in the method tables are self-rooted. The additional indirect
roots and factories are enumerated below.

### `SteadyStateThrottlingTest` closure

| Ownership node | Structural role | Parent or callers | Downstream executable ownership | Behavioral purpose | Final disposition | Implementation consequence |
| --- | --- | --- | --- | --- | --- | --- |
| `checkContractCallsTps` | registered executable test | HAPI registration | `checkTps` and `scCallOps` | contract-call steady-state throughput | `REMOVE_P07_11B` | Delete the complete registered test. |
| `checkTps` | intermediate delegator; retained registration support | four TPS registrations | `checkCustomNetworkTps` | common native and contract throughput harness | `RETAIN_NATIVE` | Preserve; its three native registered callers remain. |
| `checkCustomNetworkTps` | intermediate delegator; provider invoker | `checkTps` | supplied provider and `competingClientFor` | common load execution and TPS assertion | `RETAIN_NATIVE` | Preserve the generic native harness. |
| `scCallOps` | provider factory | only `checkContractCallsTps` | anonymous `suggestedInitializers` and `get` | constructs the contract-call load provider | `REMOVE_P07_11B` | Delete after its sole registered caller is removed. |
| `scCallOps.suggestedInitializers` | anonymous operation implementation; executable construction site | contained by `scCallOps` | `contractCreate` 1 | deploys the contract used by load | `REMOVE_P07_11B` | Removed with `scCallOps`. |
| `scCallOps.get` | operation supplier; anonymous operation implementation; executable construction site | contained by `scCallOps` | ordinary `contractCall` 1 | supplies successful contract calls | `REMOVE_P07_11B` | Removed with `scCallOps`. |
| `competingClientFor` | provider factory; branch owner | `checkCustomNetworkTps` | native default branch and `ContractCalls` branch | chooses competing load by transaction family | `SPLIT_REQUIRED` | Preserve the native default branch. |
| `competingClientFor.ContractCalls` | branch owner; executable construction site | selected only for `"ContractCalls"` | `contractCreate` 1 and ordinary `contractCall` 1 | contract-specific competing load | `REMOVE_P07_11B` | Surgically remove the branch when its only root is deleted. |

The complete executable path is:

```text
checkContractCallsTps
├─ scCallOps
│  ├─ suggestedInitializers → contractCreate
│  └─ get → contractCall
└─ checkTps
   └─ checkCustomNetworkTps
      ├─ applies scCallOps
      └─ competingClientFor
         └─ ContractCalls branch → contractCreate + contractCall
```

There is no retained or deferred caller of `scCallOps`. In contrast,
`checkTps`, `checkCustomNetworkTps`, and the default branch of
`competingClientFor` support the retained `checkXfersTps`,
`checkFungibleMintsTps`, and `checkCryptoCreatesTps` registrations.

### Other indirect ownership closures

| Closure | Registered or external roots | Providers, helpers, and caller path | Root disposition | Provider/helper disposition |
| --- | ---: | --- | --- | --- |
| Kitchen Sink fee comparison | `kitchenSinkFeeComparisonCrypto`, `kitchenSinkFeeComparisonFull` (2) | `runCryptoTransactions → cryptoTransactions`; `runAllTransactions → cryptoTransactions, scheduleTransactions, hookTransactions, contractTransactions, ethereumTransactions` | both `SPLIT_REQUIRED` | `runCryptoTransactions` `RETAIN_NATIVE`; `runAllTransactions` `SPLIT_REQUIRED`; category factories retain their method-table dispositions |
| `DiverseStateCreation` | `getSpecsInSuite` (1 suite registration root) | `getSpecsInSuite → createDiverseState` | `SPLIT_REQUIRED` | `createDiverseState` `SPLIT_REQUIRED` |
| Token airdrop fee setup | 35 registered methods | each root calls `TokenAirdropSimpleFeesTest.createAccountsAndKeys` | all `RETAIN_NATIVE` | helper `SPLIT_REQUIRED`; remove only its contract setup |
| Token claim/cancel fee setup | 26 registered methods | each root calls `TokenClaimAndCancelAirdropSimpleFeesTest.createAccountsAndKeys` | all `RETAIN_NATIVE` | helper `SPLIT_REQUIRED`; remove only its contract setup |
| Topic contract collectors | `AtomicTopicCustomFeeCreateTest.TopicCreatePositiveScenarios.topicWithContractCollector`, `TopicCustomFeeCreateTest.TopicCreatePositiveScenarios.topicWithContractCollector` (2) | each root calls its suite's `deployMutableContract` | both `REMOVE_P07_11B` | both helpers `REMOVE_P07_11B` |
| Repeatable schedule signatures | the two `signScheduleRevertsOnUnrelated…` registrations (2) | each root calls both `addEd25519SigViaContractAndExpect` and `addEcdsaSigViaContractAndExpect` | both `REMOVE_P07_11B` | both helpers `REMOVE_P07_11B`; four invocation edges |
| Account-completion fuzzing | `HollowAccountCompletionFuzzing.hollowAccountCompletionFuzzing` (1) | external root → `AccountCompletionFuzzingFactory.initOperations` | `SPLIT_REQUIRED` | factory `SPLIT_REQUIRED` |
| Completed-hollow fuzzing | `CompletedHollowAccountOperationsFuzzing.completedHollowAccountOperationsFuzzing` (1) | external root → `HollowAccountCompletedFuzzingFactory.initOperations` | `SPLIT_REQUIRED` | factory `SPLIT_REQUIRED` |
| ID fuzzing | `AtomicBatchFuzzing.atomicMixedOperations`, `AddressAliasIdFuzzing.addressAliasIdFuzzing` (2) | roots → `IdFuzzingProviderFactory.initOperations` → four `initOp…` factories | both `SPLIT_REQUIRED` | aggregator `SPLIT_REQUIRED`; four executable initializer factories `REMOVE_P07_11B` |
| Steady-state contract load | `checkContractCallsTps` (1) | complete path documented above | `REMOVE_P07_11B` | `scCallOps` `REMOVE_P07_11B`; `competingClientFor` `SPLIT_REQUIRED`; shared delegators retained |

The 35 `TokenAirdropSimpleFeesTest` roots are:
`multipleTokenAirdropFTToReceiverWithoutFreeAutoAssociationsAggregateAirdropFullCharging`,
`tokenAirdropAutoCreateAccountWithFTMovingToED25519AliasFullCharging`,
`tokenAirdropAutoCreateAccountWithNFTMovingToECDSAAliasFullCharging`,
`tokenAirdropAutoCreateHollowAccountWithFTMovingResultingInPendingAirdropAndFullCharging`,
`tokenAirdropFTAndNFTToAssociatedReceiverBaseFeesFullCharging`,
`tokenAirdropFTAndNFTToAssociatedReceiverWithInsufficientPayerBalanceFailsOnIngest`,
`tokenAirdropFTAndNFTToAssociatedReceiverWithInsufficientPayerBalanceFailsOnPreHandle`,
`tokenAirdropFTAndNFTToAssociatedReceiverWithInsufficientTxnFeeFailsOnIngest`,
`tokenAirdropFTAndNFTToAssociatedReceiverWithInsufficientTxnFeeFailsOnPreHandle`,
`tokenAirdropFTAndNFTToMultipleAssociatedReceiversExtrasFeesFullCharging`,
`tokenAirdropFTToAssociatedReceiverBaseFeesFullCharging`,
`tokenAirdropFTToAssociatedReceiverFromSenderWithInvalidSignatureFailsOnIngest`,
`tokenAirdropFTToAssociatedReceiverFromSenderWithInvalidSignatureFailsOnPreHandle`,
`tokenAirdropFTToAssociatedReceiverFromSenderWithThresholdKeyFullCharging`,
`tokenAirdropFTToAssociatedReceiverSigRequiredFullCharging`,
`tokenAirdropFTToMultipleAssociatedReceiversExtrasFeesFullCharging`,
`tokenAirdropNFTToAssociatedReceiverBaseFeesFullCharging`,
`tokenAirdropNFTToMultipleAssociatedReceiversExtrasFeesFullCharging`,
`tokenAirdropReceiverAssociatedWithAndWithoutFreeAutoAssociationsResultsInPendingAndSuccessfulAirdropsFullCharging`,
`tokenAirdropReceiverFreeAutoAssociationsBaseFeesFullCharging`,
`tokenAirdropReceiverNoFreeAutoAssociationsExtraAirdropFullCharging`,
`tokenAirdropReceiverNoFreeAutoAssociationsMultiplePendingAirdropsFullCharging`,
`tokenAirdropReceiverNoFreeAutoAssociationsResultsInPendingBaseFeesFullCharging`,
`tokenAirdropReceiverWithAndWithoutFreeAutoAssociationsResultsInPendingAndSuccessfulAirdropsFullCharging`,
`tokenAirdropReceiverWithExhaustedFreeAutoAssociationsResultsInFaildTxnAndFullCharging`,
`tokenAirdropWithAccountFrozenForTokenFailsOnHandleAndFeesFullCharging`,
`tokenAirdropWithAllowanceIsNotSupportedFailsOnIngest`,
`tokenAirdropWithDuplicateNFTSerialFailsOnIngest`,
`tokenAirdropWithDuplicatePendingAirdropFailsOnHandleAndFeesFullCharging`,
`tokenAirdropWithEmptyTokenTransferBodyFailsOnIngest`,
`tokenAirdropWithInsufficientTokenBalanceFailsOnHandleAndFeesFullCharging`,
`tokenAirdropWithInvalidNFTSerialFailsOnHandleAndFeesFullCharging`,
`tokenAirdropWithMultipleSendersForATokenFailsOnIngest`,
`tokenAirdropWithPausedTokenFailsOnHandleAndFeesFullCharging`, and
`tokenAirdropWithSenderNotAssociatedToTokenFailsOnHandleAndFeesFullCharging`.

The 26 `TokenClaimAndCancelAirdropSimpleFeesTest` roots are:
`tokenAirdropCancelPendingAirdropToHollowAccountWithFTMovingFullFeesCharging`,
`tokenAirdropClaimPendingAirdropToHollowAccountWithFTMovingFullFeesCharging`,
`tokenCancelAirdropWithExtraSignaturesFullCharging`,
`tokenCancelClaimedFTAirdropFailsOnHandle`,
`tokenCancelFTAirdropFullFeesCharging`,
`tokenCancelFTAirdropThatIsAlreadyCanceledFailsOnHandle`,
`tokenCancelMultipleFTAirdropBaseFeesFullCharging`,
`tokenCancelMultiplePendingFTAndNFTAirdropsFeesFullCharging`,
`tokenCancelMultiplePendingNFTAirdropsFeesFullCharging`,
`tokenCancelNonExistingFTAirdropFailsOnHandle`,
`tokenCancelPendingNFTAirdropFeesFullCharging`,
`tokenClaimAirdropBaseFeesFullCharging`,
`tokenClaimAirdropWithExtraSignaturesFullCharging`,
`tokenClaimAirdropWithMissingReceiverSignatureFailsOnHandle`,
`tokenClaimAirdropWithMissingSenderSignatureFailsOnIngest`,
`tokenClaimFTAirdropThatIsAlreadyCanceledFailsOnHandle`,
`tokenClaimFTAirdropThatIsAlreadyClaimedFailsOnHandle`,
`tokenClaimMultiplePendingFTAirdropsFeesFullCharging`,
`tokenClaimMultiplePendingFTAndNFTAirdropsFeesFullCharging`,
`tokenClaimMultiplePendingNFTAirdropsFeesFullCharging`,
`tokenClaimNFTMultiplePendingAirdropsForTheSameSerialAllClaimsAfterFirstOneFailOnHandle`,
`tokenClaimNFTPendingAirdropWithWrongSerialFailsOnHandle`,
`tokenClaimNonExistingFTAirdropFailsOnHandle`,
`tokenClaimPendingAirdropForHollowAccountWithoutSignatureFailsOnHandle`,
`tokenClaimPendingNFTAirdropFeesFullCharging`, and
`tokenClaimPendingNFTAirdropWhenSenderNoLongerOwnsTheTokenFailsOnHandle`.

### Ownership-node reconciliation

| Ownership level | Count | `REMOVE_P07_11B` | `SPLIT_REQUIRED` | `RETAIN_NATIVE` | `DEFER_P07_11C` | `UNRESOLVED` |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| registered or externally reachable roots | **183** | 70 | 22 | 61 | 30 | 0 |
| provider/factory owners | **19** | 10 | 8 | 1 | 0 | 0 |
| intermediate caller nodes | **2** | 0 | 0 | 2 | 0 | 0 |
| construction-bearing helpers | **7** | 5 | 2 | 0 | 0 | 0 |
| explicit conditional branch owners | **1** | 1 | 0 | 0 | 0 | 0 |

The registered-root total comprises 110 construction-bearing roots already
present in the method census and 73 indirect roots added by this closure
audit. The provider/factory total comprises 15 construction-bearing
providers, plus `runCryptoTransactions`, `runAllTransactions`,
`IdFuzzingProviderFactory.initOperations`, and `scCallOps`. The two
intermediate callers are `checkTps` and `checkCustomNetworkTps`.

There are **92** distinct caller, registration, provider-application, or
containment edges in the indirect closure graph. The 225
owner-to-construction links are reported separately by the operation census
and are not included in the 92. This pass added 80 previously absent explicit
ownership nodes: 73 indirect roots, four outer provider/factory owners, two
intermediate delegators, and the `competingClientFor.ContractCalls` branch
owner.

No helper or provider marked `REMOVE_P07_11B` has a retained or deferred
consumer. Mixed provider closures are `SPLIT_REQUIRED`; common delegators
with native callers are `RETAIN_NATIVE`. The ownership-node `UNRESOLVED`
count is **0**.

## Operation reconciliation

### Protected P07-11C enumeration

`ContractUpdateSuite` remains behaviorally unchanged. Its 19 enclosing
methods contain 20 ordinary `contractCreate` operations, one
`contractCustomCreate`, seven ordinary `contractCall` operations, and one
ordinary `contractCallLocal`. Every method remains `DEFER_P07_11C`.
The enclosing methods are:

- `updateMaxAutomaticAssociationsAndRequireKey`;
- `idVariantsTreatedAsExpected`;
- `updateStakingFieldsWorks`;
- `eip1014AddressAlwaysHasPriority`;
- `updateWithBothMemoSettersWorks`;
- `updatingExpiryWorks`;
- `rejectsExpiryTooFarInTheFuture`;
- `updateAutoRenewWorks`;
- `updateAutoRenewAccountWorks`;
- `updateAdminKeyWorks`;
- `immutableContractKeyFormIsStandard`;
- `canMakeContractImmutableWithEmptyKeyList`;
- `givenAdminKeyMustBeValid`;
- `fridayThe13thSpec`;
- `updateDoesNotChangeBytecode`;
- `tryContractUpdateWithMaxAutoAssociations`;
- `playGame`;
- `cannotUpdateImmutableContractExceptExpiry`;
- `cannotUpdateContractExceptExpiryWithWrongKey`.

`ContractRecordsSanityCheckSuite` remains behaviorally unchanged:

- `contractDeleteRecordSanityChecks`: ordinary `contractCreate` 1;
- `contractCreateRecordSanityChecks`: ordinary `contractCreate` 1;
- `contractCallWithSendRecordSanityChecks`: ordinary `contractCreate` 1 and
  one logical `contractCallWithSendRecordSanityChecks` call wrapper;
- `circularTransfersRecordSanityChecks`: `createDefaultContract` 1,
  `contractCallWithTuple` 1, and `contractCallWithFunctionAbi` 1;
- `contractUpdateRecordSanityChecks`: ordinary `contractCreate` 1.

All five methods remain `DEFER_P07_11C`. This enumeration adds subtype detail
without reclassifying historical ownership.

### Aggregate census

| Aggregate family | P07-11B scope | Shared P07-11C owners | Total |
| --- | ---: | ---: | ---: |
| `contractCreate` | 115 | 26 | **141** |
| all `contractCall` family operations | 54 | 10 | **64** |
| `ethereumCall` | 9 | 0 | **9** |
| executable local query | 10 | 1 | **11** |

### Subtype census

| Aggregate family | Concrete subtype | Count |
| --- | --- | ---: |
| `contractCreate` | ordinary `contractCreate` | 129 |
| `contractCreate` | `createDefaultContract` | 11 |
| `contractCreate` | `contractCustomCreate` | 1 |
| `contractCall` | ordinary `contractCall` | 54 |
| `contractCall` | `contractCallWithFunctionAbi` | 8 |
| `contractCall` | `contractCallWithTuple` | 1 |
| `contractCall` | logical `contractCallWithSendRecordSanityChecks` | 1 |
| `ethereumCall` | ordinary `ethereumCall` | 6 |
| `ethereumCall` | `ethereumCryptoTransfer` | 1 |
| `ethereumCall` | `ethereumCryptoTransferToAlias` | 2 |
| executable local query | ordinary `contractCallLocal` | 10 |
| executable local query | `contractCallLocalWithFunctionAbi` | 1 |

Each subtype sum equals its aggregate-family total. In particular, the one
ordinary call physically inside `contractCallWithSendRecordSanityChecks` is
reported under the logical wrapper subtype, reducing the ordinary subtype
from its raw factory-name count of 55 to 54 without changing the aggregate of
64.

### Ownership and method census

| Final disposition | Methods/factories | Create | Call | Ethereum | Local |
| --- | ---: | ---: | ---: | ---: | ---: |
| `REMOVE_P07_11B` | 79 | 81 | 35 | 9 | 7 |
| `SPLIT_REQUIRED` | 23 | 28 | 17 | 0 | 0 |
| `DEFER_P07_11C` | 30 | 32 | 12 | 0 | 4 |
| `RETAIN_NATIVE` | 0 | 0 | 0 | 0 | 0 |
| `UNRESOLVED` | 0 | 0 | 0 | 0 | 0 |
| **Total** | **132** | **141** | **64** | **9** | **11** |

The 30 deferred methods comprise 24 methods in the two protected owners, five
executable-query methods in the three query suites, and
`FileUpdateSuite.gasLimitOverMaxGasLimitFailsPrecheck`.

### Generation census and caller closures

| Generation form | Operation constructions |
| --- | ---: |
| direct in registered/enclosing behavior | 192 |
| helper-contained | 7 |
| dynamically generated by operation factories | 26 |
| **Total** | **225** |

The seven helper-contained constructions are owned by the two
`RepeatableIntegrationTests` signature helpers, the two airdrop
`createAccountsAndKeys` helpers, and three `deployMutableContract` helpers.
Their caller ownership is:

- `addEd25519SigViaContractAndExpect` and
  `addEcdsaSigViaContractAndExpect`: two callers each, four invocation edges
  total; both callers and both helpers are `REMOVE_P07_11B`;
- `TokenAirdropSimpleFeesTest.createAccountsAndKeys` and
  `TokenClaimAndCancelAirdropSimpleFeesTest.createAccountsAndKeys`: retained
  native callers require `SPLIT_REQUIRED`; only contract setup is removable;
- `TokenAirdropBase.deployMutableContract`: zero remaining callers and
  `REMOVE_P07_11B`;
- the HIP-991 `deployMutableContract` helpers: one contract-collector caller
  each; helper and caller are `REMOVE_P07_11B`.

Dynamic factories are the five Kitchen Sink category factories, six
regression initializer factories, and three steady-state throttle provider
methods. Their caller consequences are recorded in their method rows:
mixed native factories are `SPLIT_REQUIRED`, while execution-only provider
branches are `REMOVE_P07_11B`.

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
- the two contract-auto-renew methods in
  `consensus/AtomicTopicCreateSuite.java`;
- the two contract-auto-renew methods in `consensus/TopicCreateSuite.java`;
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
- `token/TokenAssociationSpecs.java` contract-account methods;
- `token/batch/AtomicTokenAssociationSpecs.java` contract-account methods;
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
