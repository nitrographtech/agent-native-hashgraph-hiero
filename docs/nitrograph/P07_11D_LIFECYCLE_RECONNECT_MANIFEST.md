# P07-11D Lifecycle, Reconnect, and Historical-Fixture Manifest

## Authority

This manifest begins at
`c32d6b5ce7041f809a31c761b6d497c1f14bfbdf` and refines the twelve-site
handoff in `P07_11D_LIFECYCLE_RECONNECT_HANDOFF.md`. P07-11B and P07-11C
ownership decisions are immutable.

No entry is `UNRESOLVED`.

## Fixture authority

The live `P06aHistoricalStateReconnectTest` producer is not an authoritative
fixture boundary. The authoritative reconnect input is the immutable release
`p07-four-node-postwrite-v065-storage424242-v1`, described by
`docs/nitrograph/fixtures/postwrite-four-node-v065.metadata.json`.

- Generator source commit:
  `64da043f766da29d0fd3e20e3f51051fdd31f5a1`
- Generator source archive SHA-256:
  `6af7596d365e62561df913032daaaf4437c3eafeb75e567e33057e8eab5b7a7a`
- Fixture archive SHA-256:
  `0fd75fd4da408787ddef6b5a73300ce8018e330eb2f3a8e98d11ef9fbceceaa3`
- Manifest SHA-256:
  `967798ff82aad71ac7b9f6b4464279479f4e2a201098a02995cfd6cd70e30439`
- State round:
  `2143`
- State root:
  `0c3b973439f127093ad1d64bd9777d9a365a6d18522e6e46c6bd0fa41fa9f4e8eba8b4eac33b0cda2bfa82cf175a0894`
- Four-node equal-weight roster:
  nodes `0`, `1`, `2`, and `3`
- Protected inventory SHA-256:
  `5f74ae63338dfb5510d0dc09a7afafe64f5ee00f9d0c7ed5c040cb5fe82dfa48`
- Historical maps:
  `STORAGE=1` with decoded value `424242`, `BYTECODE=2`,
  `EVM_HOOK_STATES=1`, and `LAMBDA_STORAGE=1`
- Authentication:
  repository-controlled metadata, release archive checksum, per-file
  `SHA256SUMS`, signed-state root, and four public certificate fingerprints
- Private material:
  none

The fixture is deterministic historical evidence. Current test clients consume
and validate it; they do not regenerate it through live contract execution.

## Twelve-site disposition

| # | Owner and root | Wrapper | Required invariant | Final disposition | Replacement and retained assertions |
| ---: | --- | --- | --- | --- | --- |
| 1 | `LifecycleTest.assertAllGetInfoResponsesIncludeExternalizedLedgerId` | `contractCustomCreate` | Native state and query behavior survive upgrade/restart; ledger ID remains externally consistent | `SPLIT_REQUIRED` | Remove contract creation/info branch. Retain account, file, topic, fungible token, NFT, and schedule setup and ledger-ID assertions in the same lifecycle callers. |
| 2 | `P06aHistoricalStateReconnectTest.historicalStateSurvivesNativeReconnect` | successful `contractCreate("SimpleStorage")` | Historical STORAGE survives genuine reconnect | `REPLACE_WITH_IMMUTABLE_FIXTURE` | Delete the obsolete combined generator/consumer. Fixture B supplies authenticated `STORAGE=424242`; the established external four-node harness owns real reconnect. |
| 3 | same | successful `contractCall("SimpleStorage","set")` | Historical written value survives reconnect | `REPLACE_WITH_IMMUTABLE_FIXTURE` | Fixture B's authenticated round-2143 state supplies the written value and protected-map fingerprint. |
| 4 | same | successful `contractCreate("StorageAccessHook")` | Historical hook/bytecode maps survive reconnect | `REPLACE_WITH_IMMUTABLE_FIXTURE` | Fixture B supplies authenticated BYTECODE, EVM_HOOK_STATES, and LAMBDA_STORAGE maps. |
| 5 | `P06aHistoricalStateReconnectTest.rejectedLegacyBodies` | `explicitContractCreate` | Unsupported historical operation remains rejected across activation/reconnect | `REMOVE_OBSOLETE_LIFECYCLE_ROOT` | Remove duplicate with the obsolete producer. The authenticated fixture consumer and permanent rejection suite retain this invariant without successful setup. |
| 6 | same | `contractCall` | Same | `REMOVE_OBSOLETE_LIFECYCLE_ROOT` | Same replacement. |
| 7 | same | `contractUpdate` | Same | `REMOVE_OBSOLETE_LIFECYCLE_ROOT` | Same replacement. |
| 8 | same | `contractDelete` | Same | `REMOVE_OBSOLETE_LIFECYCLE_ROOT` | Same replacement. |
| 9 | same | `explicitEthereumTransaction` | Same | `REMOVE_OBSOLETE_LIFECYCLE_ROOT` | Same replacement. |
| 10 | `AuthenticatedHistoricalFixtureConsumerTest.rejectedBodies` | `explicitContractCreate` | Authenticated fixtures remain consumable; unsupported bodies reject before native continuity | `PRESERVE_MINIMAL_REJECTION` | Retain unchanged. It has no successful executable setup and follows native account, asset, and topic operations. |
| 11 | same | direct `HapiContractCall` | Same | `PRESERVE_MINIMAL_REJECTION` | Retain unchanged. |
| 12 | `DiverseStateValidation.validateDiverseState` | `contractCallLocal` | Diverse saved-state hashing, serialization, and retained native state validation remain broad | `REWRITE_HISTORICAL_QUERY_ASSERTION` | Remove obsolete local execution and its contract-bytecode assertion. Retain file-state and other native diverse-state validation. Historical bytecode/maps remain authenticated by Fixture A/B metadata and protected-map validation. |

