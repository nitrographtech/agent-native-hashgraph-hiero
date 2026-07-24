# P07 Protected Compatibility Inventory

Status: permanent protection list  
Base: `p06b/native-packaging-isolation-implementation@b487cd23bab93a6fc84c435a82f2d300ec58d83e`

The following assets survive repository de-execution. An item having an EVM-era or contract-era name
does not make it executable or removable.

## Persisted state

| Asset | Required invariant |
|---|---|
| Service name `ContractService` | Exact spelling and registration identity retained |
| `STORAGE` | State key, state ID, `SlotKey`/`SlotValue` codecs, and values retained |
| `BYTECODE` | State key, state ID, `ContractID`/`Bytecode` codecs, and values retained |
| `EVM_HOOK_STATES` | State key, state ID, codecs, and values retained |
| `LAMBDA_STORAGE` | Historical literal, state ID, codecs, and values retained |
| V0.49 schema | Version, state definitions, ordering, and identifiers retained |
| V0.65 schema | Version, state definitions, ordering, and identifiers retained |
| Saved-state roots and signatures | Readable and verifiable without migration or execution |

The authoritative regression cardinalities remain `STORAGE=1`, `BYTECODE=2`,
`EVM_HOOK_STATES=1`, and `LAMBDA_STORAGE=1`; the retained storage value is `424242`.

## Neutral API and runtime boundary

- `ContractService` and `HistoricalContractStateService`.
- `ReadableContractStateStore`.
- `HistoricalReadableContractStateStore`.
- `HistoricalReadableEvmHookStore`.
- `HistoricalContractKeyUtils`.
- Six stream-builder interfaces in their retained legacy fully qualified packages:
  `ContractCallStreamBuilder`, `ContractCreateStreamBuilder`, `ContractDeleteStreamBuilder`,
  `ContractOperationStreamBuilder`, `ContractUpdateStreamBuilder`, and
  `EthereumTransactionStreamBuilder`.
- `HistoricalContractRuntimeProvider` and factory.
- `HistoricalContractStoreFactory`.
- Fail-closed legacy transaction and query handlers.
- `INVALID_TRANSACTION_BODY` for all five retained executable transaction bodies.

## Historical data and translation

- All contract and Ethereum PBJ/protobuf transaction bodies needed for decoding and rejection.
- Contract function results, logs, actions, state changes, storage changes, bytecode sidecars, and
  Ethereum-format historical output models.
- Block stream inputs and outputs carrying historical contract data.
- Record stream builders and parent/child transaction association.
- Sidecar ordering, transaction association, one-of presence, null/empty distinctions, address and
  hash widths, unsigned numeric bit patterns, and original/written storage value semantics.
- Neutral values: `HistoricalLog`, `HistoricalContractResult`, `HistoricalContractAction`,
  `HistoricalContractStateChanges`, `HistoricalStorageChange`, and
  `HistoricalContractBytecode`, including their supporting value objects.
- Official mirror record, sidecar, and block ingestion compatibility.

## Lifecycle and evidence

- Authenticated P06A fixture releases, tags, manifests, retrieval scripts, public compromised test
  identity descriptor, checksums, and generation evidence.
- Historical load, startup, native processing, save, restart, replay, signed-state load, reconnect,
  and state synchronization gates.
- Map-specific key, value, cardinality, codec, and semantic fingerprints.
- P06B golden translation evidence and mirror semantic fingerprints.
- Native binary policy proving executable providers and classes remain unavailable.

## Explicitly not protected as executable behavior

- `ContractServiceImpl`.
- Full-runtime provider/store factories.
- Executable handlers and queries.
- Besu/Tuweni/EVM engine integrations.
- Mutable world-state implementations.
- System-contract execution.
- Standalone execution.
- Solidity compiler/runtime fixtures after the final reproducible corpus is frozen.
- Executable tracers.

