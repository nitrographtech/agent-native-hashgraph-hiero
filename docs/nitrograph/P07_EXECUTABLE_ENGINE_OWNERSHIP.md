# P07 Executable Engine Ownership

## Retired closure

The retired production closure was owned by `app-service-contract-impl` and `hedera-app`.

| Area | Representative ownership | P07-8 result |
| --- | --- | --- |
| Handlers and queries | contract create/call/update/delete and paid query handlers | `DELETE_IN_P07_8` |
| Processors | context transaction/query, transaction components, FrameRunner | `DELETE_IN_P07_8` |
| EVM | HederaEVM, HEVM, Bonneville, message/create processors, custom operations | `DELETE_IN_P07_8` |
| World state | root/proxy updater, frame state, mutable/proxy accounts | `DELETE_IN_P07_8` |
| Stores/storage | writable store, journals, TxStorageUsage, rent, slot validation | `DELETE_IN_P07_8` |
| Results/tracing | executable result construction, callback seam, NoTracer | `DELETE_IN_P07_8` |
| Composition | ContractServiceImpl, full providers/stores, Dagger and JPMS edges | `DELETE_IN_P07_8` |
| Schemas | V0.49 and V0.65 neutral historical implementations | `RETAIN_PERMANENTLY` |
| Stream builders | six neutral interfaces retaining legacy FQNs | `NEUTRAL_API` |
| PBJ/protobuf | contract and Ethereum data/wire models | `RETAIN_PERMANENTLY` |

## Dependency graph

```text
contract body/query
  -> executable handler
  -> transaction/query component
  -> HederaEVM / FrameRunner / message processors
  -> RootProxyWorldUpdater / ProxyWorldUpdater / EvmFrameState
  -> mutable account + writable STORAGE/BYTECODE
  -> result + live tracer callbacks
```

Every node in this production graph is removed. The reverse graph consisted of the full runtime
provider/store factories, application JPMS/service composition, contract implementation tests,
ordinary contract HAPI suites, and the former in-tree fixture generator. The fixture consumer is
now satisfied solely by the immutable release.

## Measured result

`app-service-contract-impl` now has five production files: two deprecated schema forwarding
classes, two test-client conversion utilities, and a minimal module descriptor. These are
non-runtime residue for P07-9. No handler, processor, mutable world-state class, executable account,
executable store, or tracer callback remains.
## Validation ownership after retirement

The production engine remains deleted. The exact-head activation probe proved that restoring it
would only mask an authenticated-fixture ownership mismatch: the immutable release has a
one-node roster and does not contain the required post-write storage state. Resolving that mismatch
requires separate authority over fixture publication, not production engine ownership.
