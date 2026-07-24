# P07 Repository De-execution Census

Status: census complete; source deletion not started  
Base: `p06b/native-packaging-isolation-implementation@b487cd23bab93a6fc84c435a82f2d300ec58d83e`

## Method and boundaries

This census uses source paths, Java imports, JPMS descriptors, Gradle project ownership, application
assembly, tests, fixtures, and CI definitions. It distinguishes a source tree being present from it
being reachable in the Nitrograph native runtime. P06B already proved the exact native artifact
cannot load the executable stack.

No source, package, Gradle project, persisted identifier, PBJ definition, schema, codec, state ID,
service name, consensus code, or platform code was changed during this census.

## Quantitative baseline

| Item | Count or size |
|---|---:|
| Repository files visible to `rg --files` | 9,356 |
| Gradle/JPMS build descriptors inspected | 197 |
| `app-service-contract-impl` main files | 422 |
| `app-service-contract-impl` test files | 264 |
| `app-service-contract-impl` total source files | 686 |
| `app-service-contract-impl` source bytes | 4,185,454 |
| Contract implementation production Java files importing Besu | 282 |
| Contract implementation production Java files importing Tuweni | 208 |
| Repository Java files mentioning `MessageFrame` | 200 |
| Repository Java files mentioning `WorldUpdater` | 161 |
| Contract HAPI suite Java files | 191 |
| Contract HAPI suite bytes | 4,384,846 |
| Contract test resources | 1,017 files / 4,326,334 bytes |
| Solidity source fixtures | 620 files |

These values are a checkout census, not a Git pack-size estimate.

## Source ownership census

