# P06B Packaging Removal Plan

Status: plan only; no physical removal authorized

## Native Gradle and module changes

1. Split the combined application runtime classpath or introduce a native-specific runtime
   configuration.
2. Move contract record-builder/store interfaces required for historical compatibility into the
   implementation-neutral contract API or compatibility module.
3. Remove native `requires transitive com.hedera.node.app.service.contract.impl`.
4. Remove native `requires transitive org.hyperledger.besu.datatypes` and
   `org.hyperledger.besu.evm`.
5. Remove the corresponding contract implementation, EVM, Besu, and Tuweni runtime dependencies
   from the native distribution configuration while retaining them in the full configuration.

## Dagger and construction

- Retain the provider interface and historical binding.
- Keep full-provider construction outside the native component/configuration.
- Remove any full-only handler/query bindings once the native and full component graphs are
  separately assembled.
- Retain no service-loader entry that can discover an executable provider in native mode.

## Candidate native jar removals

- `app-service-contract-impl-*.jar`
- `evm-*.jar`
- `besu-datatypes-*.jar` after data bridges are moved
- `besu-native-common-*.jar`
- `tuweni-bytes-*.jar` and `tuweni-units-*.jar` after remaining API bridges are neutralized
- executable system-contract and Solidity dependencies transitively owned by the implementation

`app-service-contract-*.jar` or a replacement compatibility API remains for schemas and state keys.
Secp256k1 is reviewed independently because native account/signature functionality may require
generic cryptography even after EVM removal.

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

1. Separate native compile and runtime configurations exist.
2. Native module descriptors contain no executable EVM requirements.
3. Historical schemas and all four retained maps pass authenticated lifecycle tests.
4. Shared record, sidecar, block, and mirror outputs retain golden equivalence.
5. Native provider/Dagger graphs cannot construct or discover executable providers.
6. Native runtime and packaged-jar inventories contain no prohibited executable EVM artifacts.
7. Full runtime and fixture generation remain reproducible on their explicitly separate graph.
8. SBOM, license, and notice changes are reviewed without waiver.
9. Consensus, platform-sdk, persisted identifiers, codecs, and schemas remain unchanged.

