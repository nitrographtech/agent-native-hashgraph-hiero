# P06B Contract Compatibility API Ownership

## Decision

The existing `app-service-contract` artifact is the neutral compatibility API owner. Creating a
second Gradle project would duplicate the already-correct API dependency direction. Legacy Java
packages are retained where callers and record builders already reference their fully qualified
names.

| Type or family | Original owner | P06B-7A owner | Classification | Package/bridge decision |
|---|---|---|---|---|
| `ContractOperationStreamBuilder` | `app-service-contract-impl` | `app-service-contract` | HISTORICAL_COMPATIBILITY_INTERFACE | FQCN retained; no bridge |
| create/call/update/delete/Ethereum stream builders | `app-service-contract-impl` | `app-service-contract` | NEUTRAL_API_CANDIDATE | FQCN retained; no bridge |
| `ReadableContractStateStore` contract | implementation concrete class | `app-service-contract` interface | HISTORICAL_COMPATIBILITY_INTERFACE | new neutral package; full class implements it |
| `HistoricalReadableContractStateStore` | none | `app-service-contract` | HISTORICAL_COMPATIBILITY_INTERFACE | direct retained-map adapter |
| `HistoricalReadableEvmHookStore` | none | `app-service-contract` | HISTORICAL_COMPATIBILITY_INTERFACE | direct retained-map adapter |
| `HistoricalContractKeyUtils` | implementation writable store | `app-service-contract` | DATA_MODEL_ONLY | generic key minimization only |
| implementation `ReadableContractStateStore` | `app-service-contract-impl` | unchanged | EXECUTABLE_IMPLEMENTATION | implements neutral API |
| `WritableContractStateStore` | `app-service-contract-impl` | unchanged | EXECUTABLE_IMPLEMENTATION | full runtime only |
| `WritableEvmHookStore` | `app-service-contract-impl` | unchanged | EXECUTABLE_IMPLEMENTATION | full runtime only |
| executable sidecar collectors/tracers | `app-service-contract-impl` | unchanged | EXECUTABLE_IMPLEMENTATION | full-runtime producer only |
| PBJ contract records, actions, state changes, bytecode | HAPI/PBJ | unchanged | DATA_MODEL_ONLY | wire packages unchanged |
| `FullContractRuntimeProvider` | combined app | full app artifact only | FULL_RUNTIME_ONLY | excluded from native jar |
| `TransactionExecutors` and standalone implementations | combined app | standalone/full artifact only | STANDALONE_ONLY | excluded from native jar |

## Signature audit

The six relocated builder interfaces expose HAPI/PBJ, application SPI, token-service child-builder,
and neutral historical-log types. They expose no Besu, Tuweni, `MessageFrame`, world-state,
system-contract, or executable provider type.

The prior executable default method accepting `CallOutcome` was not moved into the API.
`CallOutcome` now applies its own common fields to the neutral builder contract inside the full
implementation module.

## Persisted and binary compatibility

- All six builder FQCNs are unchanged.
- No duplicate class is emitted; ownership moves from one jar to the other.
- No service name, state key, state ID, schema version, codec, PBJ/protobuf type, or wire field
  changes.
- The retained-map adapters use the existing V0.49 and V0.65 state IDs directly.