| Component | Gradle / JPMS owner | Source and package roots | Tests / fixtures / CI | Reachability and compatibility | Classification | Difficulty |
|---|---|---|---|---|---|---|
| Contract implementation root | `hedera-smart-contract-service-impl`; `com.hedera.node.app.service.contract.impl` | `hedera-node/hedera-smart-contract-service-impl/src/main/java/com/hedera/node/app/service/contract/impl` | 264 module tests; full distribution; P06A fixture generation; smart-contract HAPI CI | Native: absent. Full/standalone/fixture: required. Historical: none after API extraction. | EXECUTABLE_RUNTIME, FULL_RUNTIME_ONLY | Very high: monolithic owner of handlers, EVM, state, systems, tracers, and full schemas |
| Full provider and stores | `hedera-app`; `com.hedera.node.app` | `services/FullContractRuntimeProvider*`, `store/FullContractStoreFactory` | app composition tests; full startup | Native app jar: absent. Full: construction root for `ContractServiceImpl`. | FULL_RUNTIME_ONLY | Medium after full distribution retirement |
| Standalone executor | `hedera-app`; same JPMS module | P07-1 removes `workflows/standalone/**`, `StandaloneFeeCalculator`, and `StandaloneFeeCalculatorImpl` | 3 standalone tests plus fee/record tests removed with the implementation | Native app jar: already absent before P07-1. Repository production source: removed in P07-1 pending final lifecycle validation. | STANDALONE_ONLY | Wave 1 implementation complete; validation pending |
| Transaction handlers | contract implementation | `impl/handlers/**` (17 production files) | handler tests; app dispatcher/composition tests | Native uses fail-closed handlers from historical provider. Full handlers execute contracts. | EXECUTABLE_RUNTIME | Medium; remove full-provider wiring first |
| EVM processors and operations | contract implementation | `impl/exec/**` excluding system contracts; `impl/hevm/**` | implementation tests; EVM validation suites | Native: absent. Full: execution engine integration. | EVM_ENGINE, EXECUTABLE_RUNTIME | High |
| World state and mutable stores | contract implementation | `impl/state/**` (29 files / 155,738 production bytes) | state/store tests; full store factory; integration suites | Native historical adapters are in `app-service-contract`, not here. | WORLD_STATE | High: mixed account, frame-state, and writable persisted-store code |
| System contracts | contract implementation | `impl/exec/systemcontracts/**` (183 production files / 875,686 bytes) | HTS/HSS/HAS unit tests and contract precompile HAPI suites | Native: absent. Full: executable precompiles for token, schedule, and account services. | SYSTEM_CONTRACT | Very high due to broad token/schedule test coupling |
| Execution tracers | contract implementation and fixture tooling | Runtime tracer interfaces plus fixture-owned `EvmActionTracer`/`ActionStack` closure | tracer tests; pinned full-node fixture generation | Native shared translation is neutral. P07-3A isolated the authenticated fixture's three-action producer in `fixture-tooling`; normal native and full artifacts use `NoTracer` and do not package it. | EXECUTION_TRACER, FIXTURE_TOOLING_REQUIRED | PREREQUISITE COMPLETE: resume removal of the remaining runtime tracer interfaces and bindings |
| Ethereum execution | contract implementation | `EthereumTransactionHandler`, `HydratedEthTxData`, `EthereumCallDataHydration`, `EthTxSigsCache`, HEVM transaction factories/results | Ethereum HAPI suites; fixture transaction sequence | Historical Ethereum PBJ output remains permanent; execution path is full-only. | ETHEREUM_RUNTIME | High |
| Solidity runtime/assets | no independent production Gradle module | executable address/query behavior in implementation; `test-clients/src/main/resources/contract/**` contains Solidity sources and compiled fixtures | 620 `.sol` fixtures; smart-contract HAPI and performance CI | “Solidity” persisted/query terminology is historical and permanent. Compiler/source fixtures are test/full-runtime inputs. | SOLIDITY_RUNTIME, FIXTURE_ONLY, TEST_ONLY | Medium after fixture archive is frozen |
| Besu integration | contract implementation; app full descriptor; test clients; platform base crypto | 282 implementation, 11 test-client, 4 app, and 1 platform production import owners | full tests, fixtures, test clients | Native exact artifact: absent. Full and source: present. Platform has a protected generic secp verifier edge. | EXECUTABLE_RUNTIME; one protected GENERIC_SHARED_DEPENDENCY | Very high; platform edge requires separate authorization |
| Tuweni integration | contract implementation; app full descriptor; test clients | 208 implementation, 46 test-client, and 1 app production import owners | full tests and fixture clients | Native exact artifact: absent. Historical neutral boundaries no longer require it. | EXECUTABLE_RUNTIME, TEST_ONLY | High |
| Bonneville experimental EVM | contract implementation | `impl/bonneville/**` (7 files) | implementation tests where present | No historical compatibility role identified. | EVM_ENGINE, EXECUTABLE_RUNTIME | Low once module deletion is authorized |
| Native library verification | contract implementation | `impl/nativelibverification/NativeLibVerifier` | full startup/tests | Native filtered artifact does not invoke it. | EXECUTABLE_RUNTIME | Low |
| Fixture identity tooling | `fixture-tooling` plus shared derivation in `test-clients` | `P06aFixtureIdentityGenerator`; `fixtures/p06a/P06aPublicFixtureIdentity` | P06A reproducible public test identity and reconnect | PEM-writing is isolated in tooling; deterministic in-memory derivation remains shared test infrastructure. | FIXTURE_ONLY; descriptor is PERMANENT evidence | P07-2 externalized |
| Historical fixture generation suites | `fixture-tooling` and `test-clients` | Fixture-only `HistoricalContractStateFixtureCreation`; shared `DiverseStateCreation`, freeze, and legacy Ethereum suites | pinned full-runtime generation workflow | Dedicated entry point is outside runtime artifacts; general HAPI suites remain shared test infrastructure. | FIXTURE_ONLY / GENERAL_TEST_INFRASTRUCTURE | P07-2 bounded ownership split complete |
| Smart-contract HAPI suite | `test-clients` | `suites/contract/**`; contract DSL/utility imports elsewhere | `hapiTestSmartContract*`, block-node contract tests, performance tests | Mostly executable full-runtime coverage; a bounded rejection/mirror subset must remain. | TEST_ONLY, FULL_RUNTIME_ONLY, some HISTORICAL_COMPATIBILITY | Very high: 34 direct implementation importers and cross-suite address helpers |
| State validator implementation imports | `hedera-state-validator` | `StateUtils`; JPMS requires contract implementation | validator tests and P06 map evidence | Must move remaining schema/state use to neutral historical API before implementation deletion. | REQUIRES_BRIDGE, HISTORICAL_COMPATIBILITY | Low/medium |
| App implementation imports | `hedera-app` | 16 production/test owners outside contract implementation | app tests and full distribution | Six record-builder imports already resolve from neutral API despite legacy package. Full/standalone imports must be deleted with their profiles. | MIXED | Medium |
| Neutral contract API | `hedera-smart-contract-service`; `com.hedera.node.app.service.contract` | 18 production files including six stream builders and history packages | API tests, activation, reconnect, mirror | Required by native, full until retirement, historical schemas and builders. | NEUTRAL_API, HISTORICAL_COMPATIBILITY, PERMANENT | Must retain |
| Neutral historical translation | `hedera-app` | `blocks/historical/**` and `BlockItemsTranslator` consumers | P06B goldens and official mirror ingestion | Native and historical stream compatibility. No execution behavior. | HISTORICAL_COMPATIBILITY, PERMANENT | Must retain |
| PBJ/protobuf contract models | `hapi` and protobuf source modules | contract bodies, results, logs, actions, state changes, bytecode, block outputs | records, sidecars, blocks, mirror | Persisted/wire compatibility and deterministic rejection. | PBJ_MODEL, PERMANENT | Must retain unchanged |
| Historical schemas and stores | neutral contract API | `history/V0490ContractSchema`, `V065ContractSchema`, historical readable stores | map integrity and saved-state lifecycle | Owns `STORAGE`, `BYTECODE`, `EVM_HOOK_STATES`, `LAMBDA_STORAGE`. | PERSISTED_SCHEMA, PERMANENT | Must retain unchanged |

