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

This resolves the exact `NoClassDefFoundError` blocker. P06B-7 subsequently removes all executable
dependency jars from the native distribution while retaining the combined full module descriptor
only in the full artifact.

## Authorized follow-on

P06B-7 packaging status: implementation complete; exact-head lifecycle and mirror evidence are the
remaining merge-readiness gates.
