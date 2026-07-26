# P07-11 Executable HAPI Ownership

## Checkpoint and scope

This census was measured from exact head
`c24bb9e0172ca2920c6b06e6932e2dce84ae2135`. CI run
`30180320165` passed and the local and remote heads matched with a clean
worktree.

The census proves that the residual boundary is not a small signing-helper
cleanup. Successful executable HAPI ownership remains distributed across 93
suite files totaling 3,182,123 bytes. Those files contain 426
`contractCreate(...)` occurrences, 305 `contractCall(...)` occurrences, 74
`ethereumCall(...)` occurrences, and 15 suite files with executable local-call
or bytecode-query use.

## Core operation ownership

| Symbol | Path | Direct/reverse ownership | Classification | Decision |
| --- | --- | --- | --- | --- |
| `HapiContractCreate` | `spec/transactions/contract/HapiContractCreate.java` | 9 direct class references; factory, DSL, random-provider, update, Ethereum-create and utility consumers | EXECUTABLE_CONTRACT_CREATE_OPERATION | DELETE only after suite/factory closure |
| `HapiContractCall` | `spec/transactions/contract/HapiContractCall.java` | 18 direct class references; DSL, random-provider, query, transaction, utility and reconnect consumers | EXECUTABLE_CONTRACT_CALL_OPERATION | SPLIT rejection consumer, then delete success API |
| `HapiEthereumCall` | `spec/transactions/contract/HapiEthereumCall.java` | 5 direct class references | EXECUTABLE_ETHEREUM_OPERATION | DELETE with Ethereum suites/providers |
| `HapiEthereumContractCreate` | `spec/transactions/contract/HapiEthereumContractCreate.java` | 3 direct class references | EXECUTABLE_ETHEREUM_OPERATION | DELETE with Ethereum suites |
| `HapiContractCallLocal` | `spec/queries/contract/HapiContractCallLocal.java` | DSL/query/random-provider consumers | EXECUTABLE_QUERY_OPERATION | DELETE after query suites |
| `HapiGetContractBytecode` | `spec/queries/contract/HapiGetContractBytecode.java` | Query factory and historical/execution consumers | EXECUTABLE_QUERY_OPERATION | SPLIT historical inspection before deletion |
| `Signing` | `utils/Signing.java` | only the two Ethereum operation classes | EXECUTABLE_SIGNING | DELETE atomically with both Ethereum operations |
| `ContractFnResultAsserts` | `spec/assertions/ContractFnResultAsserts.java` | 38 direct references spanning execution and historical validation | EXECUTABLE_RESULT_EXPECTATION / HISTORICAL_PARSER | SPLIT before deletion |
| `ContractLogAsserts` | `spec/assertions/ContractLogAsserts.java` | 8 direct references | EXECUTABLE_LOG_EXPECTATION / HISTORICAL_PARSER | SPLIT before deletion |
| `GasMatcher` | `spec/assertions/matchers/GasMatcher.java` | matcher infrastructure | EXECUTABLE_GAS_EXPECTATION | DELETE with execution expectations |

The four transaction operation classes plus `Signing` contain 68,840 source
bytes. They cannot be removed first: their reverse consumers are the remaining
suite and provider closure.

## Active dependency ownership

The test-client JPMS module still requires:

- `org.hyperledger.besu.datatypes`;
- `org.hyperledger.besu.evm`;
- `org.hyperledger.besu.internal.crypto`;
- `org.hyperledger.besu.nativelib.secp256k1`;
- `tuweni.bytes`;
- `tuweni.units`;
- `com.esaulpaugh.headlong`.

Direct main-source importer counts are Besu 7, Tuweni 13, and Headlong 100.
Resource inventory still includes 260 Solidity sources, 227 contract ABI JSON
files, and 377 contract binary files. Dependency edges cannot be removed before
the corresponding suite/resource owners.

## Suite ownership table

Counts are literal source occurrences for create, call, Ethereum call, Ethereum
create, and local call respectively. A row classified
`CONTRACT_BASED_TEST_SETUP` must be split method-by-method so native behavior
is retained. `DELETE_IN_P07_11` is an execution-only suite candidate, subject
to reverse-registration and historical checks.

