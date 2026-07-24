# P06B-7 Native Packaging Blocker

Status: **RESOLVED by P06B-7A**

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

## P06B-7A resolution

The approved prerequisite moved the six record-builder interfaces, with unchanged FQCNs, into
`app-service-contract`; added neutral read-only adapters for all retained maps; selected contract
stores and runtime providers through profile-specific factories; and produced a filtered native
application jar distinct from the full application jar.

The controlled exclusion probe now reports:

| Probe | Result without implementation/Besu/EVM/Tuweni jars |
|---|---|
| `Hedera` | available |
| `RecordStreamBuilder` | available |
| all six neutral builder interfaces | available |
| neutral retained-map interfaces/adapters | available |
| `HistoricalContractRuntimeProvider` | available |
| `FullContractRuntimeProvider` | unavailable |
| `TransactionExecutors` | unavailable |
| implementation `ContractStateStore` | unavailable |
| Besu `EVM` | unavailable |
| Tuweni `Bytes` | unavailable |

This resolves the exact `NoClassDefFoundError` blocker. It does not claim completion of P06B-7:
the native distribution still carries executable dependency jars and the combined full module
descriptor remains a full-artifact concern.

## Authorized follow-on

P06B-7 may now continue with:

1. extract contract record-builder interfaces into an implementation-neutral API module;
2. extract retained historical map store interfaces/adapters into the compatibility API;
3. split the application artifact/source set so full-provider and standalone classes are excluded
   from the native jar;
4. preserve packages/wire/state identifiers where relocation is not required;
5. rerun the complete P06A/P06B compatibility matrix before excluding any executable jar.
