# P07-10A shared-helper ownership

## Checkpoint

- Baseline branch: `p07/remove-test-client-evm-residue`
- Baseline head: `0dfd53d6797050008e9033f738ce0a91e051fe0d`
- Baseline CI: `30177227323` (`success`)
- Scope: test-client ownership only; production runtime and protected platform cryptography are unchanged.

P07-10A separates data and helper ownership from suites that exercise the retired
contract engine. It does not make suite names into permanent compatibility APIs.

## Ownership rules

| Responsibility | Permanent test-client owner | Forbidden owner |
|---|---|---|
| Historical result translation vectors | `HistoricalContractResultFixtures` | `RecordsSuite` |
| Historical sidecar vectors | `HistoricalSidecarFixtures` / `SidecarVerbs` | trace execution suites |
| Historical storage encoding | `HistoricalStorageEncoding` | `EncodingUtils` |
| Deterministic retired-body vectors | `LegacyTransactionVectors` | executable contract suites |
| Native system-account and alias vectors | `NativeAccountTestVectors` | `Evm46ValidationSuite` |
| Native token/airdrop vectors | `NativeTokenTestVectors` when genuinely native | `Create2OperationSuite` |
| Native ECDSA signing | `NativeEcdsaSigning` | mixed Ethereum/runtime signing utility |

These are narrow test-client owners. They are not exported to production, neutral
contract APIs, fixture tooling, or the protected platform crypto layer.

## Exact shared symbols

| Current owner | Symbol | Direct retained consumers | Classification | Dependency surface | Decision |
|---|---|---|---|---|---|
| `RecordsSuite` | `OVERSIZED_CONTRACT_ACTIONS_MEMO : String` | `ContractCallTranslator` | `HISTORICAL_RESULT_TRANSLATION`, `DEAD_SUITE_OWNERSHIP` | JDK only | Move to `HistoricalContractResultFixtures`; remove translator-to-suite edge |
| `EncodingUtils` | `getInitcode(String,Object...) : ByteString` | `SidecarVerbs`; trace suite | `HISTORICAL_SIDECAR_TRANSLATION` | PBJ/protobuf, Headlong; no Besu; no Tuweni | Move the retained sidecar operation to historical ownership |
| `EncodingUtils` | `formattedAssertionValue(long) : ByteString` | HIP-1195 storage validation; trace suite | `HISTORICAL_STORAGE_VALIDATION` | Tuweni `Bytes`, `UInt256` | Replace with exact dependency-free unsigned minimal-width encoding |
| `EncodingUtils` | `formattedAssertionValue(String) : ByteString` | trace suite only | `DEAD_SUITE_OWNERSHIP` | Tuweni `Bytes`, `UInt256` | Delete with trace suite unless a retained historical vector is found |
| `EncodingUtils` | ABI tuple/function/address helpers | trace suite only | `DEAD_SUITE_OWNERSHIP` | Headlong | Delete with trace execution coverage |
| `TraceabilitySuite` | test identity string `TraceabilitySuite.actionsShowPropagatedRevert` | `ContractCreateTranslator` | `HISTORICAL_RESULT_TRANSLATION`, `DEAD_SUITE_OWNERSHIP` | JDK only | Move exact memo/test identity to historical result fixtures |
| `TraceabilitySuite` | class reference in `GasMatcher` documentation | `GasMatcher` | `DEAD_SUITE_OWNERSHIP` | JDK only | Remove suite coupling; document gas tolerance generically |
| `Evm46ValidationSuite` | `existingSystemAccounts : List<Long>` | `CryptoTransferSuite` | `NATIVE_ACCOUNT_VECTOR`, `DEAD_SUITE_OWNERSHIP` | JDK only | Move to `NativeAccountTestVectors` |
| `Evm46ValidationSuite` | `nonExistingSystemAccounts : List<Long>` | `CryptoTransferSuite`, `SelfDestructSuite` | mixed native/executable vector | JDK only | Move native use; executable consumer is deleted later |
| `Evm46ValidationSuite` | `systemAccounts : List<Long>` | opcode suites | `DEAD_SUITE_OWNERSHIP` | JDK only | Delete with executable opcode suites |
| `Create2OperationSuite` | constants and `setExpectedCreate2Address` | token-airdrop and trace suites | executable CREATE2 support, not a neutral alias helper | Tuweni plus live contract operations | Do not relabel as native; delete consumers that require retired contract execution |
| `Create2OperationSuite` | `assertCreate2Address`, `setIdentifiers`, `lazyCreateAccount` | `AirdropsDisabledTest` | mixed executable/native orchestration | live contract create/call plus native account operations | Split only if a native-only consumer remains after executable coverage deletion |
| `ContractCreateSuite` | `EMPTY_CONSTRUCTOR_CONTRACT : String` | `LeakyCryptoTestsSuite` | contract resource vector | JDK only, but consumer deploys a contract | Move only if retained rejection coverage uses it; otherwise delete both execution paths |
| `HTSPrecompileResult` | fluent expected-result encoder | allowance, atomic batch, integration, precompile suites | mixed legacy system-contract expectation | Headlong/PBJ/protobuf; Tuweni through result types | Keep temporarily while method-level consumer classification is completed; do not move to production |
| `Signing` | `signMessage(EthTxData,byte[],boolean)` and overload | `HapiEthereumCall`, `HapiEthereumContractCreate`, contract-create suite | dead executable Ethereum signing | native secp256k1 binding, Ethereum model | Delete with executable Ethereum builders |
| `Signing` | `signMessage(byte[],byte[]) : byte[65]` | retained authorization/integration suites | `NATIVE_ECDSA_SIGNING` | native secp256k1 binding; no Besu datatypes/Tuweni | Move to `NativeEcdsaSigning` unchanged |
| `Signing` | `signMessageEd25519(byte[],byte[])` | no currently observed reverse consumer | dead generic signing residue | Bouncy Castle | Delete after final reverse-consumer check |

