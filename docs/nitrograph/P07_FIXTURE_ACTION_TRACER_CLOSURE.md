# P07 Fixture Action-Tracer Closure

Status: **ISOLATED**  
Wave: P07-3A

## Measured closure

The authenticated P06A generator needs four concrete production types to create ordered PBJ
`ContractAction` values:

| Type | Former owner | P07-3A owner | Purpose | Disposition |
|---|---|---|---|---|
| `EvmActionTracer` | `app-service-contract-impl` | `fixture-tooling` | Converts execution callbacks into PBJ actions | MOVE_TO_FIXTURE_EXECUTION_SUPPORT |
| `ActionStack` | `app-service-contract-impl` | `fixture-tooling` | Maintains action hierarchy, depth, revert, and finalization | MOVE_TO_FIXTURE_EXECUTION_SUPPORT |
| `ActionWrapper` | `app-service-contract-impl` | `fixture-tooling` | Holds action plus frame validation state | MOVE_TO_FIXTURE_EXECUTION_SUPPORT |
| `ActionsHelper` | `app-service-contract-impl` | `fixture-tooling` | Validates and diagnoses PBJ actions | MOVE_TO_FIXTURE_EXECUTION_SUPPORT |
| `FixtureActionTracerFactory` | new | `fixture-tooling` | Creates one collector per fixture transaction | FIXTURE_ACTION_TRACING_REQUIRED |

The isolated implementation is 33,234 source bytes. It depends on the full executable contract
implementation's callback interface, `MessageFrame`, PBJ `ContractAction`, and execution metadata.
It does not own or duplicate an EVM, world state, `ContractServiceImpl`, system contracts, record
translation, or historical interpretation.

## Runtime boundary

`ActionSidecarContentTracerFactory` is the only runtime-facing SPI. `ContractServiceImpl` discovers
it with its defining class loader. No provider means the normal full runtime receives `NoTracer`;
exactly one tooling provider means the transient fixture full node receives a fresh collector per
transaction. Multiple providers fail closed.

```text
fixture-tooling
  -> FixtureActionTracerFactory
  -> EvmActionTracer + ActionStack
  -> app-service-contract-impl callback SPI
  -> pinned full-node execution
  -> PBJ ContractAction

native/full runtime distributions
  -> no fixture-tooling jar
  -> no fixture tracer provider
  -> no fixture tracer implementation
```

No class is duplicated under the same fully qualified name. The moved classes use the tooling
package `com.hedera.services.bdd.fixturetooling.tracing`; no reflection, service metadata, script,
or persisted artifact required their former names.

## Output ownership

- Actions require the isolated live tracer.
- State/storage changes remain independently produced from `TxStorageUsage`.
- Bytecode remains independently produced by creation/transaction processors.
- PBJ and neutral historical readers are unchanged.
- Existing records, sidecars, blocks, and mirror rows remain tracer-free to interpret.

The normal full-runtime action-sidecar output is intentionally disabled after this split. Contract
execution, contract results, state/storage changes, and bytecode production remain owned by the
full runtime pending resumed P07-3.

## Reproducibility result

The transient augmented full-node distribution ran
`:fixture-tooling:populateP06aHistoricalContractState` with explicit fixture-only sidecar and hook
configuration. The complete run produced:

- 3 ordered actions: CREATE, CALL, CREATE;
- 3 action sidecar records;
- 2 state-change sidecar records;
- 2 bytecode sidecar records;
- 7 total sidecar records;
- semantic sidecar SHA-256
  `318284b758f4734c4b74580c4c8edf688e17264d1def624f93685b162affe15f`.

The run was transient and unsigned for publication purposes. It did not replace, sign, publish, or
modify the authoritative fixture, release, tag, identity, or provenance.
