# P07-8 Reconnect Harness Ownership

## Two-fixture consumer separation

`AuthenticatedHistoricalFixtureConsumerTest` is the repository-owned retained-operation and
fail-closed-body suite. The surrounding fixture installer verifies and copies an immutable signed
state and PCES set; the suite never invokes `DiverseStateCreation`,
`HistoricalContractStateFixtureCreation`, fixture tooling, Solidity compilation, live contract
deployment, or mutable world-state construction.

Fixture A remains a one-node activation consumer path. Fixture B installs the same authenticated
round-2143 four-member state for nodes 0–3 plus each node's canonical PCES, launches the exact
non-executable `HederaNode.jar`, and orchestrates stop/advance/restart reconnect. Runtime classpaths
contain neither the frozen generator release nor executable compatibility classes.

## Existing flow

The existing `P06aHistoricalStateReconnectTest` is a combined generator and consumer. Its
`given` phase uploads and deploys `SimpleStorage` and `StorageAccessHook`, calls
`SimpleStorage.set(424242)`, creates the hook owner and native fixtures, and then freezes the
generated four-node network. Its later phases restart that same generated network in native mode,
stop node 2, advance the other nodes, and reconnect node 2.

| Step | Owner/path | Classification | P07-8 disposition |
| --- | --- | --- | --- |
| Contract uploads, creates, and call | `P06aHistoricalStateReconnectTest.given` | FIXTURE_GENERATION / OBSOLETE_EXECUTION_DEPENDENCY | Remove from a consumer harness |
| Hook/native fixture creation | same `given` phase | FIXTURE_GENERATION | Not a fixture-consumer operation |
| Freeze generated four-node state | same test | FIXTURE_GENERATION | Cannot be replaced by the published fixture |
| Native restart | `FakeNmt.restartNetwork` | STATE_ACTIVATION | Retain when a compatible multi-node fixture exists |
| Stop learner and advance network | `FakeNmt.shutdownWithin`, `nativeAdvancement` | RECONNECT_ORCHESTRATION | Retain |
| Restart learner and await ACTIVE | `TryToStartNodesOp`, `waitForActive` | STATE_SYNCHRONIZATION | Retain |
| Native/rejection checks | final operations | POST_RECONNECT_VALIDATION | Retain |
| Published fixture download/verification | `tools/p06a/fixtures/fetch-preactivation-v065.sh` | FIXTURE_VERIFICATION | Retain |
| Published saved state/PCES installation | external validation workspace | FIXTURE_INSTALLATION | Retain for one-node activation |
| State fingerprint | `hedera-state-validator ... p06a-contract-map-fingerprint` | STATE_ACTIVATION | Retain |

## Published fixture topology

The authenticated manifest explicitly records:

- `fixture_network_id`: `123`;
- `fixture_node_id`: `0`;
- one saved-state lineage:
  `data/saved/com.hedera.services.ServicesMain/0/123/4744`;
- one deterministic public certificate, `s-public-node1.pem`;
- PCES created only by node 0.

The loaded state roster contains exactly one entry, node 0 with weight 1. The fixture is therefore
a one-node activation fixture, not a four-node reconnect fixture. Replicating its directory into
four node workspaces does not create a four-node signed roster and would not constitute a valid
reconnect test. Changing the roster would change the signed state and authenticated fixture.

## Exact-head consumer probe

The exact P07-8 distribution was copied to an isolated temporary workspace. The immutable
fixture data and regenerated compromised-by-design node-0 key were installed, and
`contracts.enabled=false` was explicitly set because the archived configuration predates that
default.

The node loaded round 4744 at root
`9a6ffdddf7dda57154bd84730df9507da8411deafdb61e7c075a886235dbf1b2aff08ded3ffddcfecf5f9b57dd858485`,
replayed 29 PCES events containing two transactions, and transitioned
`REPLAYING_EVENTS -> OBSERVING -> CHECKING -> ACTIVE`. It produced new signed states.

The first immutable state and a later exact-head state both have `STORAGE` cardinality zero. The
required cardinality-one/value-424242 state is not present in the authenticated saved state and is
not produced by its replayable PCES boundary.

## Stop-gate conclusion

There is no valid fixture-consumer four-node reconnect path from the published artifact:

1. the artifact authenticates a one-node roster;
2. its saved state does not contain the required storage value;
3. the existing four-node test obtains that value only by live `ContractCall`;
4. creating a compatible four-node state would be fixture generation, which P07-8 forbids.

The harness separation is therefore blocked by fixture topology and fixture contents, not by the
P07-8 production runtime. No executable class should be restored and no authenticated artifact
should be mutated to bypass this boundary.
