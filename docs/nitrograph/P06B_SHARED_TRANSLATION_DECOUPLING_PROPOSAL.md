# P06B — Shared Translation Decoupling Proposal

P06B begins only after the remaining P06A reconnect and synchronization gate is complete.
It does not remove physical EVM artifacts.

## Neutral boundary

Introduce implementation-neutral historical translation values for:

- log address, topics, and data;
- contract result status, gas, bloom, output, and created-contract identity;
- storage changes and sidecar actions needed for record and block interpretation;
- Ethereum-format transaction result metadata.

`BlockItemsTranslator` and shared record translation consume these values instead of
`org.hyperledger.besu.evm.log.Log`. Adapters remain in the standalone executable contract
runtime and convert Besu/Tuweni values at that boundary.

## Dependency sequence

1. Inventory every Besu/Tuweni type crossing from contract implementation modules into
   `hedera-app`, record translation, block translation, stream writers, and test fixtures.
2. Add neutral values and byte-preserving adapters with parity tests over the authenticated
   P06A streams.
3. Move `BlockItemsTranslator` log/result inputs to the neutral boundary.
4. Move shared record translation and sidecar interpretation to the same boundary.
5. Keep full-runtime `ContractServiceImpl` composition and adapters intact.
6. Prove native startup, replay, reconnect, synchronization, and mirror interpretation again.

## Remaining full-runtime coupling

- executable handler composition and standalone transaction execution;
- `ContractServiceImpl` registration in full distributions and state-validator workflows;
- Besu world-updater, frame, tracer, and processor paths;
- Tuweni word/address representations within executable storage adapters;
- full-runtime contract test fixtures.

These remain full-runtime-only until a later physical-removal slice.

## Physical-removal gate

Physical removal is permitted only after:

- P06A map integrity, reconnect, synchronization, signed-state, and mirror gates pass;
- shared record/block translation has no Besu/Tuweni API surface;
- native distribution class-load proof remains free of executable EVM classes;
- all legacy bodies remain `INVALID_TRANSACTION_BODY`;
- standalone full-runtime parity tests continue to pass;
- dependency reports show no native path to executable contract artifacts;
- licensing and binary-distribution inventories are updated.

No persisted key, state ID, codec, schema version, consensus, or platform-sdk change is
part of P06B.

## P06A handoff findings

- A real four-node reconnect transferred and validated a signed state containing non-empty
  `STORAGE`, `BYTECODE`, `EVM_HOOK_STATES`, and `LAMBDA_STORAGE`; the normalized per-map
  content fingerprint was unchanged before and after transfer.
- Four equal-weight nodes are the minimum local topology for isolating one node while
  retaining the platform's greater-than-two-thirds consensus threshold.
- Reconnect used the serialized virtual-map boundary and did not require contract execution.
- The mirror importer remains the final external compatibility gate. Its PostgreSQL and
  Redis Testcontainers infrastructure must be available before mirror findings can refine
  the neutral log/result boundary.
- P06B must retain the distinction between prohibited executable EVM classes and legacy
  Besu/Tuweni data-model types still used by record and block translation.
