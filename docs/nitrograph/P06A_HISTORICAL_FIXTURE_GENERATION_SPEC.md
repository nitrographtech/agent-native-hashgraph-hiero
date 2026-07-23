# P06A Controlled Historical Fixture Generation

Status: generation specification, approved 2026-07-23.

## Meaning of authenticated

`NODE_GENERATED_AUTHENTIC_TEST_FIXTURE` means an artifact was produced by the pinned
node implementation from committed generation inputs, is attributable to an exact
source commit and software version, and is accompanied by generation logs, an artifact
inventory, and SHA-256 fingerprints. It does **not** mean the artifact has production
mainnet provenance.

PBJ objects, hand-built map fragments, mocked lifecycle state, and copied individual
values are never classified as authenticated node state.

## Immutable generation inputs

| Input | Value |
|---|---|
| Pre-activation source | `ff6490d66994da11af72e1d2f185ec7874fa383a` |
| Pre-activation tree | `7f0bf298d73d386b0483282033eaccdc2268a9b8` |
| Source tag | `anhn-upstream-v0.75.1-baseline` / `v0.75.1` |
| Build profile | Full executable runtime, `:app:run`, `contracts.enabled=true` |
| Activation source | `415608b45a4c7928993cdcc9a1e19e3e75466240` |
| Activation profile | Native-agent runtime, `contracts.enabled=false` |
| Java | Eclipse Temurin 25.0.2+10-LTS |
| Gradle | Wrapper 9.5.0, revision `3fe117d68f3907790f3809f121aa36303a9151f8` |
| OS | WSL2 Linux 6.18.33.2, x86-64 |
| CPU/RAM/swap observed | 14 CPUs / 23 GiB / 8 GiB |
| JVM arguments | Repository defaults; effective command line and heap data captured from the process |
| Node topology | One local node for generation; minimum controlled multi-node topology only for reconnect/state sync |
| Network/node | Local development network, node `0`, gRPC `50211` |
| Genesis configuration | Exact files assembled by the pinned `:app:run` Sync task |
| Hook-state generation | After the initial clean freeze, set only `hooks.hooksEnabled=true` in the generated runtime `application.properties`, restart the same pinned full runtime from its saved state, and run `HistoricalContractStateFixtureCreation` |

The pre-activation corpus is created only by actual consensus-node transaction
processing and a clean freeze/state write. The original corpus is copied read-only
before activation; activation never modifies the original.

## Expected schemas and persisted maps

The pinned v0.75.1 lineage registers:

- V0.49.0: `ContractService.STORAGE` and `ContractService.BYTECODE`.
- V0.65.0: `ContractService.EVM_HOOK_STATES` and persisted
  `ContractService.LAMBDA_STORAGE`.

State identifiers, key literals, codecs, and schema versions must match the definitions
at the pinned source commit. Generation stops if any map cannot be created without
source changes or identifier changes.

## Transaction sequence

The bounded transaction sequence is:

1. Run `DiverseStateCreation` to create contract accounts, deploy bytecode, invoke
   state-writing contract functions, and retain validation metadata.
2. After a clean freeze, enable hooks in the generated runtime configuration, restart
   the same pinned node from that saved state, and run
   `HistoricalContractStateFixtureCreation`. This creates an account allowance hook
   and retains the slot `p06a-slot` with value `p06a-value` through real remote-node
   transactions.
3. Run a contract logging case with sidecar validation enabled.
4. Run one supported Ethereum-format contract transaction.
5. Run native crypto transfer, token, and consensus-topic transactions as continuity
   controls.
6. Submit a freeze transaction and wait for `FREEZE_COMPLETE`.

Exact test selectors, transaction IDs, statuses, and timestamps are captured in the
generation log and corpus manifest. A selector that deletes its generated state is not
eligible.

## Expected outputs

- Node-created signed state and all referenced virtual-map/MerkleDB files.
- State metadata, signatures, round, software version, and root hash emitted by the node.
- Record streams and contract records.
- Contract action/state-change/bytecode sidecars where emitted.
- Block streams where supported by the pinned local configuration.
- Node stdout/stderr, test-client output, effective configuration, metrics, and process
  resource measurements.
- A complete file inventory with byte size and SHA-256 for every retained artifact.

Checksums are intentionally not predicted. They are written only after successful node
generation and then verified by a second independent checksum pass.

## Activation boundary

The boundary is the first startup from an immutable copy of the pre-activation fixture
using source `415608b45a4c7928993cdcc9a1e19e3e75466240` with
`contracts.enabled=false`. The activation round, timestamp, software version, selected
provider, registered schemas, and state root are recorded.

After activation the node must reject every legacy body with
`INVALID_TRANSACTION_BODY`, process native transactions and Coordination Layer events,
write a new state, restart from it, and continue native processing. Historical maps must
remain readable and unchanged except for separately documented migration metadata.

## Regeneration commands

From this repository:

```bash
scripts/p06a/verify-fixture-environment.sh \
  /home/ericf/Code/agent-native-hashgraph/upstream/hiero-consensus-node

scripts/p06a/generate-preactivation-fixture.sh \
  /home/ericf/Code/agent-native-hashgraph/upstream/hiero-consensus-node \
  /tmp/p06a-historical-fixture
```

The generation script prints the follow-up client and freeze commands after the node
becomes ready. `scripts/p06a/fingerprint-fixture.sh` creates the immutable inventory
after clean shutdown.

No generated state is committed until its measured size and the repository's durable
artifact mechanism have been reviewed.

## Validation gates

1. Source and tree match exactly; source worktree is clean.
2. Full generation configuration has contracts enabled.
3. Every retained state map exists in the written node state.
4. Contract bytecode/storage and hook state/storage are non-empty where the node's
   inspection tooling can establish cardinality.
5. State root, round, signatures, logs, records, sidecars, and block files are inventoried.
6. Every checksum verifies on an immutable copy.
7. Activation and restart use native mode and never resolve an executable handler.
8. Negative bodies do not mutate historical maps.
9. Reconnect/state-sync evidence comes from real nodes, not mocks.
10. Large artifacts are not uploaded without an approved durable mechanism.
