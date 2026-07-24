# P07-3 Full-Runtime Output Delta

Status: **P07-3A IMPLEMENTED; RESUMED P07-3 DELTA STILL PENDING**

P07-3A removed the concrete live action producer from normal runtime ownership and retained it only
in fixture tooling. The normal full distribution therefore no longer emits action sidecars; the
transient fixture distribution still does. Remaining callback SPI removal belongs to resumed
P07-3.

| Output | Current full-runtime behavior | Behavior if action tracers are removed | Historical compatibility | Fixture and mirror impact |
|---|---|---|---|---|
| Contract result records | Produced from transaction execution result | Retained | Existing and new results remain interpretable | Expected to remain ingestible |
| Contract logs | Included in contract results and neutral log translation | Retained | Existing logs unchanged | Expected to remain ingestible |
| Contract actions | Fixture-augmented full node produces ordered PBJ actions | Normal full runtime uses `NoTracer`; fixture tooling preserves live output | Existing action sidecars remain readable | Reproduction retains 3 actions through tooling |
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

The transient reproducibility run produced three contract actions, two state-change groups, two
bytecode records, and seven sidecar records. The immutable authenticated corpus remains unchanged.
