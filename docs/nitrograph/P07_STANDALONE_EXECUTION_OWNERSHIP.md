# P07 Standalone Execution Ownership

Status: P07-1 implementation inventory

This inventory distinguishes the deleted, in-process standalone transaction
executor from ordinary full-node transaction handling. The full node,
`FullContractRuntimeProvider`, and `ContractServiceImpl` are intentionally
retained for later waves.

## Production ownership

| Path/type | Purpose and consumers before P07-1 | Fixture use | Classification / disposition |
|---|---|---|---|
| `workflows/standalone/TransactionExecutors` | Public factory for an in-process executor; consumed only by its standalone tests | None in the authenticated P06 generation workflow | `DELETE_IN_P07_1` |
| `workflows/standalone/TransactionExecutor` | Standalone execution functional interface | None | `DELETE_IN_P07_1` |
| `workflows/standalone/ExecutorComponent` | Standalone-only Dagger graph | State-validator used it only as an indirect, uninitialized route to the shared `TransactionChecker`; replaced by direct shared construction | `DELETE_IN_P07_1` |
| `standalone/impl/StandaloneDispatchFactory` | Builds standalone dispatches against caller-supplied state | None | `DELETE_IN_P07_1` |
| `standalone/impl/StandaloneModule` | Standalone-only Dagger providers | None | `DELETE_IN_P07_1` |
| `standalone/impl/StandaloneNetworkInfo` | Synthetic network view for standalone dispatch | None | `DELETE_IN_P07_1` |
| `standalone/impl/NoopVerificationStrategies` | Standalone-only signature strategy | None | `DELETE_IN_P07_1` |
| `fees/StandaloneFeeCalculator` and `StandaloneFeeCalculatorImpl` | Standalone fee-calculation implementation; production consumers absent | None | `DELETE_IN_P07_1` |
| two-argument `SavepointStackImpl.buildHandleOutput` | Convenience overload used only by `TransactionExecutors` | None | `DELETE_IN_P07_1` |
| full-node handle workflow | Consensus transaction handling | Generates the authenticated fixture through normal HAPI submission | `RETAIN_FOR_FULL_NODE` |
| `TransactionChecker` | Shared transaction parsing and validation | Used by node and state-validator | `RETAIN_AS_NEUTRAL_API` |
| historical runtime/store providers and adapters | Read-only saved-state compatibility | Reopens authenticated fixtures | `RETAIN_AS_HISTORICAL_COMPATIBILITY` |

The removed production set comprised nine Java files: seven under
`workflows/standalone` and two standalone fee-calculator files. It had no
native-node startup consumer and no normal full-node startup consumer.

## Test ownership

The standalone implementation tests (`TransactionExecutorsTest`,
`NoopVerificationStrategiesTest`, `StandaloneNetworkInfoTest`,
`StandaloneFeeCalculatorTest`, and `SimpleFeesRecordStreamTest`) are deleted
with their implementation. Shared full-node, record, fee, historical
compatibility, reconnect, and fixture-generation coverage remains with its
existing owner.

## Fixture-generation path

The authenticated P06 fixture generation sequence uses the pinned full node and
the HAPI suites `DiverseStateCreation` and
`HistoricalContractStateFixtureCreation`. Neither suite imports or constructs
`TransactionExecutors`. P07-1 therefore does not regenerate, replace, retag, or
republish any authenticated fixture.

## Composition and metadata

P07-1 removes the standalone JPMS exports and obsolete native-jar exclusions.
No service-loader entry, startup profile, or distribution assembly owns a
standalone provider. The state-validator retains its tooling behavior by
constructing the shared `TransactionChecker` directly, without a standalone
Dagger graph.