| Path | Create | Call | Eth call | Eth create | Local call | Classification |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/consensus/AtomicTopicCreateSuite.java` | 1 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/consensus/TopicCreateSuite.java` | 1 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/classiccalls/FailureCharacterizationSuite.java` | 1 | 1 | 0 | 0 | 1 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/ethereum/HelloWorldEthereumSuite.java` | 8 | 2 | 10 | 6 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/ethereum/JumboTransactionsEnabledTest.java` | 1 | 1 | 15 | 1 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/ethereum/NonceSuite.java` | 22 | 1 | 21 | 12 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/ethereum/batch/AtomicHelloWorldEthereumSuite.java` | 8 | 2 | 10 | 5 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/evm/Evm50ValidationSuite.java` | 2 | 6 | 0 | 0 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/evm/batch/AtomicEvm50ValidationSuite.java` | 2 | 6 | 0 | 0 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/fees/AtomicSmartContractServiceFeesTest.java` | 1 | 1 | 1 | 0 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/fees/SimpleSmartContractServiceFeesTest.java` | 1 | 2 | 2 | 0 | 2 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/fees/SmartContractServiceFeesTest.java` | 1 | 0 | 1 | 0 | 1 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/hapi/ContractCallHapiOnlySuite.java` | 1 | 1 | 0 | 0 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/hapi/ContractStateSuite.java` | 2 | 17 | 0 | 0 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/hapi/ContractUpdateSuite.java` | 20 | 7 | 0 | 0 | 1 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/hapi/batch/AtomicContractUpdateSuite.java` | 15 | 0 | 0 | 0 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/hip906/HbarAllowanceApprovalTest.java` | 5 | 6 | 0 | 0 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/hips/batch/AtomicIsAuthorizedTest.java` | 18 | 22 | 0 | 0 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/hips/hip632/IsAuthorizedTest.java` | 18 | 22 | 0 | 0 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/leaky/LeakyEthereumTestsSuite.java` | 3 | 0 | 3 | 0 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/leaky/batch/AtomicLeakyEthereumTestsSuite.java` | 3 | 0 | 3 | 0 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/opcodes/CreateOperationSuite.java` | 9 | 11 | 0 | 0 | 2 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/opcodes/DelegateCallOperationSuite.java` | 1 | 2 | 0 | 0 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/opcodes/GlobalPropertiesSuite.java` | 4 | 4 | 0 | 0 | 4 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/opcodes/PrngSeedOperationSuite.java` | 2 | 2 | 0 | 0 | 1 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/opcodes/PushZeroOperationSuite.java` | 2 | 2 | 0 | 0 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/openzeppelin/ERC1155ContractInteractions.java` | 1 | 3 | 0 | 0 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/openzeppelin/ERC721ContractInteractions.java` | 1 | 3 | 0 | 0 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/opsduration/OpsDurationThrottleTest.java` | 12 | 14 | 0 | 0 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/records/ContractRecordsSanityCheckSuite.java` | 4 | 1 | 0 | 0 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/records/LogsSuite.java` | 5 | 5 | 0 | 0 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/records/batch/AtomicLogsSuite.java` | 5 | 5 | 0 | 0 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/validation/EvmValidationTest.java` | 2 | 2 | 0 | 0 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/contract/validation/batch/AtomicEvmValidationTest.java` | 2 | 2 | 0 | 0 | 0 | DELETE_IN_P07_11 |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/crypto/CryptoTransferSuite.java` | 7 | 12 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/crypto/CryptoUpdateSuite.java` | 2 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/crypto/HollowAccountFinalizationSuite.java` | 4 | 3 | 0 | 2 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/crypto/LeakyCryptoTestsSuite.java` | 4 | 1 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/fees/CryptoSimpleFeesSuite.java` | 9 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/fees/CryptoTransferWithHooksSimpleFeesSuite.java` | 3 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/fees/KitchenSinkFeeComparisonSuite.java` | 9 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/fees/ScheduleServiceFeesSuite.java` | 1 | 1 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/fees/ScheduleServiceSimpleFeesTest.java` | 2 | 2 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/file/DiverseStateCreation.java` | 2 | 1 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/file/FileUpdateSuite.java` | 8 | 11 | 2 | 0 | 7 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/file/HistoricalContractExecutionRejection.java` | 0 | 1 | 0 | 0 | 0 | DETERMINISTIC_REJECTION_OPERATION |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip1195/Hip1195DisabledTest.java` | 1 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip1261/AtomicBatchCrossServiceSimpleFeesTest.java` | 1 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip1261/AtomicBatchNegativeSimpleFeesTest.java` | 2 | 1 | 1 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip1261/ContractServiceSimpleFeesTest.java` | 31 | 6 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip1261/CryptoCreateWithHooksSimpleFeesTest.java` | 3 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip1261/CryptoTransferSimpleFeesTest.java` | 10 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip1261/CryptoTransferWithCustomFeesAndHooksSimpleFeesTest.java` | 1 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip1261/CryptoTransferWithCustomFeesSimpleFeesTest.java` | 2 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip1261/CryptoTransferWithHooksSimpleFeesTest.java` | 1 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip1261/CryptoUpdateSimpleFeesTest.java` | 2 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip1261/ScheduleServiceSimpleFeesTest.java` | 1 | 1 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip1261/TokenAirdropSimpleFeesTest.java` | 1 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip1261/TokenClaimAndCancelAirdropSimpleFeesTest.java` | 1 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip423/ScheduleLongTermExecutionTest.java` | 1 | 2 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip551/AtomicBatchInvalidSignaturesTests.java` | 8 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip551/AtomicBatchNegativeTest.java` | 15 | 19 | 3 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip551/contracts/AtomicBatchContractSignatureValidationTest.java` | 12 | 4 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip551/contracts/AtomicBatchEndToEndSmartContractsHTSCallsAndAssociationsTest.java` | 37 | 66 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip551/contracts/AtomicBatchEthereumCallKeysTest.java` | 2 | 2 | 2 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip904/TokenAirdropBase.java` | 1 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip991/AtomicTopicCustomFeeCreateTest.java` | 1 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip991/AtomicTopicCustomFeeUpdateTest.java` | 1 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip991/TopicCustomFeeCreateTest.java` | 1 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip991/TopicCustomFeeUpdateTest.java` | 1 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/hip993/SystemFileExportsTest.java` | 1 | 1 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/integration/CongestionPricingTest.java` | 1 | 3 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/integration/RepeatableHip1215Tests.java` | 0 | 1 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/integration/RepeatableIntegrationTests.java` | 0 | 2 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/integration/RepeatableScheduleLongTermExecutionTest.java` | 2 | 1 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/integration/hip1195/Hip1195BasicTests.java` | 19 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/integration/hip1195/Hip1195StorageTest.java` | 1 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/integration/hip1259/Hip1259EnabledTests.java` | 3 | 2 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/issues/Issue305Spec.java` | 1 | 2 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/issues/IssueRegressionTests.java` | 4 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/misc/InvalidgRPCValuesTest.java` | 1 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/queries/AsNodeOperatorQueriesTestEmbedded.java` | 2 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/queries/DisabledNodeOperatorTest.java` | 2 | 2 | 0 | 0 | 2 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/queries/RepeatableOperatorQueryTests.java` | 1 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/reconnect/P06aHistoricalStateReconnectTest.java` | 2 | 2 | 0 | 0 | 0 | HISTORICAL_FIXTURE_SUPPORT |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/regression/factories/AccountCompletionFuzzingFactory.java` | 1 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/regression/factories/HollowAccountCompletedFuzzingFactory.java` | 1 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/regression/factories/IdFuzzingProviderFactory.java` | 4 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/staking/StakingSuite.java` | 1 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/throttling/PrecompileMintThrottlingCheck.java` | 1 | 1 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/throttling/SteadyStateThrottlingTest.java` | 2 | 2 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/token/TokenAssociationSpecs.java` | 2 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |
| `hedera-node/test-clients/src/main/java/com/hedera/services/bdd/suites/token/batch/AtomicTokenAssociationSpecs.java` | 2 | 0 | 0 | 0 | 0 | CONTRACT_BASED_TEST_SETUP |

## Review boundary

This measured closure is too broad for blind continuation inside PR #28:

- 93 suite files / 3.18 MB are directly involved;
- the largest single suite is 245,138 bytes;
- contract, native-fee, hook, schedule, batch, query, reconnect, fuzzing, and
  historical consumers are interleaved;
- 805 live executable verb occurrences remain;
- broad deletion would exceed the bounded P07-10A/P07-10B review model.

The safe next implementation boundary is a new linear wave after merging the
already-tested P07-10 changes:

1. P07-11A: delete execution-only `suites/contract` and Ethereum provider
   closures.
2. P07-11B: split contract setup from native fee, hook, schedule, batch, topic,
   account, token, and integration suites.
3. P07-11C: replace the remaining general HAPI operations with the minimal
   deterministic rejection operation and historical query adapters.
4. P07-11D: remove released signing, ABI, resource, Gradle, and JPMS ownership.

No production change, fixture mutation, or protected-crypto change is required.

