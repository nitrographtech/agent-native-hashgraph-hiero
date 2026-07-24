# P06B Distribution Dependency Matrix

Base: `agent-native@1ffddab00991a695acb62fa5ae7e8fdadfcf359e`

| Module/artifact | Native node | Full node | Standalone | Fixture generation | Tests | Historical compatibility | Stream/mirror role | Required action |
|---|---|---|---|---|---|---|---|---|
| `app-service-contract` | yes | yes | yes | yes | yes | schemas, state keys, neutral builders/stores | schema and stream interpretation | RETAIN |
| `app-service-contract-impl` | packaged pending P06B-7, not required by exclusion probe | yes | yes | yes | yes | none in native | executable producer | REMOVE_FROM_NATIVE |
| native `HederaNode.jar` | yes | no | no | no | yes | neutral translation/orchestration | records/blocks | RETAIN |
| full `HederaNode.jar` | no | yes | yes | yes | yes | neutral translation | records/blocks | RETAIN_FOR_FULL |
| `FullContractRuntimeProvider` | absent from native app jar | yes | yes | yes | yes | none | none | REMOVE_FROM_NATIVE_CLASSPATH |
| `TransactionExecutors` | absent from native app jar | optional | yes | optional | yes | none | none | RETAIN_FOR_STANDALONE |
| neutral contract record builders | yes | yes | yes | yes | yes | data-only ABI | records/blocks | RETAIN |
| historical read-only state adapters | yes | yes | yes | yes | yes | retained map access | state compatibility | RETAIN |
| Besu EVM/datatypes | currently yes | yes | yes | yes | yes | none after neutralization | none | REMOVE_FROM_NATIVE |
| Tuweni bytes/units | currently yes | yes | yes | yes | yes | remaining API bridges | none | SPLIT_MODULE then REMOVE |
| Besu native common | currently yes | yes | yes | yes | yes | none | none | REMOVE_FROM_NATIVE |
| secp256k1 native binding | currently yes | yes | yes | yes | yes | possibly generic signature verification | historical Ethereum metadata | RETAIN_TEMPORARILY_WITH_JUSTIFICATION |
| PBJ/HAPI models | yes | yes | yes | yes | yes | required | records/sidecars/blocks | RETAIN |
| neutral `Historical*` values | yes | yes | yes | yes | yes | required | records/sidecars/blocks | RETAIN |
| mirror importer | no | no | no | no | external tests | interpretation gate | official ingestion | EXTERNAL |

## Exact blocking edges

1. RESOLVED: record builders retain their packages but are owned by `app-service-contract`.
2. RESOLVED: shared builders use neutral retained-map/key interfaces.
3. RESOLVED: native store construction uses historical read-only adapters.
4. REMAINING FOR P06B-7: the full module descriptor and runtime classpath retain executable
   dependencies.
5. RESOLVED: the native application jar omits full-provider and standalone classes.

## Required split

The API/application prerequisite is complete. P06B-7 may now remove implementation/Besu/Tuweni/EVM
dependencies from the native runtime configuration, add a native-specific module descriptor, and
repeat the full binary and lifecycle gates.
