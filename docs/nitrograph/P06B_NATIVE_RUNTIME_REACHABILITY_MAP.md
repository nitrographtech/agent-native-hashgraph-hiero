# P06B Native Runtime Reachability Map

Status: authoritative after P06B-5  
Base: `agent-native@15e912e668df1431e3fbdc6f7443c23eeb21fdaf`

## Method

This inventory separates source reachability, construction reachability, and packaging. A jar being
packaged does not prove it is loaded; a class not being loaded does not prove it is absent from the
compile or packaging graph. The P06B-6 policy checks all native startup and shared-translation source
boundaries, while the packaging inventory remains a deferred physical-removal input.

## Production reachability

| Dependency | Source/module | Dependency type | Native | Full | Standalone/fixture | Compatibility need | Behavior | Classification | Removal phase | Coverage |
|---|---|---|---|---|---|---|---|---|---|---|
| `HistoricalContractRuntimeProvider` | `hedera-app` | startup provider | selected | no | no | schemas and rejection | read-only | HISTORICAL_COMPATIBILITY_ONLY | retain | provider, activation, reconnect |
| `HistoricalContractStateService` | `hedera-smart-contract-service` | schema service | selected | no | no | V0.49/V0.65 state | read-only | NATIVE_REACHABLE_DATA_ONLY | retain | map integrity |
| `FullContractRuntimeProvider.create` | `hedera-app` | explicit factory | branch-excluded | selected | optional | none | executable construction | FULL_RUNTIME_ONLY | DEFER_TO_PHYSICAL_REMOVAL | full build |
| `ContractServiceImpl` | contract implementation | implementation | not constructed | constructed | constructed | fixture generation only | executable | FULL_RUNTIME_ONLY / FIXTURE_GENERATION_ONLY | DEFER_TO_PHYSICAL_REMOVAL | full and fixture tests |
| standalone `TransactionExecutors` | `hedera-app` | direct construction | not on node startup path | optional | selected | none | executable | STANDALONE_ONLY | DEFER_TO_MODULE_CLEANUP | standalone tests |
| contract record-builder interfaces | contract implementation | Java interfaces | compile reachable | reachable | reachable | legacy record/block ABI | data only | NATIVE_REACHABLE_DATA_ONLY | DEFER_TO_MODULE_CLEANUP | builder suites |
| contract state-store interfaces | contract implementation | store adapters | compile reachable | reachable | reachable | retained maps | state access, no execution | HISTORICAL_COMPATIBILITY_ONLY | DEFER_TO_MODULE_CLEANUP | map tests |
| `EntityAddressUtils` | `hedera-app` authorization | byte utility | reachable | reachable | reachable | native alias checks | neutral bytes | REMOVABLE_IN_P06B_6 completed | retain neutral | golden unit vectors |
| hapi-utils secp256k1 helpers | `hapi-utils` | native crypto | transitively packaged | executable handler use | fixture use | historical signature metadata | crypto, not EVM engine | NATIVE_REACHABLE_DATA_ONLY | DEFER_TO_MODULE_CLEANUP | existing crypto tests |
| `HederaExceptionalHaltReason` | `hapi-utils` | Besu enum bridge | compile/package reachable | reachable | reachable | legacy error mapping | data enum | NATIVE_REACHABLE_DATA_ONLY | DEFER_TO_MODULE_CLEANUP | result tests |
| `InvalidTransactionException` | `hapi-utils` | Tuweni payload | compile/package reachable | reachable | reachable | legacy error data | data only | NATIVE_REACHABLE_DATA_ONLY | DEFER_TO_MODULE_CLEANUP | existing tests |
| Besu/Tuweni in contract implementation | contract implementation | 345 importing production files | packaged, not native-loaded | reachable | reachable | fixture/full runtime | executable | FULL_RUNTIME_ONLY | DEFER_TO_PHYSICAL_REMOVAL | full suite |
| Besu in `NoopVerificationStrategies` | standalone | verification interface | packaged, not node reachable | optional | reachable | none | executable adapter | STANDALONE_ONLY | DEFER_TO_MODULE_CLEANUP | standalone tests |
| PBJ historical sidecars | `hedera-app` | wire values | reachable | reachable | reachable | mirror/stream history | data only | HISTORICAL_COMPATIBILITY_ONLY | retain | golden/mirror |
| Dagger `ContractRuntimeProvider` binding | `hedera-app` | bound instance | historical instance | full instance | explicit instance | handler composition | mode-dependent | NATIVE_REACHABLE_DATA_ONLY | retain | component tests |

## Construction paths

Native node:

`Hedera` → `contracts.enabled=false` → `HistoricalContractRuntimeProvider` →
`HistoricalContractStateService` → unsupported handler set.

Full node:

`Hedera` → `contracts.enabled=true` → `FullContractRuntimeProvider.create` →
`ContractServiceImpl` → executable handlers, metrics, native-library verification.

Standalone:

`TransactionExecutors` → `ContractServiceImpl` → `FullContractRuntimeProvider`.

Fixture generation uses the full node path at the pinned pre-activation lineage. There is no
`ContractServiceImpl` reference in `Hedera`; its construction is confined to the full provider and
standalone executor.

## Dagger and service metadata

- Dagger receives an already selected `ContractRuntimeProvider`; native modules do not bind
  `ContractServiceImpl`, `FullContractRuntimeProvider`, or system-contract implementations.
- The facade supplied to handle workflows is unsupported in native mode and executable only in the
  full provider.
- No contract execution service-loader provider was found in the application module descriptors.
- `module-info.java` still requires the contract implementation and Besu modules because one app jar
  supports full and native profiles. This is BUILD_ONLY/PACKAGING_ONLY debt.

## Native artifact baseline

The current native distribution contains 204 library jars, 128,894,808 bytes total. Its sorted jar
name inventory SHA-256 is
`357572f0577a670aab0afd54ecdaa65a36e175a8f8271c6e4cfdf1d6742d8b2b`.

Known deferred jars include:

| Jar | Bytes | Classification |
|---|---:|---|
| `app-service-contract-impl-0.75.0-SNAPSHOT.jar` | 1,820,231 | PACKAGING_ONLY in native |
| `besu-datatypes-25.2.2-module.jar` | 47,038 | PACKAGING_ONLY/data bridge |
| `besu-native-common-1.3.0-module.jar` | 3,820 | PACKAGING_ONLY |
| `evm-25.2.2-module.jar` | 668,164 | PACKAGING_ONLY executable |
| `tuweni-bytes-2.4.2-module.jar` | 69,056 | PACKAGING_ONLY/data bridge |
| `tuweni-units-2.4.2-module.jar` | 62,626 | PACKAGING_ONLY/data bridge |
| `secp256k1-1.3.0-module.jar` | 5,552,832 | shared crypto; review separately |

P06B-6 does not remove these jars. Their presence is explicitly not evidence of runtime loading.

