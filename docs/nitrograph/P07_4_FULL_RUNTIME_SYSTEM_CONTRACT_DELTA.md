# P07-4 full-runtime system-contract delta

## Boundary

P07-4 removes the EVM/Solidity-facing façades at addresses `0x167` through `0x16c`. It does not
remove ordinary EVM bytecode execution, generic Besu cryptographic precompiles, the `0x16d` account
hook execution seam, or the native account, token, schedule, exchange-rate, and randomness
services.

| Output or behavior | Before | After |
|---|---|---|
| Ordinary contract create/call | Executed by the full legacy EVM | Unchanged |
| Contract result record | Produced by ordinary and system-contract execution | Produced by ordinary execution |
| `0x167`/`0x16c` HTS dispatch | Translator registry synthesized native token transactions | No registration; existing missing-target/address semantics apply |
| `0x16a` account dispatch | HAS translators dispatched account operations | No registration; existing missing-target/address semantics apply |
| `0x16b` schedule dispatch | HSS translators dispatched schedule operations | No registration; existing missing-target/address semantics apply |
| `0x168` exchange rate | Live system-contract result | No registered system contract |
| `0x169` PRNG | Live system-contract result | No registered system contract |
| Token/account/schedule native transactions | Available directly | Unchanged |
| Action sidecars | Concrete producer remains fixture-tooling-only | Historical actions remain readable; ordinary fixture tracing remains reproducible |
| State/storage sidecars | Produced independently of system-contract tracing | Unchanged for ordinary execution and historical input |
| Bytecode sidecars | Produced independently during contract creation | Unchanged |

No new failure adapter was introduced. Former system-contract addresses follow the existing
ordinary missing-target/system-account rules, so no EVM-to-native synthetic dispatch remains.

## Tracer seam delta

`CustomMessageCallProcessor` no longer invokes the system-contract execution branch or its
system-contract-specific callback sequence. Generic precompile callbacks and ordinary frame
callbacks remain because they are owned by the EVM engine and authenticated fixture protocol.
Their removal remains deferred to the EVM-engine wave.
