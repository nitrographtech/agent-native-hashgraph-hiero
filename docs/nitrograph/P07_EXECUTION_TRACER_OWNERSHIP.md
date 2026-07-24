# P07 Execution Tracer Ownership

Status: **BLOCKED — census complete; production deletion not started**  
Base: `agent-native@d6c5a7e9b44f32ac5d8f8d7ee686687792c6b2ac`  
Wave: P07-3

## Stop-gate conclusion

The authenticated P06 fixture-generation path requires live execution tracing to reproduce its
contract-action sidecars. The immutable authenticated corpus contains three contract actions and
seven sidecar records. Live actions are produced only by `EvmActionTracer` and its `ActionStack`.
Removing the tracer producers would therefore change the documented reproducibility contract even
though existing historical action sidecars would remain readable.

The P07-3 instruction explicitly requires stopping before deletion when authenticated fixture
regeneration requires live tracers. No production source, test, build, JPMS, Dagger, service,
configuration, metric, fixture, or output file was changed by this checkpoint.

## Measured ownership

The contract implementation contains 20 production files that directly name
`ActionSidecarContentTracer`, `AddOnEvmActionTracer`, or `EvmActionTracer`, totaling 254,199 bytes.
Twelve implementation test files directly name these types, totaling 178,367 bytes. These are
dependency-impact measurements, not proposed deletion counts: several files contain core execution
behavior that cannot be deleted in the tracer wave.

| Component | Source owner and role | Reachability | Output dependency | Classification | P07-3 disposition |
|---|---|---|---|---|---|
| `ActionSidecarContentTracer` | `impl/exec`; Besu `OperationTracer` plus PBJ action contract | Full runtime and fixture generation; native absent | Contract actions | EXECUTION_TRACER_CORE | DELETE_IN_P07_3 only after fixture prerequisite |
| `EvmActionTracer` | `impl/exec/tracers`; owns `ActionStack` and callback handling | Full runtime and fixture generation; native absent | Sole live PBJ action producer | ACTION_TRACE_PRODUCER | BLOCKED |
| `AddOnEvmActionTracer` | `impl/exec/tracers`; delegates to the primary tracer and add-ons | Full runtime only; native absent | Contract actions and add-on callbacks | TRACER_AGGREGATOR | BLOCKED |
| `NoTracer` | `impl/exec/tracers`; no-op query tracer | Full query runtime; native absent | No action output | EXECUTION_TRACER_CORE | Delete with tracer API after prerequisite |
| `ActionStack` and action validation | `impl/exec/utils`; frame/action hierarchy and validation | Full runtime and fixture generation | Action ordering, depth, revert/finalization | ACTION_TRACE_PRODUCER | BLOCKED; do not split casually |
| `TransactionModule` | `impl/exec`; constructs `EvmActionTracer(new ActionStack())` | Full runtime and fixture generation | Transaction tracer binding | TRACER_AGGREGATOR | Remove binding after prerequisite |
| `QueryModule` | `impl/exec`; provides a query `ActionSidecarContentTracer` | Full runtime | Query execution callbacks | TRACER_AGGREGATOR | Remove binding after prerequisite |
| `ContractServiceComponent` | implementation Dagger component; binds add-on tracer supplier | Full runtime | Optional add-on action tracing | TRACER_AGGREGATOR | Remove binding after prerequisite |
| `ContractServiceImpl` | passes tracer supplier into the component | Full runtime and fixture generation | Composition only | FULL_RUNTIME_REQUIRED | Retain; later wave |
| `ContextTransactionProcessor` | selects primary/add-on tracer and invokes transaction processor | Full runtime and fixture generation | Actions; independently emits bytecode | FULL_RUNTIME_REQUIRED | Retain execution; remove tracer branch only after prerequisite |
| `ContextQueryProcessor` | selects no-op/query tracer | Full runtime | Execution callback contract | FULL_RUNTIME_REQUIRED | Retain; requires bounded callback replacement |
| `FrameRunner` | invokes origin/finalization callbacks and passes tracer into EVM processing | Full runtime and fixture generation | Action lifecycle | FULL_RUNTIME_REQUIRED | Mixed execution/tracing; separation prerequisite |
| `TransactionProcessor` | accepts tracer in core transaction execution signature | Full runtime and fixture generation | Action callback propagation | FULL_RUNTIME_REQUIRED | Signature separation prerequisite |
| `HederaEVM` / `HederaEvmTransactionProcessor` | pass Besu operation tracer through execution | Full runtime and fixture generation | Callback propagation | FULL_RUNTIME_REQUIRED | Retain execution; later EVM wave |
| `HederaEvmTransactionResult` | calls `tracer.contractActions()` when sidecars are enabled | Full runtime and fixture generation | Materializes PBJ actions | ACTION_TRACE_PRODUCER | Must change only with authorized output delta |
| `CustomMessageCallProcessor` | casts the Besu callback to action tracer for system/precompile actions | Full runtime, system contracts, fixture generation | Nested/system action hierarchy | ACTION_TRACE_PRODUCER | Coupled to Wave 4; do not delete now |
| Bonneville classes | experimental EVM callback propagation | Full runtime only | Tracer callback propagation | RETAIN_FOR_LATER_WAVE | Defer to EVM-engine removal |
| PBJ `ContractAction` | HAPI/PBJ model | Historical and mirror permanent | Historical action representation | NEUTRAL_OUTPUT_MODEL | Retain permanently |
| `HistoricalContractAction` | neutral shared translator model | Native and historical | Sidecar/block translation | HISTORICAL_COMPATIBILITY_ONLY | Retain permanently |

