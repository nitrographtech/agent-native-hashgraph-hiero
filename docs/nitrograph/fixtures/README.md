# P06A Historical Fixture

> THIS IDENTITY IS PUBLIC, COMPROMISED BY DESIGN, AND MUST NEVER BE USED OUTSIDE
> THE P06A FIXTURE HARNESS.

This directory describes the release-safe P06A pre-activation corpus. Its historical
state was created by the pinned full node, but its signing identity is intentionally
reproducible and provides no security or production identity authenticity.

Never use this fixture, its identity descriptor, or a generated key on mainnet,
testnet, previewnet, staging, a public network, or a developer-funded network.

Consumers must use `tools/p06a/fixtures/fetch-preactivation-v065.sh`. The fetch path
verifies the committed archive SHA-256 before extraction, rejects extra or altered
files, validates all per-file checksums, and checks the state round, state root, and
roster certificate fingerprint.

The archive does not contain a private key. Activation tests must explicitly invoke
`scripts/p06a/generate-public-fixture-identity.sh` into a new restricted temporary
directory and destroy that generated material after the isolated run.

The generation-only Java entry points are owned by the dedicated `fixture-tooling`
artifact. Neither native nor full runtime distributions package that artifact.

The GitHub Release asset is operationally frozen by its content hash, dedicated
annotated tag, committed manifest, and the project policy that an existing fixture
asset must never be replaced. GitHub Release storage is not inherently immutable.

Regeneration starts from the exact pinned source and committed fixture identity tooling:

```bash
tools/p06a/fixtures/regenerate-preactivation-v065.sh \
  /path/to/pinned-source \
  /path/to/this-repository \
  /new/empty/workspace \
  --enable-public-p06a-fixture-identity \
  --acknowledge-compromised-test-key
```

Node output contains legitimate timing, round, transaction, and signed-state
variability. Deterministic archive construction is proven for a fixed corpus; a new
node generation is validated semantically using schema/map coverage, transaction
sequence, state-root validity, and the compatibility matrix rather than falsely
promising identical node-output bytes.
