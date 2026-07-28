# P07 pinned mirror importer regression

This directory commits the mirror importer harness used by P07-11E. It removes
the former dependency on an untracked test in a local mirror-node checkout.

## Pinned input

The only supported importer source is
`hiero-ledger/hiero-mirror-node@834a7a1cb9204b02c192098c60654d08eb85bc2a`.
The runner creates a detached temporary worktree at that commit, copies the
committed `P06bFixtureRegressionTest.java` into it, and runs the official
importer integration test with its Testcontainers PostgreSQL database. It does
not modify importer production source or schema.

```sh
git clone https://github.com/hiero-ledger/hiero-mirror-node.git /path/to/hiero-mirror-node
git -C /path/to/hiero-mirror-node checkout 834a7a1cb9204b02c192098c60654d08eb85bc2a
```

Fixture A is release
`p06a-fixture-preactivation-v065-ff6490d-round4744`. Fixture B is release
`p07-four-node-postwrite-v065-storage424242-v1`. Verify the immutable fixture
archive/manifest before using its record and block directories:

```sh
tools/p06a/fixtures/verify-preactivation-v065.sh FIXTURE_A.tgz EMPTY_EXTRACTION_DIR
tools/p07/verify-four-node-postwrite-fixture.sh FIXTURE_B_DIRECTORY
```

The historical `3698/8968` native totals were a local observation, not a
deterministic specification. CI run `30322277799` did not generate or retain
that corpus. The authenticated replacement is release
`p07-native-corpus-689e32ac-v1`; see `native-corpus/PROVENANCE.md`.

Retrieve and verify it twice with:

```sh
first=$(mktemp -d)
second=$(mktemp -d)
tools/p07/retrieve-native-mirror-corpus.sh "$first"
tools/p07/retrieve-native-mirror-corpus.sh "$second"
cmp "$first/p07-native-corpus-689e32ac-v1/MANIFEST.sha256.tsv" \
    "$second/p07-native-corpus-689e32ac-v1/MANIFEST.sha256.tsv"
```

The archive checksum, embedded per-file manifest, exact file inventory, and
absence of sidecars are validated before importer execution.

## Run

```sh
MIRROR_IMPORTER_DIR=/path/to/hiero-mirror-node \
P07_FIXTURE_A_RECORDS=/path/to/fixture-a/data/recordStreams \
P07_FIXTURE_A_BLOCKS=/path/to/fixture-a/data/blockStreams \
P07_FIXTURE_B_RECORDS=/path/to/fixture-b/records \
P07_FIXTURE_B_BLOCKS=/path/to/fixture-b/blocks \
P07_NATIVE_RECORDS=/path/to/exact-head/node0/data/recordStreams \
P07_MIRROR_OUTPUT_DIR=/tmp/p07-mirror-output \
tools/p07/run-pinned-mirror-importer-regression.sh
```

`P07_MIRROR_CORPORA=fixtures` or `P07_MIRROR_CORPORA=native` runs a
deterministic subset; the default is `all`.

Docker access is required by the importer. On a host where access is granted
through a group, wrap the command with the host's normal group launcher, for
example `sg docker -c '…'`.

The runner verifies the committed expectations:

| Corpus | Record files | Transactions | Results | Logs | Actions | Attached sidecars | Blocks |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Fixture A | 11 | 754 | 3 | 1 | 3 | 5 (7 semantic records in the certified corpus) | 139 |
| Fixture B | 34 | 861 | 3 | 1 | 0 | 4 | 131 |
| Immutable native | 3639 | 8841 | 0 | 16 record-shaped logs | 0 | 0 execution sidecars | not applicable |

Thus historical contract-shaped fixture content must remain readable, while
the exact-head corpus must contain no successful removed-contract result,
action, or execution sidecar. The positive transaction counts also prove the
retained native record corpus remains interpretable.

For the native corpus, the importer also requires positive evidence for every
surface in `native-corpus/feature-inventory.tsv`. The generated `summary.tsv`
is deterministic and the runner prints its
SHA-256. Raw JUnit XML files and their run-specific SHA-256 values may also be
retained as execution evidence. A mismatch in any count, a missing signed
record or complete block, a sidecar hash mismatch, an importer/schema failure,
or a checkout at any other commit fails the run.

Run the committed corruption control with:

```sh
tools/p07/test-pinned-mirror-importer-regression-policy.sh
tools/p07/test-native-mirror-corpus-policy.sh
```

It first accepts the certified summary, then changes Fixture A's transaction
expectation from `754` to `755` and requires validation to fail. The native
control downloads the artifact twice, compares hashes and manifests, removes a
required record, and injects a prohibited sidecar; both mutations must fail.
