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

## Enforcement

`tools/p06b/verify-native-runtime-dependencies.sh` checks the source and assembled native profile.
CI runs it after both distributions are assembled. Explicit exceptions are limited to:

- `FullContractRuntimeProvider`, for the full distribution;
- standalone `TransactionExecutors`;
- contract-implementation and fixture-generation modules outside the shared native boundary.

The check deliberately does not claim physical jar absence. Packaging is governed by
`P06B_PACKAGING_REMOVAL_PLAN.md`.

