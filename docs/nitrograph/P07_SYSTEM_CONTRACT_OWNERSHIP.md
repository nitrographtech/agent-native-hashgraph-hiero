# P07 System-Contract Ownership

Status: authoritative pre-deletion census for P07-4.

## Measured production ownership

The EVM-facing implementation is owned by
`hedera-smart-contract-service-impl` under
`com.hedera.node.app.service.contract.impl.exec.systemcontracts`.

| Area | Production files | Bytes | Classification | P07-4 disposition |
|---|---:|---:|---|---|
| Root contracts and results | 8 | 30,807 | SYSTEM_CONTRACT_RUNTIME | DELETE_IN_P07_4 |
| Common attempts/calls/translators | 9 | 49,279 | SYSTEM_CONTRACT_CALL / SYSTEM_CONTRACT_TRANSLATOR | DELETE_IN_P07_4 |
| HTS | 134 | 638,079 | SYSTEM_CONTRACT_RUNTIME | DELETE_IN_P07_4 |
| HAS | 18 | 72,319 | SYSTEM_CONTRACT_RUNTIME | DELETE_IN_P07_4 |
| HSS | 14 | 85,202 | SYSTEM_CONTRACT_RUNTIME | DELETE_IN_P07_4 |
| **Total** | **183** | **875,686** |  |  |

The matching implementation test tree contains 143 files and 905,928
bytes. Test-client precompile/system-contract suites and 44 directly named
Solidity/precompile resource paths are catalogued for removal or Wave 5
disposition; they are not production runtime dependencies.

## Registered addresses

| Address | Owner | Native-service target | Disposition |
|---|---|---|---|
| `0x167` | HTS legacy ABI | Token service | Remove EVM façade; retain token service |
| `0x168` | Exchange rate | Exchange-rate data | Remove EVM façade; retain native data access |
| `0x169` | PRNG | Native randomness | Remove EVM façade; retain native randomness |
| `0x16a` | HAS | Account service | Remove EVM façade; retain account service |
| `0x16b` | HSS | Schedule service | Remove EVM façade; retain schedule service |
| `0x16c` | HTS current ABI | Token service | Remove EVM façade; retain token service |

`0x16d` is the HIP-1195 hook execution address, not a registered system
contract in `ProcessorModule`. Its ordinary-EVM hook execution belongs to a
later engine/world-state wave and is not deleted merely because HTS constants
formerly named it.

ABI selectors are declared by `SystemContractMethod` instances in translator
classes. They are runtime routing metadata, not PBJ or persisted identifiers.
They are deleted with their translators. Solidity interfaces that remain
ambiguous are explicitly deferred to Wave 5.

## Composition and reverse consumers

`ProcessorModule` registers the six address entries. The versioned processor
modules pass the resulting map into `CustomMessageCallProcessor`. The
processor routes an addressed frame to `HederaSystemContract.computeFully()`.
HTS, HAS, and HSS call factories choose a translator, construct a call, and
dispatch either directly or through a synthetic native transaction.

The principal reverse consumers outside the system-contract package are:

- `ContractServiceComponent` and `ContractServiceImpl`;
- `ProcessorModule` and the HTS/HAS/HSS translator modules;
- `CustomMessageCallProcessor`;
- versioned EVM processor modules and address checks;
- Bonneville `CallManager`;
- custom call/create/self-destruct operations;
- proxy/hook EVM account adapters;
- system-contract method/metrics utilities.

These are executable full-runtime consumers. Native reachability remains
zero. Historical readers, neutral historical values, records, sidecars, block
translation, PBJ models, and mirror ingestion do not import this package.

## Classification

- `SYSTEM_CONTRACT_RUNTIME`: concrete root contracts plus HTS/HAS/HSS.
- `SYSTEM_CONTRACT_TRANSLATOR`: ABI translator classes and decoder helpers.
- `SYSTEM_CONTRACT_CALL`: call attempts and call implementations.
- `SYSTEM_CONTRACT_REGISTRY`: address map and method registry.
- `SYSTEM_CONTRACT_ABI`: `SystemContractMethod` declarations and Solidity
  interfaces.
- `SYSTEM_CONTRACT_GAS`: gas requirements embedded in system calls.
- `SYSTEM_CONTRACT_METRICS`: system-contract operation-duration labels.
- `SYSTEM_CONTRACT_TEST`: implementation and HAPI execution tests.
- `SYSTEM_CONTRACT_FIXTURE_RESOURCE`: system-contract Solidity interfaces and
  bytecode not used by the authenticated P06 fixture.
- `NATIVE_SERVICE` / `SHARED_NATIVE_API`: account, token, schedule, exchange
  rate, PRNG, dispatch, fee, key, and record APIs outside the façade.
- `HISTORICAL_COMPATIBILITY`: PBJ records/sidecars and neutral readers.

## Planned compiling sub-waves

1. Remove global registries, translator modules, method metrics, and bindings.
2. Remove peripheral root contracts.
3. Remove HSS.
4. Remove HAS.
5. Remove HTS.
6. Remove common call infrastructure and final composition references.

The implementation may collapse adjacent sub-waves in one commit when the
dependency graph requires atomic compilation. No sub-wave may delete a native
service implementation.
