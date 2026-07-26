# P07-11A Execution-Only Suite Ownership

## Baseline and architecture gate

Baseline: merged P07-10 commit `3002aa45c7d3d6622746fa6ac809b9f1a26d9593`.

The P07-11 census identified 93 suite files with direct executable verb ownership. This gate
rechecked every row for native behavior, historical interpretation, deterministic rejection,
shared constants, reverse consumers, annotation-driven contract lifecycle, and suite-local helper
ownership.

**Gate result: bounded deletion is possible.**

- 30 direct census suites are entirely execution-only.
- Four additional annotation-driven suites are entirely execution-only.
- The 57 non-suite files supporting `FailureCharacterizationSuite` form a closed,
  zero-external-consumer classic-call helper closure.
- Two apparent execution-only suites still own shared constants and are deferred.
- No core HAPI operation, historical parser, deterministic-rejection operation, mixed native
  suite, production source, platform-sdk source, or base-crypto source is in the deletion set.

## Exact deletion set

### Direct executable suites

1. `suites/contract/classiccalls/FailureCharacterizationSuite.java`
2. `suites/contract/ethereum/HelloWorldEthereumSuite.java`
3. `suites/contract/ethereum/JumboTransactionsEnabledTest.java`
4. `suites/contract/ethereum/NonceSuite.java`
5. `suites/contract/ethereum/batch/AtomicHelloWorldEthereumSuite.java`
6. `suites/contract/evm/Evm50ValidationSuite.java`
7. `suites/contract/evm/batch/AtomicEvm50ValidationSuite.java`
8. `suites/contract/fees/AtomicSmartContractServiceFeesTest.java`
9. `suites/contract/fees/SimpleSmartContractServiceFeesTest.java`
10. `suites/contract/fees/SmartContractServiceFeesTest.java`
11. `suites/contract/hapi/ContractCallHapiOnlySuite.java`
12. `suites/contract/hapi/ContractStateSuite.java`
13. `suites/contract/hapi/batch/AtomicContractUpdateSuite.java`
14. `suites/contract/hip906/HbarAllowanceApprovalTest.java`
15. `suites/contract/hips/batch/AtomicIsAuthorizedTest.java`
16. `suites/contract/hips/hip632/IsAuthorizedTest.java`
17. `suites/contract/leaky/LeakyEthereumTestsSuite.java`
18. `suites/contract/leaky/batch/AtomicLeakyEthereumTestsSuite.java`
19. `suites/contract/opcodes/CreateOperationSuite.java`
20. `suites/contract/opcodes/DelegateCallOperationSuite.java`
21. `suites/contract/opcodes/GlobalPropertiesSuite.java`
22. `suites/contract/opcodes/PrngSeedOperationSuite.java`
23. `suites/contract/opcodes/PushZeroOperationSuite.java`
24. `suites/contract/openzeppelin/ERC1155ContractInteractions.java`
25. `suites/contract/openzeppelin/ERC721ContractInteractions.java`
26. `suites/contract/opsduration/OpsDurationThrottleTest.java`
27. `suites/contract/records/LogsSuite.java`
28. `suites/contract/records/batch/AtomicLogsSuite.java`
29. `suites/contract/validation/EvmValidationTest.java`
30. `suites/contract/validation/batch/AtomicEvmValidationTest.java`

These suites contain 143 literal `contractCreate`, 145 literal `contractCall`, 66 literal
`ethereumCall`, 24 literal Ethereum-create, and 11 literal local-call occurrences. They expose 238
direct HAPI test registrations and total 533,479 source bytes.

### Annotation-driven executable suites

1. `suites/contract/fees/ContractServiceQueriesSimpleFeesTest.java`
2. `suites/contract/hips/batch/AtomicAliasTest.java`
3. `suites/contract/hips/hip632/AliasTest.java`
4. `suites/contract/opcodes/GasPriceSuite.java`

These four suites contain 36 HAPI test registrations and 34,129 source bytes. Their contract
creation/call ownership is supplied by `@Contract`/DSL lifecycle rather than literal transaction
verbs. Their query, alias-precompile, and gas-result expectations require retired execution.

### Classic-call helper closure

The entire `suites/contract/classiccalls` package has no consumer outside that package.
`FailureCharacterizationSuite` is counted above; the other 57 files (116,394 bytes) are:

