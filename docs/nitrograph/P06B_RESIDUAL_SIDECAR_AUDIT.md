# P06B Residual Sidecar Audit

Base: `agent-native@15e912e668df1431e3fbdc6f7443c23eeb21fdaf`

| Boundary | Shared input | Shared representation | Executable producer | Classification |
|---|---|---|---|---|
| Logs | PBJ result/log data | `HistoricalLog` and neutral bloom | full runtime only | NEUTRALIZED |
| Contract results | PBJ `EvmTransactionResult` | `HistoricalContractResult` | full runtime only | NEUTRALIZED |
| Actions | PBJ `ContractAction` | `HistoricalContractAction` | full runtime tracer only | NEUTRALIZED |
| State changes | PBJ `ContractStateChanges` | `HistoricalContractStateChanges` | full world-state tracker only | NEUTRALIZED |
| Storage changes | PBJ storage changes | `HistoricalStorageChange` | full world-state tracker only | NEUTRALIZED |
| Bytecode | PBJ `ContractBytecode` | `HistoricalContractBytecode` | full creation processor only | NEUTRALIZED |
| Sidecar metadata/hash | PBJ stream metadata | PBJ native | none | PBJ_NATIVE |
| Parent association | consensus timestamp and builder order | PBJ native | none | PBJ_NATIVE |
| Block trace inputs | PBJ `ExecutedInitcode`/slot usage | PBJ native and neutral actions | full producer only | FULL_RUNTIME_PRODUCER_ONLY |

## Closure result

`RESIDUAL_SHARED_EXECUTABLE_DEPENDENCIES: 0`

No shared sidecar path directly consumes Besu, Tuweni, `MessageFrame`, mutable world-state objects,
runtime code objects, or executable tracer values. Contract-implementation record-builder
interfaces remain compile-time ABI dependencies and are tracked as data-only module-cleanup debt;
they do not carry executable objects across the neutral sidecar boundary.

No additional wrapper is warranted for PBJ-native metadata or associations.

