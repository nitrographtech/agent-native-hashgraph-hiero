# P07-3 Full-Runtime Output Delta

Status: **PROPOSED ONLY — no production change performed**

The P07-3 tracer deletion was stopped because live action tracing is required to reproduce the
authenticated fixture corpus. This document records the output delta that deletion would cause; it
does not authorize or represent that delta as implemented.

| Output | Current full-runtime behavior | Behavior if action tracers are removed | Historical compatibility | Fixture and mirror impact |
|---|---|---|---|---|
| Contract result records | Produced from transaction execution result | Retained | Existing and new results remain interpretable | Expected to remain ingestible |
| Contract logs | Included in contract results and neutral log translation | Retained | Existing logs unchanged | Expected to remain ingestible |
| Contract actions | `EvmActionTracer` and `ActionStack` produce ordered PBJ actions | Live action list becomes absent unless a replacement producer is introduced | Existing action sidecars remain readable | Authenticated corpus regeneration loses its three actions; unacceptable without authorization |
| State/storage changes | Built independently from transaction storage accesses in `CallOutcome` | Retained while world-state tracking remains | Existing state-change sidecars remain readable | Expected to remain ingestible; not validated because deletion did not occur |
| Bytecode sidecars | Built independently by contract creation/transaction processors | Retained while contract creation remains | Existing bytecode sidecars remain readable | Expected to remain ingestible; not validated because deletion did not occur |
| Sidecar association and ordering | Builder associates live actions/state/bytecode with parent transaction | Action sidecar portion disappears; remaining producers retain their paths | Existing associations unchanged | New corpus counts differ; immutable historical corpus remains unchanged |
| Block output | Includes PBJ action/state/bytecode trace data when produced | Action trace data disappears for new full-runtime transactions | Existing blocks remain readable | New block semantic fingerprint changes |

## Non-negotiable distinction

Historical interpretation is already neutral and tracer-free:

- `HistoricalContractAction`;
- `HistoricalContractStateChanges`;
- `HistoricalStorageChange`;
- `HistoricalContractBytecode`;
- record, sidecar, block, and mirror readers.

Live full-runtime action production is not tracer-free. Removing that producer is therefore not a
historical-read compatibility problem; it is a fixture-reproducibility and new-output-contract
change.

The expected authenticated historical mirror counts remain three contract actions, two
state-change groups, two bytecode records, and seven sidecar records. They were not rerun against a
modified runtime because no runtime modification was permitted after the stop gate triggered.
