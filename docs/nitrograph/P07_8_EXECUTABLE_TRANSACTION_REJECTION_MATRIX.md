# P07-8 Executable Transaction Rejection Matrix

The sole production contract provider is `HistoricalContractRuntimeProvider`. Its handler surface
is fail-closed and performs no mutation, sidecar production, bytecode creation, or storage writes.

| Body/query | Precheck/handle result | Record/block behavior |
| --- | --- | --- |
| ContractCreate | `INVALID_TRANSACTION_BODY` | rejected transaction only; no execution output |
| ContractCall | `INVALID_TRANSACTION_BODY` | rejected transaction only; no execution output |
| EthereumTransaction | `INVALID_TRANSACTION_BODY` | rejected transaction only; no execution output |
| executable ContractUpdate | `INVALID_TRANSACTION_BODY` | rejected transaction only |
| executable ContractDelete | `INVALID_TRANSACTION_BODY` | rejected transaction only |
| contract execution queries | unsupported at the historical provider boundary | no execution result or sidecar |

Fee behavior follows the existing invalid-body path. Wire bodies remain parseable and historical
records remain readable. No body is reinterpreted as a native operation.