- the abstract/result/inventory types in the package root;
- every class under `mutations`;
- every class under `views`.

They construct and classify live system-contract/classic-call mutations and views for the deleted
suite. They have no native, historical, mirror, fixture, or deterministic-rejection ownership.

### Total

- Files: 91
- Source bytes: 684,002
- Direct/annotation-driven suite classes: 34
- Suite-local helper classes: 57
- HAPI test registrations: 274

No resource is deleted in P07-11A. Solidity, ABI, and binary cleanup remains P07-11D so resource
reverse ownership can be measured after P07-11B/C.

## Classification of all 93 direct census suites

| Classification | Count | Ownership |
|---|---:|---|
| `EXECUTION_ONLY_DELETE_IN_11A` | 30 | The direct suite list above |
| `MIXED_NATIVE_EXECUTION_DEFER_TO_11B` | 61 | The 59 census rows classified `CONTRACT_BASED_TEST_SETUP`, plus the two shared-owner suites below |
| `REJECTION_OR_HISTORICAL_DEFER_TO_11C` | 2 | `HistoricalContractExecutionRejection`, `P06aHistoricalStateReconnectTest` |
| `RESOURCE_OR_DEPENDENCY_DEFER_TO_11D` | 0 suite rows | All remaining resource/dependency ownership is tracked separately |
| `RETAIN_NATIVE` | 0 direct executable rows | Native-only suites have no direct executable-verb row |
| `UNKNOWN_REQUIRES_REVIEW` | 0 | Every census row is resolved |

The 59 original mixed rows remain exactly those marked `CONTRACT_BASED_TEST_SETUP` in
`P07_11_EXECUTABLE_HAPI_OWNERSHIP.md`.

### Shared-owner deferrals

- `ContractUpdateSuite` owns `ADMIN_KEY`/`NEW_ADMIN_KEY`, consumed by retained or mixed account,
  airdrop, crypto-update, hollow-account, and atomic-signature suites.
- `ContractRecordsSanityCheckSuite` owns `PAYABLE_CONTRACT`, consumed by retained/mixed staking and
  throttling suites.

Deleting either now would require extracting mixed-suite setup ownership and would cross into
P07-11B. `AtomicContractUpdateSuite` has no such dependency and remains in P07-11A.

## Provider, registration, matcher, and resource findings

- HAPI suites are annotation/classpath discovered; there is no explicit provider, service entry,
  or test-plan registration exclusively naming the deletion set.
- Reverse references among paired execution-only suites disappear atomically.
- No reverse reference from outside the deletion set remains, except the two explicitly deferred
  shared-owner suites.
- `ContractFnResultAsserts` and `ContractLogAsserts` remain shared with historical consumers and
  are not deleted.
- `GasMatcher` remains until its complete execution-expectation closure reaches zero consumers.
- `HapiContractCreate`, `HapiContractCall`, `HapiEthereumCall`,
  `HapiEthereumContractCreate`, query operations, and `Signing` remain for P07-11C.
- Solidity, ABI, bytecode, Gradle, JPMS, and dependency edges remain for P07-11D.
- No historical assertion or rejection vector must be extracted for the bounded 91-file set.

## Implementation plan

1. Delete the exact 91-file source closure.
2. Compile main and test-client test source.
3. Search for dangling class, package, import, discovery, and resource references.
4. Add a portable policy rejecting the deleted suites/package while allowing P07-11B/C/D
   ownership.
5. Record exact deletion and dependency deltas.
6. Run existing P06/P07 policies, application/distribution validation, immutable fixture,
   reconnect, and mirror certification before merge.

P07-11A does not broaden into mixed native-suite edits, core HAPI operation deletion, resource
cleanup, signing cleanup, or protected cryptography.

## Implemented boundary

The exact 91-file closure above was deleted without adjustment. Post-deletion compilation proves
that no source consumer or suite-discovery edge remains. The two shared-owner deferrals remain
present, and no production, mixed native, historical, rejection, fixture, resource, Gradle, JPMS,
platform-sdk, or base-crypto source was changed.

The post-deletion literal operation inventory in test-client main source is:

- `contractCreate`: 274;
- `contractCall`: 172;
- `ethereumCall`: 9.

These remaining operations belong to P07-11B/C. P07-11A does not reinterpret them as part of its
bounded deletion.
