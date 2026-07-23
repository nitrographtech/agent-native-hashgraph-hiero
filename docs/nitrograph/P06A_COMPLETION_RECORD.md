# P06A Completion Record

Status: **COMPLETE**

P06A extracted an implementation-neutral, read-only historical contract-state
boundary and proved that authenticated historical state remains readable,
migratable, restartable, reconnectable, synchronizable, and interpretable without
making legacy contract execution available in Nitrograph native mode.

## Source lineage

- Upstream baseline commit: `ff6490d66994da11af72e1d2f185ec7874fa383a`
- P05/native-agent base: `ffdea9ae4d5508033d06801b3abffad0a3d10445`
- Final P06A merge: `f7b8dc0cde6b641767ff7836e3d22d4762fcabdf`
- Full upstream ancestry is preserved. No P06A history was rewritten.

| PR | Tested head | CI run | Merge commit |
| --- | --- | --- | --- |
| #2 — provider extraction | `369a1f99ac59b46ca50b3157d9e7cdf971eceb02` | `29979019459` | `415608b45a4c7928993cdcc9a1e19e3e75466240` |
| #3 — authenticated fixture and activation | `9ffa7f31604bfbba7ecc4e4fe0b695597122f69f` | `30023268855` | `eb016ede9e4b5c02208566ed2319e4042086bd71` |
| #4 — map-integrity gates | `808d665cddcbffbc47adbf8af22f4cee3e2ec45d` | `30025365395` | `df183ff8caceba58361849961e3d43b4b376dc1f` |
| #5 — storage, reconnect, synchronization, and mirror gates | `54f2ad2b33aab8069e6ed12ed6eb92e045908d0a` | `30037670774` | `f7b8dc0cde6b641767ff7836e3d22d4762fcabdf` |

All four exact-head CI runs passed. Each PR used a merge commit.

## Authenticated fixture

- Classification:
  `NODE_GENERATED_AUTHENTIC_TEST_FIXTURE_WITH_PUBLIC_TEST_IDENTITY`
- Tag: `p06a-fixture-preactivation-v065-ff6490d-round4744`
- Release ID: `RE_kwDOTgyWPc4VYsvU`
- Archive asset ID: `RA_kwDOTgyWPc4dDHT3`
- Manifest asset ID: `RA_kwDOTgyWPc4dDHT1`
- Archive:
  `nitrograph-p06a-preactivation-v065-ff6490d-round4744.tar.gz`
- Archive SHA-256:
  `5e40a5c530d27ad77e41c06ab3a5b73df7b7be222f3829590f770e35142bffb0`
- Per-file manifest SHA-256:
  `e82234205950328f4f6940c588f692d873bd2acff5a642d3167a88eba3f5dcc2`
- Pre-activation round: `4744`
- Pre-activation state root:
  `9a6ffdddf7dda57154bd84730df9507da8411deafdb61e7c075a886235dbf1b2aff08ded3ffddcfecf5f9b57dd858485`
- Post-activation round: `9221`
- Post-activation state root:
  `6c699162be610a22d0a7aeb75f21cdeabb165fad1bed5a80af49ca2d26eb20347ff79f00f0cf29afea37849ea5de125e`

The historical state is authentically node-generated. Its fixture identity is
public, compromised by design, isolated to the P06A harness, and provides no
production identity assurance. No generated private PEM is published.

## Compatibility result

`ContractService.STORAGE`, `ContractService.BYTECODE`,
`ContractService.EVM_HOOK_STATES`, and `LAMBDA_STORAGE` remain registered,
readable, and unchanged after activation, rejected execution attempts, save,
restart, replay, signed-state loading, reconnect, and synchronization. Non-empty
`STORAGE` retained cardinality one and decoded value `424242`; key and value
inventory hashes remained identical.

Native account and asset operations and Coordination Layer publication pass before
and after restart and reconnect. A real four-node teacher/learner reconnect
transferred and validated the authoritative signed state, and the retained map
fingerprints matched.

The negative execution matrix is deterministic:

| Body | Result |
| --- | --- |
| Contract create | `INVALID_TRANSACTION_BODY` |
| Contract call | `INVALID_TRANSACTION_BODY` |
| Contract update | `INVALID_TRANSACTION_BODY` |
| Contract delete | `INVALID_TRANSACTION_BODY` |
| Ethereum transaction | `INVALID_TRANSACTION_BODY` |

`HistoricalContractStateService` is selected in Nitrograph native mode. No
executable contract provider resolves. Instrumented native activation, restart,
replay, rejection, reconnect, and synchronization loaded zero prohibited EVM
engine classes and zero Besu/Tuweni classes.

The official mirror importer at
`hiero-ledger/hiero-mirror-node@834a7a1cb9204b02c192098c60654d08eb85bc2a`
successfully ingested authenticated historical and post-activation records,
sidecars, and 328 complete block-stream files using its PostgreSQL and Redis
Testcontainers environment. Historical contract results, logs, actions, bytecode
sidecars, state changes, and Ethereum-format output remain interpretable. No false
successful post-activation contract result was created.

## Frozen boundary

Historical contract state is read-only and non-executable in Nitrograph. Physical
EVM/Besu artifact removal has **not** occurred; full-runtime compatibility paths
remain until later P06B and removal gates pass.

P06A changed no consensus or platform-sdk source, persisted state key, state ID,
codec, serialization identifier, or schema version.

No licensing finding is waived by this completion record. The remaining
licensing and binary-distribution inventory requirement is preserved as an
explicit prerequisite of the physical-removal gate.

