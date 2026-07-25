# P07-8 Executable Transaction Rejection Matrix

Exact-head four-node consumer validation submitted explicit `ContractCreate` and `ContractCall`
bodies after restart/reconnect. Both returned `INVALID_TRANSACTION_BODY`; no registry population,
contract deployment, compatibility runtime, or mutable state was used. The consumer suite also
proved retained account, asset, and Coordination Layer transactions remain operational. Its test
XML SHA-256 is
`20de5114ba400044dc19183b0746abbc1b29cf3639a85f74b355ad84f5156231`.

Update/delete/query and Ethereum body rejection remain covered by the production rejection-matrix
tests because the remote HAPI helpers for those bodies require executor-era registry/relayer setup
that a consumer-only harness intentionally does not recreate.

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

The authenticated fixture-consumer probe did not submit new executable bodies. Its two bundled
PCES transactions produced zero replayed consensus rounds and did not populate `STORAGE`; this is
recorded as a fixture stop-gate result, not as successful rejection-matrix validation.
