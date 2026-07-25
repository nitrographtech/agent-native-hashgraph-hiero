# P07 Executable Fixture Compatibility Ownership

## Decision

Authenticated P06A fixture generation is no longer owned by production source. The complete
execution closure is frozen at commit
`64da043f766da29d0fd3e20e3f51051fdd31f5a1` in release
`p07-executable-fixture-compat-v1`.

## Closure

| Fixture output | Frozen owner | Dependency classification |
| --- | --- | --- |
| Two contract creations and one call | `ContractServiceImpl`, transaction processors, HEVM/Besu | `EXECUTION_ENGINE_REQUIRED` |
| STORAGE value `424242` and BYTECODE entries | mutable world state and writable contract store | `MUTABLE_WORLD_STATE_REQUIRED` |
| Three actions | fixture `EvmActionTracer` closure | `ACTION_TRACER_REQUIRED` |
| Two state-change groups and two bytecode entries | execution processors and sidecar builders | `STATE_OUTPUT_REQUIRED` |
| Seven sidecar records and block/record ordering | pinned full node and fixture HAPI orchestration | `RECORD_OUTPUT_REQUIRED` |
| Hook-state write | native hook transaction in the pinned fixture suite | `FIXTURE_COMPATIBILITY_REQUIRED` |
| Signing, manifest, release, and provenance | published P06A fixture release | `RETAIN_IN_REPOSITORY_TOOLING` |

The closure includes `fixture-tooling`, `test-clients`, `hedera-app`,
`app-service-contract-impl`, Besu EVM/datatypes, Tuweni bytes/units, contract compiler inputs,
record/sidecar/block writers, and the fixture-only action tracer. It does not include private
fixture material in the compatibility release.

Production and historical compatibility have no dependency on this release. The repository-local
fixture-tooling module now verifies identity/release provenance only; executable generation is
performed by checking out or unpacking the frozen release.
## P07-8 consumer validation finding

The published fixture owns one node and one signed roster entry. It is sufficient for exact-head
one-node activation, PCES replay, and restart-state production, but it cannot seed a real
four-node reconnect. Its round-4744 `STORAGE` map is empty and remains empty after exact-head
replay. The former four-node harness produced value 424242 through live contract execution and is
therefore fixture generation, not fixture consumption.
