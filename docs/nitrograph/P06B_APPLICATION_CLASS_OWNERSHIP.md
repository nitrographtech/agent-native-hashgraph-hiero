# P06B Application Class Ownership

P06B-7A creates separate native and full application jar outputs without duplicating the common
application source tree. Physical dependency-jar removal remains P06B-7.

| Class/family | Artifact |
|---|---|
| `Hedera`, `HederaNode`, `ServicesMain`, platform lifecycle | COMMON_APP |
| accounts, native assets, Coordination Layer, records, blocks | COMMON_APP |
| neutral historical builders and translators | COMMON_APP / contract compatibility API |
| `HistoricalContractRuntimeProvider` | NATIVE_APP |
| `HistoricalContractRuntimeProviderFactory` | NATIVE_APP |
| `HistoricalContractStateService` and retained schemas/adapters | NATIVE_APP / contract compatibility API |
| fail-closed legacy handlers | NATIVE_APP / contract compatibility API |
| `FullContractRuntimeProvider` and factory | FULL_RUNTIME_APP |
| `FullContractStoreFactory` | FULL_RUNTIME_APP |
| `ContractServiceImpl` and executable handlers/system contracts | FULL_RUNTIME_APP / contract implementation |
| `TransactionExecutors`, standalone workflow implementations | STANDALONE_TOOLING |
| fixture-generation entry points | TEST_FIXTURE |

## Artifact mechanics

- The normal `app` jar remains the full executable application jar.
- `nativeAppJar` emits a distinct native application jar from common/native classes and filters
  full-provider, full-store, standalone, and embedded executable implementation classes.
- The native distribution installs `nativeAppJar` as `HederaNode.jar`.
- The full distribution installs the normal application jar.
- Profile factories are discovered through separate service metadata. Native metadata contains
  only historical runtime/store factories; full metadata contains both and selects full stores by
  priority.
- The native jar omits the combined JPMS descriptor because that descriptor provides both profiles.
  A native-specific module descriptor and executable dependency-jar removal remain P06B-7 work.

No class is assigned to multiple application jars as an ownership source; common classes are a
shared source output deliberately packaged in both profile artifacts.
