# P07-10 PR #28 Validation Matrix

## Required for PR #28

| Boundary | Required proof |
|---|---|
| Compilation | `test-clients:compileJava` and `test-clients:compileTestJava` |
| Historical result ownership | Fixture values and `ContractCallTranslator` consumers compile without suite-owned data |
| Historical sidecar ownership | `SidecarVerbs` and historical sidecar consumers compile without retired suite helpers |
| Historical storage ownership | HIP-1195 storage vectors preserve byte order, width, padding, and `424242` decoding |
| Native account vectors | Account, system-account, alias, and transfer consumers compile without `Evm46ValidationSuite` |
| Native signing | Account authorization, ECDSA, topic-fee, allowance, airdrop, and integration consumers use `NativeEcdsaSigning` |
| Mixed-suite cleanup | Modified airdrop, allowance, batch, and integration suites contain no removed CREATE2 or HTS-precompile helper dependency |
| Source ownership | Shared-helper and mixed-suite policies plus their intentional-failure controls pass |
| Repository policy | Existing P06/P07, dependency, fixture, compatibility, binary, crypto-boundary, and secret policies pass |
| CI | Exact-head native-agent CI passes |
| Historical lifecycle | Immutable Fixture A activation and Fixture B startup/save/restart preserve their authenticated boundaries |
| Distributed lifecycle | Fixture B real reconnect and synchronization preserve protected maps |
| Mirror | Pinned importer accepts Fixture A, Fixture B, and non-executable exact-head runtime corpora |

Focused validation must also prove:

- no retained consumer imports `RecordsSuite`, `TraceabilitySuite`, `EncodingUtils`,
  `Evm46ValidationSuite`, `Create2OperationSuite`, `ContractCreateSuite`, or
  `HTSPrecompileResult`;
- translators and matchers do not import suite classes for shared values;
- retained native suites do not invoke removed CREATE2 or HTS-precompile helpers;
- production and neutral contract API source do not import the new test-helper owners.

## Known remaining P07-11 expectation drift

| Ownership | Example | Classification | Follow-up |
|---|---|---|---|
| Contract gas congestion | `CongestionPricingTest.canUpdateGasThrottleMultipliersDynamically` | Successful contract execution expectation; unchanged by PR #28 | P07-11A |
| Contract creation | Remaining suites expecting successful `ContractCreate` | Execution-only or mixed setup | P07-11A/P07-11B |
| Contract calls | Remaining suites expecting successful `ContractCall` | Execution-only or mixed setup | P07-11A/P07-11B |
| Ethereum execution | `HapiEthereumCall` and `HapiEthereumContractCreate` consumers | Executable HAPI infrastructure | P07-11A/P07-11C |
| Local execution queries | Contract local-call and executable bytecode/query suites | Retired execution/query expectation | P07-11A/P07-11C |
| Contract-based native setup | Native suites still using a contract only as setup | Mixed ownership | P07-11B |

The complete inventory is in `P07_11_EXECUTABLE_HAPI_OWNERSHIP.md` and
`P07_11_EXPECTATION_DRIFT_MATRIX.md`. These failures are not suppressed and are not represented as
P07-10 regressions. A rejection test is retained only when rejection is the behavior under test.

## Unrelated

Tests outside the P07-10 changed dependency and ownership closure are not used to redefine PR #28
scope. A broad `test-clients:test` probe remains useful discovery input, but successful-execution
failures already assigned to P07-11 do not block this bounded merge. Any native, historical,
fixture, policy, or compilation regression inside the changed closure does block it.

## Frozen dependency census

| Owner | Besu importers | Tuweni importers | Headlong importers | Direct importer bytes |
|---|---:|---:|---:|---:|
| test-clients | 7 | 13 | 100 | 112,834 / 204,930 / 1,604,235 respectively |
| fixture-tooling | 0 | 0 | 0 | 0 |
| platform-sdk/base-crypto | 1 | 0 | not part of P07-10 | protected |

Unique test-client files importing at least one of Besu, Tuweni, or Headlong: 109.

Remaining ownership is classified as:

- `P07_11A_EXECUTION_ONLY_SUITE`;
- `P07_11B_CONTRACT_BASED_NATIVE_SETUP`;
- `P07_11C_REJECTION_OR_HISTORICAL_BOUNDARY`;
- `P07_11D_RESOURCE_OR_DEPENDENCY_CLEANUP`;
- `PROTECTED_BASE_CRYPTO`.
