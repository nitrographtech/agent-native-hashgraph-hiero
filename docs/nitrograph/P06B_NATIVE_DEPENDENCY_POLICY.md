# P06B Native Dependency Policy

## Permitted directions

- Native startup → `ContractRuntimeProvider` → `HistoricalContractRuntimeProvider`.
- Historical provider → retained schema service and fail-closed handlers.
- Shared record/block code → PBJ values and `Historical*` neutral values.
- Full runtime and fixture generation → contract implementation → neutral/PBJ boundaries.
- Standalone execution → full provider, while it remains excluded from native node startup.

## Prohibited directions

- `Hedera` → `ContractServiceImpl`.
- Native Dagger modules → full provider, contract implementation, or system-contract bindings.
- Shared historical translation → Besu, Tuweni, `MessageFrame`, world state, or code objects.
- Native authorization/shared utilities → contract-implementation conversion utilities.
- Native mode → executable metrics, native EVM verification, executable handlers, or system
  contracts.
- Native application jar → full runtime provider/factory, full store factory, standalone executor,
  embedded contract implementation, Besu, or Tuweni classes.
- Any native library jar → Besu/Tuweni packages, executable EVM/world-state/system-contract
  classes, or executable-only JNI libraries.
- Native module descriptors or services → executable providers or contract implementation.

## Enforcement

`tools/p06b/verify-native-runtime-dependencies.sh` checks the source and assembled native profile.
CI runs it after both distributions are assembled. Explicit exceptions are limited to:

- `FullContractRuntimeProvider`, for the full distribution;
- standalone `TransactionExecutors`;
- contract-implementation and fixture-generation modules outside the shared native boundary.

The check validates physical jar, class, module, service, and native-library absence from the exact
native distribution. `test-native-binary-policy.sh` proves rejection of deliberately injected
prohibited jars, classes, service entries, module requirements, and native libraries.

P06B-7A additionally verifies the exact native application jar, its profile-specific service
metadata, required native classes, absence of the combined full module descriptor, and an
intentional prohibited-class injection that must fail.

P06B-7 adds `probe-native-classpath.sh`, which uses the exact isolated distribution to prove
required historical APIs and factories load, executable classes do not load, ServiceLoader exposes
only historical factories, and neutral secp256k1 account verification remains functional.