No tracer-specific service-loader entry or standalone binding remains after P07-1. The relevant
live construction is Dagger/provider composition inside the full contract implementation.
`contracts.sidecars` and `FeatureFlags.isSidecarEnabled()` govern more than a removable metric; they
control live historical output and must not be deleted while fixture reproduction remains required.

## Exact output provenance

### Contract actions

```text
Besu EVM callbacks / MessageFrame transitions
  -> ActionSidecarContentTracer
  -> EvmActionTracer + ActionStack
  -> HederaEvmTransactionResult.maybeActionsFrom()
  -> List<PBJ ContractAction>
  -> CallOutcome.setCommonFieldsOn()
  -> RecordStreamBuilder.addContractActions() / addActions()
  -> record sidecar and block EvmTraceData
  -> neutral HistoricalContractAction translation
  -> official mirror ingestion
```

`HederaEvmTransactionResult.maybeActionsFrom()` returns
`tracer.contractActions()` when action sidecars are enabled. No independent live action producer
was found.

### State and storage changes

```text
world-state/storage access tracking
  -> TxStorageUsage.accesses()
  -> CallOutcome.setCommonFieldsOn()
  -> PBJ ContractStateChanges
  -> neutral HistoricalContractStateChanges / HistoricalStorageChange
  -> sidecar and block output
  -> official mirror ingestion
```

State/storage output is not produced by `EvmActionTracer`. It remains coupled to executable
world-state mutation tracking, which belongs to the later world-state wave.

### Bytecode

```text
contract creation / transaction processing
  -> CustomContractCreationProcessor or ContextTransactionProcessor
  -> PBJ ContractBytecode
  -> neutral HistoricalContractBytecode
  -> sidecar and block output
  -> official mirror ingestion
```

Bytecode output is independently produced and is not a reason to retain the action tracer.

## Fixture impact

`HistoricalContractStateFixtureCreation` invokes the pinned full node and executes the contract
transactions that create the authenticated historical state and stream corpus. The published
fixture is immutable and remains unchanged:

- tag: `p06a-fixture-preactivation-v065-ff6490d-round4744`;
- source: `ff6490d66994da11af72e1d2f185ec7874fa383a`;
- round: `4744`;
- archive SHA-256: `5e40a5c530d27ad77e41c06ab3a5b73df7b7be222f3829590f770e35142bffb0`;
- manifest SHA-256: `e82234205950328f4f6940c588f692d873bd2acff5a642d3167a88eba3f5dcc2`.

Existing fixture consumption is tracer-free. Reproducing its action-sidecar corpus is not:
the expected authenticated semantics include three contract actions. The generator therefore
classifies as `FIXTURE_REQUIRES_LIVE_TRACERS`, not `FIXTURE_USES_PRECOMPUTED_OUTPUT`.

No fixture was regenerated, replaced, republished, or resigned during this checkpoint.

## Smallest prerequisite for deletion

Wave 3 requires a separate authorization choosing one compatibility contract:

1. retain the current tracer producer in an explicitly fixture-generation-only executable artifact
   while removing it from the normal full runtime;
2. retire live regeneration of authenticated action sidecars and treat the immutable published
   corpus plus verification tooling as the permanent oracle; or
3. authorize a new fixture generation/output contract that no longer requires live action
   sidecars.

Option 1 best preserves current reproducibility but is an ownership move into executable fixture
tooling and was not authorized in P07-3. Until one option is approved, Wave 3 is **BLOCKED** and
Wave 4 must not begin.
