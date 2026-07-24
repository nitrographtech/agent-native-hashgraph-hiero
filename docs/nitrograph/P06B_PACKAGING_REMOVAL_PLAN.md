# P06B Packaging Removal Plan

Status: native packaging isolation implemented; repository source removal remains unauthorized

## Native Gradle and module changes

1. DONE in P06B-7A: emit separate native and full application jars.
2. DONE in P06B-7A: move native-required builder/store contracts to `app-service-contract`.
3. DONE: omit the combined full/native module descriptor from the native application artifact.
4. DONE: remove native module edges to contract implementation, Besu, Tuweni, and EVM.
5. DONE: remove implementation, EVM, Besu, Tuweni, precompile, KZG, and executable native
   dependencies from native packaging while retaining them in the full configuration.

## Dagger and construction

- Retain the provider interface and historical binding.
- Keep full-provider construction outside the native component/configuration.
- Remove any full-only handler/query bindings once the native and full component graphs are
  separately assembled.
- Retain no service-loader entry that can discover an executable provider in native mode.

P06B-7A introduced profile-specific runtime and store factories. Native service metadata advertises
only historical factories; full metadata retains both and selects the executable stores by
priority.

## Candidate native jar removals

- `app-service-contract-impl-*.jar`
- `evm-*.jar`
- `besu-datatypes-*.jar` after data bridges are moved
- `besu-native-common-*.jar`
- `tuweni-bytes-*.jar` and `tuweni-units-*.jar` after remaining API bridges are neutralized
- executable system-contract and Solidity dependencies transitively owned by the implementation

`app-service-contract-*.jar` or a replacement compatibility API remains for schemas and state keys.
Secp256k1 account verification remains required. P06B-7 replaces the Besu-native implementation
only in the native packaging profile with an API-compatible Bouncy Castle verifier. The upstream
platform source and full distribution remain unchanged.

## Distribution and test impact

- Update `distributionNativeAgent`, copy/runtime classpaths, module descriptors, startup scripts,
  Docker layers, SBOMs, and license reports.
- Preserve `distributionFull` and the pinned fixture-generation workflow.
- Re-run authenticated activation, maps, restart/replay, reconnect/synchronization, all stream
  golden tests, and official mirror ingestion.
- Record native jar count, bytes, sorted inventory hash, SBOM, and license inventory before/after.

## Licensing and binary inventory

No existing licensing finding is waived. Physical removal must demonstrate which Besu/Tuweni/EVM
licenses and notices disappear and which remain through historical APIs or generic cryptography.
The binary inventory must prove both removed direct jars and removed transitive jars.

## P06C physical-removal gates

1. DONE: separate native compile and runtime packaging configurations exist.
2. Native module descriptors contain no executable EVM requirements.
3. Historical schemas and all four retained maps pass authenticated lifecycle tests.
4. Shared record, sidecar, block, and mirror outputs retain golden equivalence.
5. Native provider/Dagger graphs cannot construct or discover executable providers.
6. DONE: native runtime and packaged-jar inventories contain no prohibited executable EVM
   artifacts.
7. Full runtime and fixture generation remain reproducible on their explicitly separate graph.
8. SBOM, license, and notice changes are reviewed without waiver.
9. Consensus, platform-sdk, persisted identifiers, codecs, and schemas remain unchanged.
