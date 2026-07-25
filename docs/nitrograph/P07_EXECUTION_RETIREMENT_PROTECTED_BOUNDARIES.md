# P07 Execution Retirement Protected Boundaries

P07-8 does not change the `ContractService` persisted name; STORAGE, BYTECODE,
EVM_HOOK_STATES, or LAMBDA_STORAGE keys; state IDs; codecs; V0.49/V0.65 schema versions;
registration order; or persisted values.

The following remain permanent:

- PBJ/protobuf contract and Ethereum models;
- the six neutral contract stream-builder interfaces under their existing FQNs;
- historical runtime/store factories and read-only adapters;
- neutral result, log, action, state/storage-change, and bytecode values;
- record, sidecar, block, and mirror associations;
- Network Accounts, Native Asset Service, schedules, Coordination Layer, native records;
- generic/platform cryptography, signatures, and state hashing;
- deterministic rejection of legacy executable bodies.

Historical adapters remain read-only. No executable behavior may move into them.
## Fixture boundary

The authenticated round-4744 state, its one-node roster, manifest, PCES, hashes, and publication
identity are protected as published. They must not be rewritten into a four-node or post-write
fixture merely to satisfy P07-8 validation.
