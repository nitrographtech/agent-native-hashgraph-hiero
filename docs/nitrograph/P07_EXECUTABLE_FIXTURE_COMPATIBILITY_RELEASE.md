# P07 Executable Fixture Compatibility Release

## Immutable release

- Repository: `nitrographtech/agent-native-hashgraph-hiero`
- Tag: `p07-executable-fixture-compat-v1`
- Source commit: `64da043f766da29d0fd3e20e3f51051fdd31f5a1`
- Release: `https://github.com/nitrographtech/agent-native-hashgraph-hiero/releases/tag/p07-executable-fixture-compat-v1`
- Asset: `p07-fixture-compat-v1-source.tar.gz`
- Size: `46,374,065` bytes
- SHA-256: `6af7596d365e62561df913032daaaf4437c3eafeb75e567e33057e8eab5b7a7a`
- License: Apache-2.0 repository source plus the dependency licenses resolved by the pinned build
- JVM used for independent verification: Eclipse Adoptium `25.0.3+9`
- Gradle wrapper: `9.5.0`

The release is non-production compatibility tooling. It contains tracked source only. It contains
no generated saved state, node keys, fixture private key, database, Docker state, or transient
runtime output.

## Reproducible build

```text
tar -xzf p07-fixture-compat-v1-source.tar.gz
cd agent-native-hashgraph-hiero-p07-fixture-compat-v1
JAVA_HOME=<jdk-25> ./gradlew \
  :fixture-tooling:test \
  :fixture-tooling:fixtureFullNodeDistribution \
  --no-daemon
```

An independent clean extraction completed this build successfully on 2026-07-25. The frozen
P07-3A/P07-6 authenticated evidence proves the same source path reproduces three actions, two
state-change groups, two bytecode records, seven sidecar records, map value `424242`, and valid
record/block/mirror semantics. The published P06A fixture remains authoritative; no output from
this verification was signed or promoted.

## Fixture contract

- authoritative tag: `p06a-fixture-preactivation-v065-ff6490d-round4744`
- archive SHA-256: `5e40a5c530d27ad77e41c06ab3a5b73df7b7be222f3829590f770e35142bffb0`
- manifest SHA-256: `e82234205950328f4f6940c588f692d873bd2acff5a642d3167a88eba3f5dcc2`
- verified files: `396`
- certificate fingerprint:
  `4f6d63a2fa8e8920c3b88324dc259984e170385cbb255a433f651eedbea550e1`

The public compromised-by-design fixture identity remains guarded. Private material is generated
only in a caller-created temporary directory and is never published by this release.

## Retirement policy

Retain this release while historical compatibility gates require fixture regeneration. It may be
retired only after an independently authorized replacement preserves the authenticated semantic
contract and provenance.
