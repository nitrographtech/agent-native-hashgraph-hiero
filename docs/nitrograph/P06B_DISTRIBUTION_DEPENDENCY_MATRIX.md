# P06B Distribution Dependency Matrix

Base: `agent-native@2ea4c4b88c671ab7402dc558b6517033ad450eb5`

| Module/artifact | Native node | Full node | Standalone | Fixture generation | Tests | Historical compatibility | Stream/mirror role | Required action |
|---|---|---|---|---|---|---|---|---|
| `app-service-contract` | yes | yes | yes | yes | yes | schemas/state keys | schema interpretation | RETAIN |
| `app-service-contract-impl` | currently yes | yes | yes | yes | yes | record/store interfaces only | builder ABI | SPLIT_MODULE |
| `hedera-app` combined jar | yes | yes | yes | yes | yes | neutral translation | records/blocks | SPLIT_CONFIGURATION/SPLIT_MODULE |
| `FullContractRuntimeProvider` | packaged, unreachable | yes | yes | yes | yes | none | none | REMOVE_FROM_NATIVE |
| `TransactionExecutors` | packaged, unreachable | optional | yes | optional | yes | none | none | REMOVE_FROM_NATIVE |
| contract implementation record builders | yes | yes | yes | yes | yes | data-only ABI | records/blocks | MOVE TO NEUTRAL API |
| contract implementation state stores | yes | yes | yes | yes | yes | retained map access | state compatibility | MOVE TO COMPATIBILITY API |
| Besu EVM/datatypes | currently yes | yes | yes | yes | yes | none after neutralization | none | REMOVE_FROM_NATIVE |
| Tuweni bytes/units | currently yes | yes | yes | yes | yes | remaining API bridges | none | SPLIT_MODULE then REMOVE |
| Besu native common | currently yes | yes | yes | yes | yes | none | none | REMOVE_FROM_NATIVE |
| secp256k1 native binding | currently yes | yes | yes | yes | yes | possibly generic signature verification | historical Ethereum metadata | RETAIN_TEMPORARILY_WITH_JUSTIFICATION |
| PBJ/HAPI models | yes | yes | yes | yes | yes | required | records/sidecars/blocks | RETAIN |
| neutral `Historical*` values | yes | yes | yes | yes | yes | required | records/sidecars/blocks | RETAIN |
| mirror importer | no | no | no | no | external tests | interpretation gate | official ingestion | EXTERNAL |

## Exact blocking edges

1. `RecordStreamBuilder` implements
   `contract.impl.records.ContractCreateStreamBuilder` and sibling interfaces.
2. `BlockStreamBuilder` and `PairedStreamBuilder` implement the same implementation-owned record
   interfaces and reference implementation-owned EVM hook stores.
3. application store factories construct implementation-owned contract store adapters required to
   expose retained historical maps.
4. `module-info.java` has transitive requirements on the contract implementation and Besu modules.
5. the combined `HederaNode.jar` contains both `FullContractRuntimeProvider` and the standalone
   executor packages.

## Required split

The minimum safe prerequisite is a separately reviewed API extraction:

- move record-builder interfaces into the contract API or a historical compatibility API;
- move read-only retained-map store interfaces/adapters out of the executable implementation;
- provide native and full application source sets or jars so full-provider and standalone classes
  are absent from the native artifact;
- then remove implementation/Besu/Tuweni/EVM dependencies from the native runtime configuration.

Filtering jar names alone is unsafe and is prohibited by this result.

