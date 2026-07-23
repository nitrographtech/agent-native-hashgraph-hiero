# P06B Remaining Dependency Map

Baseline: `agent-native@10dbab90172adbe63441a04679bcc442fe449991`

This map separates executable runtime coupling from historical data translation.
Counts below cover production Java imports under `hedera-node`; test imports are
reported separately by the reproducible inventory commands at the end.

## Module inventory

| Module | Besu-importing production files | Tuweni-importing production files | Classification | Reachability and disposition |
| --- | ---: | ---: | --- | --- |
| `hedera-smart-contract-service-impl` | 159 | 79 | `EXECUTABLE_RUNTIME`, `FULL_RUNTIME_ONLY`, `REMOVABLE_LATER` | EVM processors, frames, world updaters, tracers, Dagger execution modules, Besu adapters, contract result/action/sidecar production |
| `hedera-app` | 3 before P06B-1; 2 after | 0 | mixed `SHARED_NATIVE`, `STANDALONE_ONLY` | `BlockItemsTranslator` was `HISTORICAL_DATA_MODEL` and `REMOVABLE_NOW`; `TransactionExecutors` and `NoopVerificationStrategies` remain standalone/full-runtime paths |
| `hapi-utils` | 4 | 1 | mixed `SHARED_NATIVE`, `HISTORICAL_DATA_MODEL`, `REMOVABLE_LATER` | Ethereum signature/transaction utilities and conversions; module requirements remain transitive |
| `hedera-token-service-impl` | 0 | 1 | `SHARED_NATIVE`, `REMOVABLE_LATER` | Tuweni byte representation remains outside the log slice |
| `test-clients` | 11 | 46 | `FULL_RUNTIME_ONLY`, `STANDALONE_ONLY` | Full-runtime BDD execution, stream translators, fixture generation, and compatibility assertions |

The P06B-1 target is only the Besu `Log` dependency in
`hedera-app/.../blocks/BlockItemsTranslator.java`. Other rows are not authorized
for removal in this slice.

## Module descriptors and Gradle edges

- `com.hedera.node.app` still has transitive requirements on
  `org.hyperledger.besu.datatypes` and `org.hyperledger.besu.evm`. These remain
  because full and standalone runtime construction still resides in this module.
- `com.hedera.node.app.service.contract.impl` transitively requires Besu
  datatypes/EVM and Tuweni bytes/units and directly requires Besu crypto.
- `com.hedera.node.app.hapi.utils` transitively requires Besu EVM/native
  secp256k1 and Tuweni bytes.
- `com.hedera.node.app.service.token.impl` requires Tuweni bytes.
- `com.hedera.node.test.clients` requires Besu datatypes, EVM, crypto,
  native-secp256k1, and Tuweni bytes/units.
- The `hedera-app` test module requires Tuweni bytes. P06B-1 does not change
  module descriptors or distribution-wide dependency declarations because the
  standalone and full-runtime paths still require them.

## Construction, Dagger, and service boundaries

| Path | Classification | P06B disposition |
| --- | --- | --- |
| `Hedera` → `FullContractRuntimeProvider` → `ContractServiceImpl` | `FULL_RUNTIME_ONLY`, `EXECUTABLE_RUNTIME` | retain |
| native `Hedera` → `HistoricalContractRuntimeProvider` | `SHARED_NATIVE`, read-only | retain; executable provider remains unresolved |
| `TransactionExecutors` → `ContractServiceImpl` | `STANDALONE_ONLY`, `EXECUTABLE_RUNTIME` | retain |
| `ContractServiceComponent` and versioned `V034`…`V066` Dagger modules | `EXECUTABLE_RUNTIME` | retain |
| shared handler composition | `SHARED_NATIVE` | already fail-closed from P06A |
| contract implementation module service/provider metadata | `FULL_RUNTIME_ONLY` | retain |

No service-loader edge creates an executable contract provider in native mode.
The P06A class-load and provider-resolution gates remain authoritative.

## Translation paths

| Data path | Current type boundary | Classification | Planned slice |
| --- | --- | --- | --- |
| block/record log translation | `EvmTransactionLog` → neutral `HistoricalLog` → `ContractLoginfo` | `SHARED_NATIVE`, `HISTORICAL_DATA_MODEL`, `REMOVABLE_NOW` | P06B-1 |
| log bloom | neutral address/topic bytes and Keccak-256 | `SHARED_NATIVE`, `HISTORICAL_DATA_MODEL` | P06B-1 |
| full-runtime Besu log production | Besu `Log` → block trace PBJ values | `FULL_RUNTIME_ONLY`, `EXECUTABLE_RUNTIME` | retain adapter/source path |
| contract function results | Besu-backed runtime result assembly | mixed historical/executable | later P06B |
| contract actions | Besu frames/tracers → sidecar/action PBJ | mixed historical/executable | later P06B |
| contract state changes | world updater/storage abstractions → PBJ | mixed historical/executable | later P06B |
| sidecars | action, bytecode, and state-change translators | `HISTORICAL_DATA_MODEL` plus full-runtime producers | later P06B |
| Ethereum-format history | transaction/result/signature utilities | mixed `SHARED_NATIVE` and `FULL_RUNTIME_ONLY` | later P06B |

Record and block wire values remain PBJ types. P06B introduces no persisted state,
wire-format, codec, state-ID, or schema change.

## Distribution reachability

- Full distribution: retains `ContractServiceImpl`, Besu execution jars, Tuweni,
  native crypto, standalone execution, and compatibility adapters.
- Native-agent distribution: selects the historical provider; P06A instrumented
  activation/restart/replay/reconnect loaded no Besu/Tuweni or prohibited EVM
  engine classes.
- Packaged jars may still be present because `hedera-app` and `hapi-utils` retain
  full-runtime module edges. Physical packaging removal is not part of P06B-1.

## Classification summary

- `REMOVABLE_NOW`: Besu `Log`, `asBesuLog`, `bloomFor`, and `bloomForAll` usage
  in the shared `BlockItemsTranslator` log path.
- `REMOVABLE_LATER`: shared result, action, state-change, sidecar, and
  Ethereum-format translation types after independent golden-output gates.
- `EXECUTABLE_RUNTIME`: contract implementation processors, frames, operations,
  tracers, world updater, Dagger versions, and native libraries.
- `FULL_RUNTIME_ONLY`: the full provider and contract implementation module.
- `STANDALONE_ONLY`: `TransactionExecutors` and its verification/operation setup.
- `SHARED_NATIVE`: block/record translation, HAPI utilities, and remaining token
  byte utilities; each must be neutralized before physical removal.

## Reproducible inventory

```bash
rg -l '^import org\.hyperledger\.besu' hedera-node --glob '*.java'
rg -l '^import org\.apache\.tuweni' hedera-node --glob '*.java'
rg -n 'requires .*besu|requires .*tuweni' hedera-node --glob module-info.java
rg -n '(besu|tuweni)' hedera-node --glob build.gradle.kts
rg -n 'ContractServiceImpl|FullContractRuntimeProvider|HistoricalContractRuntimeProvider' hedera-node
rg -n 'BlockItemsTranslator|ContractLoginfo|EvmTransactionLog|ContractAction|StateChange' hedera-node
```
