# P06B-7 Native Packaging Blocker

Status: **BLOCKED — API/source-set split approval required**

## Controlled exclusion probe

From the exact pre-change native distribution, a temporary classpath was created with these jars
excluded:

- `app-service-contract-impl-*`
- `besu-*`
- `evm-*`
- `tuweni-*`

No repository artifact was changed. Results:

| Probe | Result |
|---|---|
| `Hedera` | available |
| `HistoricalContractRuntimeProvider` | available |
| `RecordStreamBuilder` | **unavailable** |
| Besu `EVM` | unavailable, as desired |
| Tuweni `Bytes` | unavailable, as desired |
| `FullContractRuntimeProvider` | still available from combined app jar |
| standalone `TransactionExecutors` | still available from combined app jar |

`RecordStreamBuilder` failed with:

`NoClassDefFoundError: com/hedera/node/app/service/contract/impl/records/ContractCreateStreamBuilder`

## Decision

The stop condition is met: historical record/PBJ compatibility interfaces are inseparable from the
executable implementation jar without a broader API split. The native application jar also combines
native, full-provider, and standalone classes, so binary absence cannot be proven without a
source-set or module split.

No production Gradle, Dagger, module descriptor, runtime assembly, or jar filtering change was made.
Proceeding by filename exclusion would make native record construction unloadable and would violate
the historical stream compatibility gate.

## Required authorization

Authorize a bounded P06B-7A prerequisite:

1. extract contract record-builder interfaces into an implementation-neutral API module;
2. extract retained historical map store interfaces/adapters into the compatibility API;
3. split the application artifact/source set so full-provider and standalone classes are excluded
   from the native jar;
4. preserve packages/wire/state identifiers where relocation is not required;
5. rerun the complete P06A/P06B compatibility matrix before excluding any executable jar.

