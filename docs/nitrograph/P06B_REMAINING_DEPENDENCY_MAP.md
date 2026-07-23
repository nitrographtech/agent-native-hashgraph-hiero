# P06B Remaining Dependency Map

Baseline: `agent-native@54fbee50f55c565b98e60b039ad832fc5ea0affe`

This map separates executable runtime coupling from historical data translation.
Counts below cover production Java imports under `hedera-node`; test imports are
reported separately by the reproducible inventory commands at the end.

## Module inventory

| Module | Besu-importing production files | Tuweni-importing production files | Classification | Reachability and disposition |
| --- | ---: | ---: | --- | --- |
| `hedera-smart-contract-service-impl` | 159 | 79 | `EXECUTABLE_RUNTIME`, `FULL_RUNTIME_ONLY`, `REMOVABLE_LATER` | EVM processors, frames, world updaters, tracers, Dagger execution modules, Besu adapters, contract result/action/sidecar production |
| `hedera-app` | 3 before P06B-1; 2 after | 0 | mixed `SHARED_NATIVE`, `STANDALONE_ONLY` | `BlockItemsTranslator` was `HISTORICAL_DATA_MODEL` and `REMOVABLE_NOW`; `TransactionExecutors` and `NoopVerificationStrategies` remain standalone/full-runtime paths |
| `hapi-utils` | 4 | 1 | mixed `SHARED_NATIVE`, `HISTORICAL_DATA_MODEL`, `REMOVABLE_LATER` | Ethereum signature/transaction utilities and conversions; module requirements remain transitive |
| `hedera-token-service-impl` | 0 | 1 | `SHARED_NATIVE`, `REMOVABLE_LATER` | Tuweni byte representation remains outside the log slice |
| `test-clients` | 11 | 46 | `FULL_RUNTIME_ONLY`, `STANDALONE_ONLY` | Full-runtime BDD execution, stream translators, fixture generation, and compatibility assertions |

The P06B-1 target is only the Besu `Log` dependency in
`hedera-app/.../blocks/BlockItemsTranslator.java`. Other rows are not authorized
for removal in this slice.

## Module descriptors and Gradle edges

- `com.hedera.node.app` still has transitive requirements on
  `org.hyperledger.besu.datatypes` and `org.hyperledger.besu.evm`. These remain
  because full and standalone runtime construction still resides in this module.
- `com.hedera.node.app.service.contract.impl` transitively requires Besu
  datatypes/EVM and Tuweni bytes/units and directly requires Besu crypto.
- `com.hedera.node.app.hapi.utils` transitively requires Besu EVM/native
  secp256k1 and Tuweni bytes.
- `com.hedera.node.app.service.token.impl` requires Tuweni bytes.
- `com.hedera.node.test.clients` requires Besu datatypes, EVM, crypto,
  native-secp256k1, and Tuweni bytes/units.
- The `hedera-app` test module requires Tuweni bytes. P06B-1 does not change
  module descriptors or distribution-wide dependency declarations because the
  standalone and full-runtime paths still require them.

## Construction, Dagger, and service boundaries

| Path | Classification | P06B disposition |
| --- | --- | --- |
| `Hedera` → `FullContractRuntimeProvider` → `ContractServiceImpl` | `FULL_RUNTIME_ONLY`, `EXECUTABLE_RUNTIME` | retain |
| native `Hedera` → `HistoricalContractRuntimeProvider` | `SHARED_NATIVE`, read-only | retain; executable provider remains unresolved |
| `TransactionExecutors` → `ContractServiceImpl` | `STANDALONE_ONLY`, `EXECUTABLE_RUNTIME` | retain |
| `ContractServiceComponent` and versioned `V034`…`V066` Dagger modules | `EXECUTABLE_RUNTIME` | retain |
| shared handler composition | `SHARED_NATIVE` | already fail-closed from P06A |
| contract implementation module service/provider metadata | `FULL_RUNTIME_ONLY` | retain |

No service-loader edge creates an executable contract provider in native mode.
The P06A class-load and provider-resolution gates remain authoritative.

## Translation paths

