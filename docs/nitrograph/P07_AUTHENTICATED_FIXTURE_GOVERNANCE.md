# P07 authenticated fixture governance

Authenticated fixtures are immutable, additive compatibility witnesses. They are not production
genesis states and their purpose cannot be broadened without independent evidence.

## Fixture classes

- `PREACTIVATION_SINGLE_NODE`: schema-activation and historical-provider boundary.
- `POST_WRITE_MULTI_NODE`: committed historical maps, restart, reconnect, and synchronization.
- `FUTURE_SCHEMA_BOUNDARY`: a future schema transition with an explicitly bounded purpose.
- `FUTURE_RECONNECT_BOUNDARY`: a future topology/reconnect boundary.

## Required metadata

Every fixture records its ID and purpose; source repository and commit; generator release; software
and schema versions; roster, node count, IDs, and weights; capture round and state root; protected
map fingerprints; archive, manifest, records, sidecars, and blocks hashes; public signing
identities; generation and verification commands; private-key handling; publication location; and
supersession policy.

## Rules and authority

Fixtures are immutable and additive. An old release, tag, archive, or manifest is never replaced.
Preactivation and post-write fixtures are distinct lifecycle boundaries. Only public certificates
and signatures may be published; private keys are never committed, logged, or released. Each
fixture requires an independent clean consumer verification.

The frozen `p07-executable-fixture-compat-v1` release is authorized only to generate compatibility
fixtures from its pinned source. It is not a production runtime and must never enter a production
application classpath. Future fixtures require their own documented authority and cannot silently
supersede either P07 fixture.
