# P07 four-node post-write fixture reproducibility

Two publishable runs used clean extractions of
`p07-fixture-compat-v1-source.tar.gz` (`6af7596d365e62561df913032daaaf4437c3eafeb75e567e33057e8eab5b7a7a`)
at source `64da043f766da29d0fd3e20e3f51051fdd31f5a1`, Eclipse Temurin
25.0.3+9, Gradle 9.5.0, Linux x86_64, and the guarded four-node public-fixture identity workflow.

The generation command was:

```text
P06A_PUBLIC_FIXTURE_NETWORK=four-node-explicitly-enabled
P06A_PUBLIC_FIXTURE_WORKFLOW=explicitly-enabled
./gradlew :test-clients:testSubprocess --tests com.hedera.services.bdd.suites.reconnect.P06aHistoricalStateReconnectTest --no-daemon --stacktrace
```

Run one captured round 2143 with root
`0c3b973439f127093ad1d64bd9777d9a365a6d18522e6e46c6bd0fa41fa9f4e8eba8b4eac33b0cda2bfa82cf175a0894`.
Run two captured round 3224 with a different root, as expected from consensus timing and platform
metadata. Both runs had nodes 0–3 with weight one and identical public identity fingerprints.

Both semantic fingerprints were identical:

| Map | Cardinality | Aggregate SHA-256 |
|---|---:|---|
| STORAGE | 1 (`424242`) | `4498f1861935388100d439abcd8124cb906dc479f990c91a4b5a3d6995dd892f` |
| BYTECODE | 2 | `204633a18de2a97441e56adae79ce51656496582833a906a69471e24b87091ab` |
| EVM_HOOK_STATES | 1 | `32ffae8553e2c27c2a85e6071b9d6eddb85bc07a4a175ed9047d0c2cdb9484f1` |
| LAMBDA_STORAGE | 1 | `6359602eb1ab3cb0ef54e1eeddfb8bfd5bcd89a52d86afb191b1cc8cf419056b` |

The combined protected inventory is
`5f74ae63338dfb5510d0dc09a7afafe64f5ee00f9d0c7ed5c040cb5fe82dfa48`.
The runs are semantically equivalent, not byte-deterministic. Run one is selected because it is the
first clean publishable run that passed generation, protected-map inspection, and exact-head
consumer/reconnect validation.