| Data path | Current type boundary | Classification | Planned slice |
| --- | --- | --- | --- |
| block/record log translation | `EvmTransactionLog` → neutral `HistoricalLog` → `ContractLoginfo` | `SHARED_NATIVE`, `HISTORICAL_DATA_MODEL`, `REMOVABLE_NOW` | P06B-1 |
| log bloom | neutral address/topic bytes and Keccak-256 | `SHARED_NATIVE`, `HISTORICAL_DATA_MODEL` | P06B-1 |
| full-runtime Besu log production | Besu `Log` → block trace PBJ values | `FULL_RUNTIME_ONLY`, `EXECUTABLE_RUNTIME` | retain adapter/source path |
| contract function results | Besu-backed runtime result assembly | mixed historical/executable | later P06B |
| contract actions | Besu frames/tracers → sidecar/action PBJ | mixed historical/executable | later P06B |
| contract state changes | world updater/storage abstractions → PBJ | mixed historical/executable | later P06B |
| sidecars | action, bytecode, and state-change translators | `HISTORICAL_DATA_MODEL` plus full-runtime producers | later P06B |
| Ethereum-format history | transaction/result/signature utilities | mixed `SHARED_NATIVE` and `FULL_RUNTIME_ONLY` | later P06B |

## P06B-2 contract-result flow

The shared result path starts with neutral PBJ block values, not executable
Besu results:

`TransactionOutput` → PBJ `EvmTransactionResult` → neutral
`HistoricalContractResult` → PBJ `ContractFunctionResult` → record/block output
→ official mirror importer.

| Field or edge | Current source | Reachability | Classification | P06B-2 disposition |
| --- | --- | --- | --- | --- |
| sender and receiving contract | PBJ `AccountID`, `ContractID` | shared/native | `HISTORICAL_DATA_MODEL`, `SHARED_NATIVE`, `REMOVABLE_IN_P06B_2` | snapshot in neutral result |
| return bytes and error text | PBJ `Bytes`, `String` | shared/native | `HISTORICAL_DATA_MODEL`, `SHARED_NATIVE`, `REMOVABLE_IN_P06B_2` | preserve verbatim |
| gas used, gas limit, amount/value | protobuf `uint64` represented by Java `long` | shared/native | `HISTORICAL_DATA_MODEL`, `REMOVABLE_IN_P06B_2` | retain raw bit patterns; no economic interpretation |
| call/function parameters | PBJ `InternalCallContext` | shared/native | `HISTORICAL_DATA_MODEL`, `REMOVABLE_IN_P06B_2` | neutral `HistoricalCallContext` |
| signer nonce and created contracts | `ContractOpContext` | shared/native | `HISTORICAL_DATA_MODEL`, `REMOVABLE_IN_P06B_2` | preserve nullable presence |
| EVM address and Ethereum hash | PBJ `Bytes` in `ContractOpContext` | shared/native | `HISTORICAL_DATA_MODEL`, `REMOVABLE_IN_P06B_2` | result address is snapshotted; record-level Ethereum hash remains PBJ |
| changed contract nonces | PBJ `ContractNonceInfo` | shared/native | `HISTORICAL_DATA_MODEL`, `REMOVABLE_IN_P06B_2` | retain established contract-ID ordering |
| logs and aggregate bloom | neutral P06B-1 values | shared/native | `HISTORICAL_DATA_MODEL`, `SHARED_NATIVE` | reused unchanged |
| action sidecars | contract implementation tracers → PBJ | full runtime producer; historical consumer | `EXECUTABLE_RUNTIME`, `DEFER_TO_ACTION_SLICE` | unchanged |
| state/storage changes | world updater → PBJ | full runtime producer; historical consumer | `EXECUTABLE_RUNTIME`, `DEFER_TO_STATE_CHANGE_SLICE` | unchanged |
| bytecode/action/state-change sidecar association | PBJ stream types | shared/full | `HISTORICAL_DATA_MODEL`, `DEFER_TO_SIDECAR_SLICE` | unchanged |
| Besu `Address`, `Hash`, `Log`, `Wei`, `Gas` | contract implementation internals | full runtime | `EXECUTABLE_RUNTIME`, `FULL_RUNTIME_ONLY`, `DEFER_TO_FINAL_REMOVAL` | no shared result import or adapter |
| Tuweni `Bytes`/`UInt256` | contract implementation and HAPI utilities | full runtime plus unrelated shared utilities | mixed | no shared result import; later slices |