## Consumer ownership

| Consumer | Current dependency | Required purpose | P07-10A action |
|---|---|---|---|
| `ContractCallTranslator` | `RecordsSuite` | historical block/record translation exception | Depend on `HistoricalContractResultFixtures` |
| `ContractCreateTranslator` | suite-named literal | historical translation exception | Depend on the named historical fixture constant |
| `GasMatcher` | `TraceabilitySuite` class in Javadoc | matcher documentation only | Remove class-level suite dependency |
| `SidecarVerbs` | `EncodingUtils.getInitcode` | historical sidecar bytecode expectation | Depend on historical sidecar encoding owner |
| `Hip1195StorageTest` | `EncodingUtils.formattedAssertionValue(long)` | historical storage value comparison | Depend on `HistoricalStorageEncoding` |
| `CryptoTransferSuite` | `Evm46ValidationSuite` lists | native system-account transfer vectors | Depend on `NativeAccountTestVectors` |
| `LeakyCryptoTestsSuite` | contract suite resource name | live contract execution | Delete when resumed P07-10 removes the execution-only path |
| HIP-904 airdrop suites | CREATE2 suite constants/helpers | live CREATE2 deployment and address extraction | Classify as executable coverage; do not preserve as native helpers |
| allowance/batch/integration suites | `HTSPrecompileResult` | legacy precompile result encoding | Retain temporarily pending exact method-use census |
| authorization/topic/account/airdrop tests | `Signing.signMessage(byte[],byte[])` | native ECDSA authorization | Depend on `NativeEcdsaSigning` |
| Ethereum call/create builders | `Signing.signMessage(EthTxData,...)` | retired executable transaction generation | Delete in resumed P07-10 |

## Dependency conclusions

- The historical constant/vector moves require no Besu or Tuweni types.
- Historical `uint256` assertion encoding can be expressed exactly with
  `BigInteger.toByteArray()` plus explicit sign-byte removal and negative-value
  rejection; no `UInt256` dependency is required.
- CREATE2 consumers still execute retired contract operations. Their ownership
  cannot honestly be classified as native helper ownership.
- The retained native signing method uses the test-client native secp256k1
  binding directly. It does not require Besu datatypes or Tuweni, and it does not
  alter `platform-sdk/base-crypto`.
- `HTSPrecompileResult` is a mixed legacy owner and remains a checkpoint gate.

## Planned order

1. Move historical translator and storage symbols.
2. Move native system-account vectors.
3. Split native ECDSA signing from executable Ethereum signing.
4. Delete suite shells and consumers proven to require retired execution.
5. Recompute reverse dependencies and Besu/Tuweni ownership.
6. Resume only the bounded deletion set exposed by the new graph.
