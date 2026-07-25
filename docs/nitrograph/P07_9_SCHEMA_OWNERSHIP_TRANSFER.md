# P07-9 Schema Ownership Transfer

## Result

The two deprecated historical schema forwarders moved physically from the
deleted `app-service-contract-impl` project into `app-service-contract`.
Their packages, class names, public constructors, inheritance, schema versions,
and inherited state definitions did not change.

| Schema | Old path | New path | FQN | Version and state ownership |
| --- | --- | --- | --- | --- |
| V0.49 | `hedera-node/hedera-smart-contract-service-impl/src/main/java/com/hedera/node/app/service/contract/impl/schemas/V0490ContractSchema.java` | `hedera-node/hedera-smart-contract-service/src/main/java/com/hedera/node/app/service/contract/impl/schemas/V0490ContractSchema.java` | `com.hedera.node.app.service.contract.impl.schemas.V0490ContractSchema` | Extends neutral `history.V0490ContractSchema`; version 0.49.0; inherited `STORAGE` and `BYTECODE` registrations, IDs, keys, and codecs. |
| V0.65 | `hedera-node/hedera-smart-contract-service-impl/src/main/java/com/hedera/node/app/service/contract/impl/schemas/V065ContractSchema.java` | `hedera-node/hedera-smart-contract-service/src/main/java/com/hedera/node/app/service/contract/impl/schemas/V065ContractSchema.java` | `com.hedera.node.app.service.contract.impl.schemas.V065ContractSchema` | Extends neutral `history.V065ContractSchema`; version 0.65.0; inherited `EVM_HOOK_STATES` and `LAMBDA_STORAGE` registrations, IDs, keys, and codecs. |

## Reverse consumers

- `hedera-state-validator` consumes the V0.49 `BYTECODE_STATE_ID`.
- `test-clients` consumes V0.65 hook state IDs from `EmbeddedVerbs`,
  `LambdaplexVerbs`, and `Hip1195BasicTests`.
- No production runtime selects either forwarder as an executable service.
- Historical registration continues through `HistoricalContractStateService`
  and the neutral parent schemas.

`app-service-contract` now exports the legacy schema package to the same
qualified consumers formerly listed by the implementation module. The
state-validator and test-client JPMS graphs now require only
`com.hedera.node.app.service.contract`.

## Compatibility proof

`HistoricalContractStateServiceTest` now loads both old FQNs reflectively,
asserts their neutral superclasses, constructs each through its public
constructor, and retains the existing version/state-key assertions.

The transfer changes source ownership only. It does not change:

- `ContractService` persisted service name;
- schema registration order;
- schema versions;
- state IDs or keys;
- codecs;
- migrations;
- PBJ/protobuf definitions;
- saved-state interpretation.
