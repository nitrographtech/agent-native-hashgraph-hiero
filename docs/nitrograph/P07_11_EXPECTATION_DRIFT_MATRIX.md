# P07-11 Expectation Drift Matrix

## Discovery result

The broad `test-clients:test` probe at the P07-10B checkpoint failed first in
`CongestionPricingTest.canUpdateGasThrottleMultipliersDynamically`. The test
expected a successful `ContractCreate` precheck (`OK`); the retired runtime
correctly returned `INVALID_TRANSACTION_BODY`.

Static discovery then found a substantially larger obsolete-expectation
closure:

- 93 suite files directly construct `ContractCreate`, `ContractCall`, or
  `EthereumTransaction` operations;
- 426 `contractCreate(...)` occurrences;
- 305 `contractCall(...)` occurrences;
- 74 `ethereumCall(...)` occurrences;
- 15 suite files use executable local-call or bytecode-query operations;
- 3,182,123 bytes of directly involved suite source.

These counts are source ownership indicators, not assumed test counts. Dynamic,
parameterized, and nested tests can expand one source occurrence into multiple
executions.

## Confirmed expectation drift

| Suite | Method | Current body/path | Current expectation | Retired-runtime result | Retained behavior | Decision |
| --- | --- | --- | --- | --- | --- | --- |
| `CongestionPricingTest` | `canUpdateGasThrottleMultipliersDynamically` | Upload `Multipurpose`, `ContractCreate`, repeated `ContractCall` gas load | `OK`, successful records, approximately 7x contract congestion fee | `INVALID_TRANSACTION_BODY` at create precheck | Contract gas congestion is retired | `DELETE_IN_P07_11`; do not convert to rejection |
| `CongestionPricingTest` | `canUpdateTransferThrottleMultipliersDynamically` | Native `CryptoTransfer` load | successful native transfer and approximately 7x fee | supported | General native congestion pricing | `RETAIN` unchanged |

This separation proves the correct refactoring rule: retain the native
congestion test, delete the contract-gas test, and do not restore execution or
reinterpret the contract test as a rejection test.

## Static drift by ownership area

| Area | Direct suite files | Typical obsolete expectation | Retained coverage requiring separation | Decision |
| --- | ---: | --- | --- | --- |
| `suites/contract` | 32 | successful create/call/Ethereum execution, gas, logs, results, opcodes | historical result/log parsing and a small number of rejection vectors | Delete execution-only suites; split historical consumers |
| `suites/hip1261` | 12 | contract hooks and contract-service fee success | native account/token/schedule fee behavior | Remove hook/contract methods, retain native methods |
| `suites/integration` | 7 | contract congestion, scheduled calls, hook execution | native congestion, schedules, accounts, tokens, Coordination Layer | Method-level split |
| `suites/hip551` | 5 | executable bodies in atomic batches | native atomic batches and native negative cases | Method/class split |
| `suites/fees` | 5 | contract/hook setup for fee comparisons | native fee charging | Replace setup where equivalent; otherwise delete method |
| `suites/hip991` | 4 | contract keys/setup for topic custom fees | native topic custom-fee behavior | Method-level split |
| `suites/crypto` | 4 | contract setup for transfers, aliases, hollow accounts | native accounts, transfers, aliases, ECDSA | Method-level split |
| `suites/queries` | 3 | contract local call and bytecode query execution | retained neutral/historical state inspection | Split query ownership |
| `suites/file` and reconnect | 4 | legacy fixture generation/validation helpers | authenticated fixture consumption and deterministic rejection | Preserve consumers; remove generation/execution paths |
| regression/fuzzing/issues/misc | 8 | random or malformed executable-body success paths | malformed parsing and rejection where intentional | Replace with bounded rejection vectors or delete |
| token/consensus/staking/throttling | 8 | contract-based setup or executable load | native service behavior | Method-level split |

## Core operation decisions

| Operation | Success ownership | Negative/historical ownership | Decision |
| --- | --- | --- | --- |
| `HapiEthereumCall` | Ethereum execution suites and random provider | no historical parser ownership | Delete after replacing intentional rejection vectors |
| `HapiEthereumContractCreate` | Ethereum create suites | no historical parser ownership | Delete with executable signing |
| `Signing` | only the two Ethereum HAPI operations | none; `NativeEcdsaSigning` is separate | Delete atomically with Ethereum operations |
| `HapiContractCreate` | contract suites, DSL, random providers, utilities | deterministic legacy-body rejection | Remove success API after extracting a minimal rejection operation |
| `HapiContractCall` | contract suites, DSL, precompile providers, utilities | reconnect consumer and deterministic rejection | Separate fixture/rejection use, then remove success API |
| `HapiContractCallLocal` | DSL, random query provider, execution queries | no demonstrated historical parser requirement | Delete with executable query closure |
| `HapiGetContractBytecode` | execution assertions | historical bytecode inspection may remain | Split historical inspection before deleting executable query API |

## Stop-gate conclusion

Continuing as one blind deletion in PR #28 would mix more than three megabytes
of suite source across contract, native, historical, fixture, fuzzing, fee,
schedule, query, and reconnect ownership. That violates the bounded-review
discipline established for P07-10A and P07-10B.

The recommended decomposition is:

1. `P07-11A`: execution-only contract/Ethereum suites and random providers;
2. `P07-11B`: contract setup embedded in retained native suites;
3. `P07-11C`: minimal deterministic-rejection and historical-query boundary,
   followed by HAPI operation deletion;
4. `P07-11D`: resources, signing, dependency, JPMS, policy, lifecycle, and
   mirror closure.

No production source change is required to resolve any expectation drift
identified here.