`BlockItemsTranslator` calls no contract-implementation result utility. Its one
contract utility call, `HookUtils.leftPad32`, is byte padding for historical log
topics and remains part of the later general byte-utility cleanup. No Besu or
Tuweni adapter is introduced because the shared input is already PBJ data.

The native-agent path reaches the PBJ-to-neutral adapter and neutral-to-record
translation. Full runtime reaches the same path after executable processing has
produced PBJ block output. Executable result construction, Dagger bindings, and
service providers remain confined to the contract implementation and are
`DEFER_TO_FINAL_REMOVAL`.

Record and block wire values remain PBJ types. P06B introduces no persisted state,
wire-format, codec, state-ID, or schema change.

## P06B-3 contract-action flow

Executable action construction remains isolated from shared stream translation:

`MessageFrame`/Besu/Tuweni → executable `EvmActionTracer` and `ActionStack`
→ PBJ `ContractAction` → neutral `HistoricalContractAction`
→ shared record/block builders → PBJ record sidecar or block `EvmTraceData`
→ official mirror importer.

| Field or edge | Current source | Reachability | Classification | P06B-3 disposition |
| --- | --- | --- | --- | --- |
| action tracer, stack, and frame lifecycle | Besu `MessageFrame`, `Address`, `Wei`, Tuweni `Bytes` | full runtime only | `EXECUTABLE_RUNTIME`, `FULL_RUNTIME_ONLY`, `DEFER_TO_FINAL_REMOVAL` | unchanged |
| executable-to-PBJ action production | `ActionsHelper`, `ActionStack`, `EvmActionTracer` | full runtime only | `EXECUTABLE_RUNTIME` | retained as the producer boundary; no shared adapter added |
| PBJ action ingress | `ContractOperationStreamBuilder.addActions` / `addContractActions` | full producer into shared builders | `HISTORICAL_DATA_MODEL`, `REMOVABLE_IN_P06B_3` | snapshotted immediately into neutral values |
| caller and recipient identity one-ofs | PBJ `AccountID`, `ContractID`, or targeted address bytes | shared/native historical path | `HISTORICAL_DATA_MODEL`, `SHARED_NATIVE`, `REMOVABLE_IN_P06B_3` | presence preserved explicitly |
| input, output, revert, and error one-ofs | PBJ `Bytes` | shared/native historical path | `HISTORICAL_DATA_MODEL`, `SHARED_NATIVE`, `REMOVABLE_IN_P06B_3` | bytes and null/empty presence preserved |
| action type and operation type | PBJ wire enums | shared/native historical path | `HISTORICAL_DATA_MODEL`, `REMOVABLE_IN_P06B_3` | retained without executable behavior |
| gas, gas-used, and value fields | protobuf `int64` Java bit patterns | shared/native historical path | `HISTORICAL_DATA_MODEL`, `REMOVABLE_IN_P06B_3` | preserved verbatim; no native gas semantics introduced |
| action index and hierarchy | list position plus `callDepth` | shared/native historical path | `HISTORICAL_DATA_MODEL`, `REMOVABLE_IN_P06B_3` | immutable ordered lists preserve both |
| record sidecar serialization | neutral action → PBJ `ContractActions` | shared record builder | `HISTORICAL_DATA_MODEL`, `SHARED_NATIVE`, `DEFER_TO_SIDECAR_SLICE` | wire bytes preserved; broader sidecar work deferred |
| block trace serialization | neutral action → PBJ `EvmTraceData` | shared block builder | `HISTORICAL_DATA_MODEL`, `SHARED_NATIVE` | wire bytes preserved |
| state/storage and bytecode sidecars | PBJ stream values | mixed | `DEFER_TO_STATE_CHANGE_SLICE`, `DEFER_TO_SIDECAR_SLICE` | unchanged |

There is no Besu/Tuweni adapter in `hedera-app`: the shared input is already
neutral PBJ data. `HistoricalContractAction` is a non-executable immutable
snapshot between PBJ ingress and PBJ wire emission. No `MessageFrame`, action
tracer, Besu, or Tuweni type crosses into the shared action path. The native
provider does not construct or resolve the executable producer.

