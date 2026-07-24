# P07 Fixture Tooling Ownership

Status: P07-2 COMPLETE
Base: `ad2d2dfa45907b42976917fe03ded296e488d2d1`

## Decision

Fixture generation is test/tooling behavior, never node runtime behavior. P07-2 gives the two
P06A-only entry points a dedicated Gradle and JPMS owner, `fixture-tooling`. The module invokes the
existing HAPI client and a pinned full node; it does not contain or duplicate an execution engine.

`DiverseStateCreation` remains in `test-clients`. It is paired with `DiverseStateValidation` and is
general migration/full-node test infrastructure, not a P06-only publisher. The deterministic
`P06aPublicFixtureIdentity.fixtureKeysAndCerts()` utility also remains in `test-clients` because the
four-node reconnect harness uses it without writing key material. The PEM-writing command moved to
`fixture-tooling`.

## Ownership census

| Component | Former owner | Classification | Consumers and executable dependencies | P07-2 owner / disposition |
|---|---|---|---|---|
| `HistoricalContractStateFixtureCreation` | `test-clients` main source | AUTHENTICATED_FIXTURE_GENERATOR, MOVE_TO_FIXTURE_TOOLING | Pinned full node; HAPI contract create/call and hook operations; PBJ bytes | `fixture-tooling`; package becomes `com.hedera.services.bdd.fixturetooling.p06a` |
| P06A PEM-writing CLI | `P06aPublicFixtureIdentity.main()` in `test-clients` | FIXTURE_SIGNING, MOVE_TO_FIXTURE_TOOLING | Platform key/certificate generator and Bouncy Castle; called only by `scripts/p06a/generate-public-fixture-identity.sh` | `P06aFixtureIdentityGenerator` in `fixture-tooling` |
| Deterministic public identity derivation | `test-clients` | FULL_NODE_TEST_TOOLING | Reconnect `NetworkUtils`; no disk write or publication | Retain in `test-clients`; qualified export only to fixture tooling |
| `DiverseStateCreation` / `DiverseStateValidation` | `test-clients` | GENERAL_TEST_INFRASTRUCTURE, RETAIN_IN_TEST_CLIENTS | Full-node migration validation; contract deployment is one part of a multi-service state | Retain |
| Legacy Ethereum, topic, token, and freeze suites | `test-clients` | GENERAL_TEST_INFRASTRUCTURE, RETAIN_IN_TEST_CLIENTS | Existing HAPI suites selected by the pinned generation sequence | Retain; not duplicated |
| `generate-preactivation-fixture.sh` | `scripts/p06a` | AUTHENTICATED_FIXTURE_GENERATOR | Starts exact source `ff6490d...` full node with pinned JDK | Retain as tooling script |
| `verify-fixture-environment.sh` | `scripts/p06a` | FIXTURE_VALIDATION | Verifies source commit/tree/JDK | Retain as tooling script |
| `fingerprint-fixture.sh` | `scripts/p06a` | FIXTURE_VALIDATION | Deterministic per-file SHA-256 manifest | Retain as tooling script |
| `regenerate-preactivation-v065.sh` | `tools/p06a/fixtures` | AUTHENTICATED_FIXTURE_GENERATOR | Guarded orchestration and restricted workspace | Retain as tooling script |
| Fetch and verify scripts | `tools/p06a/fixtures` | FIXTURE_VALIDATION | Immutable release consumer path; no generation or signing | Permanent compatibility consumer |
| Release/tag/manifest documentation | `docs/nitrograph/fixtures` | FIXTURE_PUBLICATION, RESOURCE_ONLY | Published release and checksum contract | Retain permanently |
| `StorageAccessHook`, `SimpleStorage`, and referenced compiled resources | `test-clients` resources | RESOURCE_ONLY | Shared HAPI resource lookup and pinned generator | Retain in test-client resources; moving them would duplicate shared resource ownership |
| Other Solidity sources and compiled resources | `test-clients` resources | OUT_OF_SCOPE | Broad contract HAPI/system/Ethereum tests | Defer; no broad Solidity movement in P07-2 |
| P06 reconnect and activation suites | `test-clients` | FIXTURE_VALIDATION | Consume authenticated fixtures; do not publish them | Retain |
| P06 mirror harness/evidence | official pinned mirror checkout and committed evidence | FIXTURE_VALIDATION | Consumes record/sidecar/block corpus | Retain |

## Publication and identity controls

- Published fixture: tag `p06a-fixture-preactivation-v065-ff6490d-round4744`, archive
  `nitrograph-p06a-preactivation-v065-ff6490d-round4744.tar.gz`.
- Archive SHA-256:
  `5e40a5c530d27ad77e41c06ab3a5b73df7b7be222f3829590f770e35142bffb0`.
- Manifest SHA-256:
  `e82234205950328f4f6940c588f692d873bd2acff5a642d3167a88eba3f5dcc2`.
- Source commit: `ff6490d66994da11af72e1d2f185ec7874fa383a`.
- Public compromised certificate SHA-256:
  `4f6d63a2fa8e8920c3b88324dc259984e170385cbb255a433f651eedbea550e1`.
- Generated private PEM files remain transient, guarded, untracked, unpublished, and destroyed
  after an isolated run.

No authenticated fixture, release, tag, identity seed, or provenance record changes in P07-2.

## Wave 3 recommendation

Execution-tracer removal should begin only after tracing full-runtime producers of
`ActionSidecarContentTracer`, `AddOnEvmActionTracer`, and `EvmActionTracer` through action,
state-change, and bytecode sidecar production. Neutral PBJ and historical translators are protected.
Fixture tooling invokes the full node and must not acquire a tracer implementation of its own.
