# P06A Historical Contract State Dependency and Compatibility Map

Status labels are `CONFIRMED`, `INFERENCE`, and `OPEN`.

## Safety invariant

`CONFIRMED`: The native-agent distribution retains the exact `ContractService` service
name, schema versions, state IDs, state-key literals, and PBJ codecs below. The historical
provider registers state only. It has no executable handlers, fee calculators, metrics,
native-library verification, Besu dependency, EVM dependency, or configuration switch
that can enable execution.

## Retained schemas and serialization

| Version | State ID source | Literal key | Key codec | Value codec | Purpose |
|---|---|---|---|---|---|
| 0.49.0 | `CONTRACTSERVICE_I_STORAGE.protoOrdinal()` | `STORAGE` | `SlotKey.PROTOBUF` | `SlotValue.PROTOBUF` | Historical contract storage |
| 0.49.0 | `CONTRACTSERVICE_I_BYTECODE.protoOrdinal()` | `BYTECODE` | `ContractID.PROTOBUF` | `Bytecode.PROTOBUF` | Historical bytecode |
| 0.65.0 | `CONTRACTSERVICE_I_EVM_HOOK_STATES.protoOrdinal()` | `EVM_HOOK_STATES` | `HookId.PROTOBUF` | `EvmHookState.PROTOBUF` | Historical hook metadata |
| 0.65.0 | `CONTRACTSERVICE_I_EVM_HOOK_STORAGE.protoOrdinal()` | `LAMBDA_STORAGE` | `EvmHookSlotKey.PROTOBUF` | `SlotValue.PROTOBUF` | Historical hook storage |

`CONFIRMED`: `LAMBDA_STORAGE` cannot be renamed without changing persisted state
identity. No key, ID, codec, protobuf field, or semantic version changes in P06A.

`CONFIRMED`: The generated PBJ definitions live in `hapi` and have no Besu dependency.
The state IDs are also represented in `virtual_map_state.proto` and block-stream state
change oneofs.

## Migration and activation

- `V0490ContractSchema` creates the modularized storage and bytecode maps. It declares
  no migration callback.
- `V065ContractSchema` adds hook metadata and hook storage. It declares no migration
  callback.
- `ContractService.migrationOrder()` remains `-1`, preserving migration before token
  service migrations that inspect contract storage links.
- `OPEN`: The irreversible Nitrograph migration activation software version and round
  are intentionally not selected in P06A.
- `OPEN`: Physical state freezing and eventual removal require authenticated
  pre-migration saved-state fixtures and a later migration gate.

## Current consumers

| Boundary | Confirmed consumers | P06A disposition |
|---|---|---|
| Startup/schema lifecycle | `Hedera`, `ServicesRegistry`, `ContractServiceImpl` | Native binds `HistoricalContractStateService`; full binds executable service |
| Dagger composition | `HederaInjectionComponent`, `ExecutorComponent`, `HandleWorkflowModule`, `FacilityInitModule`, `QueryWorkflowInjectionModule` | Shared graph binds `ContractRuntimeProvider`, not `ContractServiceImpl` |
| Transaction dispatch | `TransactionHandlers`, `TransactionDispatcher` | Contract slots use SPI `TransactionHandler`; P05 rejects disabled bodies first |
| Ethereum-only lifecycle | `DispatchProcessor`, `HollowAccountCompletions` | Uses neutral `EthereumTransactionHandlerFacade`; historical implementation always rejects |
| Query dispatch | `QueryHandlers`, `QueryDispatcher` | Contract slots use SPI `QueryHandler`; historical implementation has no executable query behavior |
| State access | `ReadableContractStateStore`, `WritableContractStateStore`, hook stores | Remain in executable implementation for now; retained schemas moved out |
| Reconnect/replay/synchronization | Generic state lifecycle keyed by service/state IDs | Identifiers and codecs unchanged; fixture validation remains required |
| Record streams | Contract stream builders and historical sidecar protobufs | No format changes; executable builders remain in full implementation |
| Block streams | `BlockItemsTranslator`, state-change protobuf oneofs | No format changes; Besu-coupled translator extraction remains |
| Standalone execution | `TransactionExecutors` | Explicitly binds full runtime; native node never selects it |
| Mirror node | Contract results, bytecode, logs, state changes, sidecars | Wire formats unchanged; historical fixture ingestion remains required |
| SDKs | Java/JS/Python/Go/Rust contract and Ethereum APIs | API surface unchanged; native submission rejection remains `INVALID_TRANSACTION_BODY` |

## Executable handlers separated from shared records

The following full-runtime handlers are now exposed to shared composition only through
SPI interfaces: contract create, update, call, delete, system delete, system undelete,
Ethereum transaction, hook store, hook dispatch, contract call local, solidity-ID lookup,
contract info, bytecode, and records.

The historical provider exposes a single fail-closed implementation for these slots.
It cannot initialize metrics, verify contract native libraries, construct Dagger's
contract component, load Besu, or mutate retained contract state.

## Direct executable dependency boundary

`CONFIRMED`: `hedera-smart-contract-service-impl` still owns Besu/EVM execution,
system contracts, Solidity execution, executable stores, handlers, calculators, and
contract stream builders. It directly requires Headlong, Besu datatypes, Besu EVM,
Tuweni bytes/units, Besu crypto, and contract-adjacent native libraries.

`CONFIRMED`: `hedera-smart-contract-service` now owns only the service API and retained
historical schemas. Its production module descriptor requires HAPI, PBJ, application
SPI, and state lifecycle APIs; it has no Besu, Tuweni, Headlong, EVM, Solidity,
system-contract, or executable-handler dependency.

## Compatibility fixtures

| Fixture | Availability | Required assertion |
|---|---|---|
| Clean genesis | Existing P05 smoke baseline | Native startup and processing |
| State with no contracts | Existing saved-state baseline | Restart and native processing |
| Contract accounts | `DiverseStateCreation` / `DiverseStateValidation` suite | Account metadata remains readable |
| Bytecode | Same diverse-state suite and `BYTECODE` map fixtures | Exact PBJ decoding |
| Contract storage | Contract state-store tests and saved-state fixtures | Exact slot-key/value decoding |
| Historical logs/records | Contract record/sidecar test fixtures | Record interpretation |
| Hook states/storage | HIP-1195 repeatable integration fixtures | Exact 0.65 map decoding |
| Reconnect/replay/state sync | Existing platform state fixtures | No state-ID or codec drift |
| Mirror ingestion | Mirror contract-result/sidecar integration fixtures | Historical ingestion without execution |

`OPEN`: A single authenticated pre-migration fixture set spanning every retained map,
record sidecar, block item, reconnect boundary, and mirror ingestion path must be
published before physical implementation removal.

## Remaining extraction sequence

1. Move read-only store interfaces/implementations needed for inspection out of the
   executable module without exposing mutation.
2. Remove Besu types from `BlockItemsTranslator` and shared stream boundaries.
3. Validate authenticated saved-state, replay, reconnect, state-sync, record, block,
   and mirror fixtures.
4. Only then remove the executable implementation from native compilation and packaging.

No `platform-sdk` or consensus source is changed by this slice.