## P06B-4 state and storage-change flow

Executable state tracking remains isolated from shared stream translation:

`WorldUpdater`/`MutableAccount`/Besu `UInt256` and Tuweni bytes
→ full-runtime `StorageAccesses`
→ contract-implementation `ConversionUtils`
→ PBJ `ContractStateChanges` or `ContractSlotUsage`
→ neutral historical snapshots at shared ingress
→ PBJ sidecar or block trace output
→ official mirror importer.

| Field or edge | Current source | Reachability | Classification | P06B-4 disposition |
| --- | --- | --- | --- | --- |
| world state, mutable accounts, and storage tracking | Besu/Tuweni contract implementation | full runtime only | `EXECUTABLE_RUNTIME`, `FULL_RUNTIME_ONLY`, `DEFER_TO_FINAL_REMOVAL` | unchanged |
| executable-to-PBJ state-change conversion | `ConversionUtils.asPbjStateChanges` | full runtime producer | `EXECUTABLE_RUNTIME` | retained producer boundary |
| executable-to-PBJ slot-usage conversion | `ConversionUtils.asPbjSlotUsages` | full runtime producer | `EXECUTABLE_RUNTIME` | retained producer boundary |
| contract identity and grouping | PBJ `ContractID` and ordered lists | shared/native historical path | `HISTORICAL_DATA_MODEL`, `SHARED_NATIVE`, `REMOVABLE_IN_P06B_4` | immutable neutral grouping |
| slot, read value, and written value | PBJ bytes, each 0–32 minimal big-endian bytes | shared/native historical path | `HISTORICAL_DATA_MODEL`, `SHARED_NATIVE`, `REMOVABLE_IN_P06B_4` | byte and presence preserving neutral value |
| absent write versus explicit zero write | nullable PBJ `valueWritten` versus present empty bytes | shared/native historical path | `HISTORICAL_DATA_MODEL`, `REMOVABLE_IN_P06B_4` | preserved explicitly |
| reverted state-change handling | shared record builder removes only written-value presence | shared/native historical path | `HISTORICAL_DATA_MODEL`, `SHARED_NATIVE` | implemented on neutral values |
| record state-change sidecar | neutral values → PBJ `ContractStateChanges` | shared record builder | `HISTORICAL_DATA_MODEL`, `SHARED_NATIVE` | wire bytes preserved |
| block slot trace | PBJ `ContractSlotUsage`, state-change indexing | shared block builder | `HISTORICAL_DATA_MODEL`, `SHARED_NATIVE` | already Besu/Tuweni-free; PBJ wire one-of retained |
| bytecode sidecar | PBJ `ContractBytecode` | mixed | `DEFER_TO_BYTECODE_SIDECAR_SLICE` | unchanged |
| HAPI byte utilities | shared utility modules | mixed | `DEFER_TO_HAPI_BYTE_SLICE` | unchanged |

No shared adapter is needed: executable world-state objects are converted to
PBJ inside `hedera-smart-contract-service-impl` before invoking a shared stream
builder. The native path reaches only PBJ-to-neutral snapshot construction and
neutral-to-PBJ wire emission. `HistoricalContractStateChanges`,
`HistoricalContractStateChange`, and `HistoricalStorageChange` contain no
world-state access or mutation behavior.

The wire schema deliberately permits 0–32 byte minimal big-endian slot and
value encodings. The neutral boundary validates that upper bound without
padding, trimming, or otherwise changing bytes. A missing written value remains
distinct from a present empty written value.

## P06B-5 bytecode-sidecar flow

Executable bytecode production remains isolated from shared sidecar
translation:

Besu `MessageFrame`, `Code`, `Address`, and Tuweni bytes
→ full-runtime `CustomContractCreationProcessor`
→ PBJ `ContractBytecode` or block `ExecutedInitcode`
→ neutral historical bytecode snapshot at shared record ingress
→ PBJ bytecode sidecar
→ official mirror importer.