## Ownership closure

### Lifecycle callers

`TssCutoverTest` and `WrapsHandoffsTest` call
`assertAllGetInfoResponsesIncludeExternalizedLedgerId` during real lifecycle
transitions. The helper remains registered and preserves its native entity
setup and post-transition query assertions. Only the unsupported contract
branch is removed.

### Reconnect ownership

`P06aHistoricalStateReconnectTest` has no caller or registration outside its
own JUnit root. Its pre-state is created exclusively by successful obsolete
execution, so retaining the class would violate the fixture-consumer boundary.
Deleting it does not delete reconnect certification: Fixture B and the
established installer/orchestrator remain the authoritative real
teacher/learner reconnect path.

### Historical fixture consumer

`AuthenticatedHistoricalFixtureConsumerTest` contains no fixture producer,
compiler, deployment helper, or successful call. The surrounding installer
authenticates the fixture before this suite performs retained native operations
and two minimal fail-closed probes.

### Diverse state

`DiverseStateValidation` remains a broad saved-state validator. Contract-local
simulation is not a supported historical query pathway; persisted historical
contract maps are validated through authenticated fixture inventory instead.

## Expected final census

| Category | Before | Remove | Remain |
| --- | ---: | ---: | ---: |
| P07-11D successful executable setup | 4 | 4 | 0 |
| P07-11D duplicate rejection constructors | 5 | 5 | 0 |
| P07-11D minimal authenticated-fixture rejection | 2 | 0 | 2 |
| P07-11D executable local query | 1 | 1 | 0 |
| P07-11C intentional minimal rejection | 6 | 0 | 6 |
| **Suite-level executable syntax** | **18** | **10** | **8** |

The eight remaining constructions are all intentional rejection probes with no
successful executable setup. There is no remaining successful lifecycle,
reconnect, or historical-query construction.

## Implementation result

Implemented at `3793c07780`:

- `LifecycleTest` retains its real lifecycle callers and native account, file,
  topic, token, NFT, and schedule assertions; its contract branch is gone.
- `P06aHistoricalStateReconnectTest` is deleted. Its live generator is replaced
  by authenticated Fixture B, while the unchanged external harness remains the
  owner of genuine teacher/learner reconnect certification.
- `AuthenticatedHistoricalFixtureConsumerTest` is unchanged and retains its
  two setup-independent rejection probes.
- `DiverseStateValidation` retains broad file-state validation but no longer
  performs contract bytecode or local-execution queries.

The post-change scan has eight sites, all intentional rejection constructions:
two in `AuthenticatedHistoricalFixtureConsumerTest`, five in
`HistoricalContractExecutionRejection`, and one in `Issue1765Suite`.
