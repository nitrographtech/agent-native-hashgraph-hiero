# P07 runtime tracer deletion inventory

## Status

`BLOCKED` before production deletion.

P07-3A moved the concrete action producer into fixture tooling, but the executable contract
implementation still owns the callback protocol that lets that producer observe execution. Removing
that protocol now crosses the explicit P07-3 stop boundary into message processing, system-contract
execution, Bonneville, and transaction-result construction.

## Runtime closure

| Runtime owner | Purpose and consumers | Classification |
|---|---|---|
| `exec/ActionSidecarContentTracer.java` | Extends Besu `OperationTracer` with Hedera callbacks consumed by `FrameRunner`, message processors, HEVM, Bonneville, and result construction | `UNKNOWN` — cannot delete without a new fixture execution adapter |
| `exec/ActionSidecarContentTracerFactory.java` | ServiceLoader seam used only by fixture tooling | `DELETE_NOW` only after the callback protocol is moved out of runtime |
| `exec/tracers/NoTracer.java` | Runtime no-op implementation required by current processor signatures | `DELETE_NOW` only after signatures accept `OperationTracer.NO_TRACING` |
| `ContractServiceImpl` | Loads the fixture factory in an augmented tooling distribution | `DELETE_NOW` only after fixture tooling owns execution composition |
| `ContractServiceComponent`, `TransactionModule`, `QueryModule` | Dagger ownership and no-tracer binding | `DELETE_NOW` after processor signatures are neutralized |
| `ContextTransactionProcessor`, `ContextQueryProcessor`, `TransactionProcessor`, `FrameRunner` | Carry the Hedera tracer through transaction and frame execution; `FrameRunner` invokes origin, sanitize, and not-executing callbacks | `UNKNOWN` — fixture callback integration is execution-path code |
| `CustomMessageCallProcessor` | Emits precompile/system-contract, lazy-creation, exceptional-halt, and not-executing callbacks | `UNKNOWN` — explicit system-contract stop condition |
| `HederaEVM`, `HederaEvmTransactionProcessor`, `HederaEvmTransactionResult` | Adapts Besu tracing and extracts PBJ actions into the result | `UNKNOWN` — fixture output would lose three actions |
| `bonneville/BEVM`, `BonnevilleEVM`, `CallManager`, `TopXTN` | Propagate the Hedera tracer through the alternate EVM implementation | `UNKNOWN` — broader EVM redesign boundary |
| `module-info.java` | `uses` factory plus tracer package export/open | `DELETE_NOW` after the source closure is removed |

The direct closure is 20 production files and 246,075 bytes. Ten directly coupled test files total
166,166 bytes.

## Fixture ownership

The following remain correctly owned by `fixture-tooling` and must not be deleted:

- `EvmActionTracer`
- `ActionStack`
- `ActionWrapper`
- `ActionsHelper`
- `FixtureActionTracerFactory`

They depend on the runtime callback protocol above. The runtime therefore still has a compile-time
and execution-callback responsibility for the fixture tracer, even though normal runtime artifacts
do not package or construct its implementation.

## Blocking dependency chain

```text
fixture-tooling EvmActionTracer
  -> runtime ActionSidecarContentTracer callback contract
  -> FrameRunner
  -> CustomMessageCallProcessor
  -> system-contract/precompile and lazy-creation callbacks
  -> HederaEVM / Bonneville
  -> HederaEvmTransactionResult.contractActions()
  -> PBJ ContractAction
```

Deleting `ActionSidecarContentTracer` without replacing this chain makes the fixture generator
produce zero actions. Moving the chain into fixture tooling requires subclassing or replacing
runtime message/frame processors and touches system-contract and world-state execution. Both are
explicit stop conditions for P07-3.

## Required prerequisite

Choose one bounded architecture before resuming deletion:

1. Authorize a fixture-only execution adapter that owns fixture variants of the frame/message/result
   callback points, with no runtime dependency on fixture tooling; or
2. Complete the system-contract and EVM processor removal waves first, then delete the now-unused
   runtime callback protocol; or
3. Authorize a broader combined tracer/system-contract execution-boundary wave.

No production source, test, Dagger, Gradle, JPMS, service metadata, PBJ model, persisted state, or
fixture was changed at this checkpoint.
