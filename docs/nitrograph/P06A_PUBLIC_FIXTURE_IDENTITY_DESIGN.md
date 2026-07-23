# P06A Public Fixture Identity Design

> THIS IDENTITY IS PUBLIC, COMPROMISED BY DESIGN, AND MUST NEVER BE USED OUTSIDE
> THE P06A FIXTURE HARNESS.

The replacement historical corpus uses a fixture-specific signing identity derived
from the committed non-secret descriptor
`P06A_PUBLIC_FIXTURE_IDENTITY_SEED`. The descriptor is deliberately not a wallet
mnemonic and provides no secrecy.

`P06aPublicFixtureIdentity` hashes the descriptor together with the purpose, fixture
network ID 123, and node ID 0. It supplies the resulting test vectors to the existing
deterministic key and certificate APIs. Generated PEM files exist only in a
caller-created temporary directory and are never committed or included in a release.

The generator refuses to run unless all of these conditions are present:

- `--enable-public-p06a-fixture-identity`;
- `--acknowledge-compromised-test-key`;
- environment `P06A_PUBLIC_FIXTURE_WORKFLOW=explicitly-enabled`;
- network ID exactly 123;
- node ID exactly 0;
- an existing, caller-specified output directory;
- no existing public or private output PEM.

The dedicated shell entry point supplies the fixed network and node values and requires
both acknowledgements. Ordinary node startup does not invoke this generator. No
production source set, consensus code, or platform-sdk code is changed.

Generation creates both PEM files temporarily because the pinned node loader requires
the signing private key to match the active roster certificate. Only the public
certificate is copied into genesis configuration and publishable fixture metadata.
The private PEM is recreated explicitly by isolated consumers and destroyed after each
test.

The replacement corpus must be generated from genesis with this public certificate
already in its roster. It is classified
`NODE_GENERATED_AUTHENTIC_TEST_FIXTURE_WITH_PUBLIC_TEST_IDENTITY`: state provenance is
authentic node-generated test evidence, while identity security is intentionally absent.