| Field or edge | Current source | Reachability | Classification | P06B-5 disposition |
| --- | --- | --- | --- | --- |
| creation frame and runtime code lookup | Besu frame, account, and code objects | full runtime only | `EXECUTABLE_RUNTIME`, `FULL_RUNTIME_ONLY`, `DEFER_TO_FULL_RUNTIME_REMOVAL` | unchanged |
| executable byte conversion | `tuweniToPbjBytes` in `CustomContractCreationProcessor` | full runtime only | `EXECUTABLE_RUNTIME`, `DEFER_TO_HAPI_BYTE_SLICE` | retained before shared ingress |
| failed creation initcode sidecar | PBJ `ContractBytecode` with absent ID/runtime | full producer into shared record builder | `HISTORICAL_DATA_MODEL`, `REMOVABLE_IN_P06B_5` | neutral snapshot preserves empty-byte wire semantics |
| successful creation bytecode sidecar | PBJ contract ID, initcode, and runtime bytes | shared/native historical path | `HISTORICAL_DATA_MODEL`, `SHARED_NATIVE`, `REMOVABLE_IN_P06B_5` | immutable neutral value |
| contract association | nullable PBJ `ContractID` | shared/native historical path | `HISTORICAL_DATA_MODEL`, `REMOVABLE_IN_P06B_5` | presence preserved |
| record sidecar serialization | neutral bytecode → PBJ `ContractBytecode` | shared record builder | `HISTORICAL_DATA_MODEL`, `SHARED_NATIVE` | byte-identical wire output |
| block creation trace | PBJ `ExecutedInitcode` | shared block builder | `HISTORICAL_DATA_MODEL`, `SHARED_NATIVE` | already Besu/Tuweni-free; unchanged |
| persisted BYTECODE state | PBJ `Bytecode` state value | historical provider | `HISTORICAL_DATA_MODEL`, `SHARED_NATIVE` | read-only and unchanged |
| Dagger/full provider and contract implementation | executable service graph | full runtime | `EXECUTABLE_RUNTIME`, `DEFER_TO_FINAL_PHYSICAL_REMOVAL` | unchanged |

No Besu/Tuweni adapter is added to `hedera-app`. The executable producer already
converts code to PBJ bytes inside the full contract implementation.
`HistoricalContractBytecode` only snapshots the PBJ contract association,
initcode, and runtime bytes. It performs no code validation, hashing, execution,
or state lookup. The `ContractBytecode` wire model has no EVM-address field;
historical association is by `ContractID`.

## Distribution reachability

- Full distribution: retains `ContractServiceImpl`, Besu execution jars, Tuweni,
  native crypto, standalone execution, and compatibility adapters.
- Native-agent distribution: selects the historical provider; P06A instrumented
  activation/restart/replay/reconnect loaded no Besu/Tuweni or prohibited EVM
  engine classes.
- Packaged jars may still be present because `hedera-app` and `hapi-utils` retain
  full-runtime module edges. Physical packaging removal is not part of P06B-1.

## Classification summary

- `REMOVABLE_NOW`: Besu `Log`, `asBesuLog`, `bloomFor`, and `bloomForAll` usage
  in the shared `BlockItemsTranslator` log path.
- `REMOVABLE_LATER`: shared result, action, state-change, sidecar, and
  Ethereum-format translation types after independent golden-output gates.
- `EXECUTABLE_RUNTIME`: contract implementation processors, frames, operations,
  tracers, world updater, Dagger versions, and native libraries.
- `FULL_RUNTIME_ONLY`: the full provider and contract implementation module.
- `STANDALONE_ONLY`: `TransactionExecutors` and its verification/operation setup.
- `SHARED_NATIVE`: block/record translation, HAPI utilities, and remaining token
  byte utilities; each must be neutralized before physical removal.

## Reproducible inventory

```bash
rg -l '^import org\.hyperledger\.besu' hedera-node --glob '*.java'
rg -l '^import org\.apache\.tuweni' hedera-node --glob '*.java'
rg -n 'requires .*besu|requires .*tuweni' hedera-node --glob module-info.java
rg -n '(besu|tuweni)' hedera-node --glob build.gradle.kts
rg -n 'ContractServiceImpl|FullContractRuntimeProvider|HistoricalContractRuntimeProvider' hedera-node
rg -n 'BlockItemsTranslator|ContractLoginfo|EvmTransactionLog|ContractAction|StateChange' hedera-node
```