## Contract implementation package census

| Package subtree | Production files | Classification |
|---|---:|---|
| `exec/systemcontracts` | 183 | SYSTEM_CONTRACT |
| `exec/operations` | 21 | EVM_ENGINE |
| `exec/scope` | 16 | EXECUTABLE_RUNTIME / WORLD_STATE |
| `exec/processors` | 11 | EVM_ENGINE |
| `exec/utils` | 20 | mixed executable utilities; extract nothing without proof |
| `exec/gas` | 8 | EXECUTABLE_RUNTIME historical terminology only in outputs |
| `exec/metrics` | 6 | EXECUTABLE_RUNTIME |
| `exec/tracers` | 3 | EXECUTION_TRACER |
| versioned EVM modules `v030`–`v067` | 19 | EVM_ENGINE |
| `hevm` | 15 | ETHEREUM_RUNTIME / EVM_ENGINE |
| `state` | 29 | WORLD_STATE |
| `handlers` | 17 | EXECUTABLE_RUNTIME / ETHEREUM_RUNTIME |
| `infra` | 8 | ETHEREUM_RUNTIME / WORLD_STATE |
| `calculator` | 8 | FULL_RUNTIME_ONLY |
| `bonneville` | 7 | EVM_ENGINE |
| `annotations` | 17 | EXECUTABLE_RUNTIME Dagger scopes |
| root/module composition | 3 plus descriptor | EXECUTABLE_RUNTIME |

## Reverse dependency census

Direct Java importers of `com.hedera.node.app.service.contract.impl` outside its owner:

| Consumer | Importing files | Disposition |
|---|---:|---|
| `test-clients` | 34 | split historical/rejection tests from full-runtime executable suites |
| `hedera-app` | 16 | retain neutral builder imports; remove full and standalone imports |
| `hedera-state-validator` | 2 | replace implementation schema/store references with neutral history API |

JPMS consumers are:

- `com.hedera.node.app` (`hedera-app`);
- `com.hedera.node.test.clients`;
- `com.hedera.state.validator`.

The implementation module itself requires Besu datatypes/EVM/crypto and Tuweni bytes/units. The app
and test-client descriptors also retain full-profile Besu/EVM edges. The neutral contract API has no
such edge.

## CI ownership

- `.github/workflows/zxc-execute-hapi-tests.yaml` owns `hapiTestSmartContract` and
  `hapiTestSmartContractSerial`.
- `node-flow-build-application.yaml`, pull-request checks, MATS, and dry-run workflows enable that
  job transitively.
- Block-node regression includes contract-delete execution coverage.
- Merge-queue, single-day, longevity, and performance workflows own SmartContract load tests.
- `anhn-native-agent-ci.yml` owns the permanent historical provider, native binary policy, and
  protected-layer checks; these must survive with contract-implementation test tasks removed.

## Protected-layer finding

`platform-sdk/base-crypto` has one production Besu-native secp256k1 verifier and a JPMS requirement
on `org.hyperledger.besu.nativelib.secp256k1`. P06B preserved platform source and replaced the class
only in the native distribution artifact.

Repository-wide Besu source/dependency removal therefore cannot complete without either:

1. an upstream platform release exposing a neutral verifier, or
2. separate explicit authorization and review for a platform-sdk change.

No P07 removal wave may cross this boundary under the current authorization.
