# P06B Distribution Dependency Matrix

Base: `agent-native@1ffddab00991a695acb62fa5ae7e8fdadfcf359e`

| Module/artifact | Native node | Full node | Standalone | Fixture generation | Tests | Historical compatibility | Stream/mirror role | Required action |
|---|---|---|---|---|---|---|---|---|
| `app-service-contract` | yes | yes | yes | yes | yes | schemas, state keys, neutral builders/stores | schema and stream interpretation | RETAIN |
| `app-service-contract-impl` | no | yes | yes | yes | yes | none in native | executable producer | REMOVED_FROM_NATIVE |
| native `HederaNode.jar` | yes | no | no | no | yes | neutral translation/orchestration | records/blocks | RETAIN |
| full `HederaNode.jar` | no | yes | yes | yes | yes | neutral translation | records/blocks | RETAIN_FOR_FULL |
| `FullContractRuntimeProvider` | absent from native app jar | yes | yes | yes | yes | none | none | REMOVE_FROM_NATIVE_CLASSPATH |
| `TransactionExecutors` | absent from native app jar | optional | yes | optional | yes | none | none | RETAIN_FOR_STANDALONE |
| neutral contract record builders | yes | yes | yes | yes | yes | data-only ABI | records/blocks | RETAIN |
| historical read-only state adapters | yes | yes | yes | yes | yes | retained map access | state compatibility | RETAIN |
| Besu EVM/datatypes | no | yes | yes | yes | yes | none after neutralization | none | REMOVED_FROM_NATIVE |
| Tuweni bytes/units | no | yes | yes | yes | yes | none after neutralization | none | REMOVED_FROM_NATIVE |
| Besu native common/precompiles | no | yes | yes | yes | yes | none | none | REMOVED_FROM_NATIVE |
| Besu secp256k1 native binding | no | yes | yes | yes | yes | none | none | REPLACED_IN_NATIVE |
| neutral secp256k1 verifier | yes | no | no | no | yes | generic account signatures | none | NATIVE_REQUIRED |
| Headlong ABI/RLP data utility | yes | yes | yes | yes | yes | Ethereum-format data decoding | historical streams/token hooks | RETAIN_WITH_JUSTIFICATION |
| PBJ/HAPI models | yes | yes | yes | yes | yes | required | records/sidecars/blocks | RETAIN |
| neutral `Historical*` values | yes | yes | yes | yes | yes | required | records/sidecars/blocks | RETAIN |
| mirror importer | no | no | no | no | external tests | interpretation gate | official ingestion | EXTERNAL |

## Exact blocking edges

1. RESOLVED: record builders retain their packages but are owned by `app-service-contract`.
2. RESOLVED: shared builders use neutral retained-map/key interfaces.
3. RESOLVED: native store construction uses historical read-only adapters.
4. RESOLVED: the native application and filtered native base-crypto jars omit combined module
   descriptors; retained module descriptors have no Besu/Tuweni/EVM edge.
5. RESOLVED: the native application jar omits full-provider and standalone classes.

## Required split

P06B-7 removes fourteen executable-only artifacts and 35,516,645 bytes from native packaging.
The full distribution retains every removed artifact. The native-only base-crypto artifact preserves
generic account-signature verification through Bouncy Castle without modifying platform source.
