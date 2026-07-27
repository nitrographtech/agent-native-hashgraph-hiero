# P07-11C Executable Boundary Manifest

## Authority and scope

This manifest is the implementation authority for P07-11C. It begins at
`50f0b7c025dd30c129627f7559b7e09e318cac0c`, after P07-11B closed.
P07-11B's 177 removed constructions and ownership decisions are immutable.

The P07-11B evidence reported 48 remaining legacy-verb constructions. Source
inspection found an additional 20 DSL-managed constructions in the two
retained failing setup roots. The correction does not reopen P07-11B:
`@Contract`-managed creates and `SpecContract.call()` operations were omitted
from its legacy-verb taxonomy.

No row is `UNRESOLVED`.

## Disposition rules

- `REMOVE_OBSOLETE`: Remove the registered root and its execution-only closure.
- `REPLACE_WITH_NATIVE_SETUP`: Replace setup while preserving a native test.
- `REWRITE_NATIVE_ASSERTION`: Preserve native behavior with a native assertion.
- `PRESERVE_REJECTION_WITHOUT_CONTRACT`: Keep a minimal rejection vector with
  no successful executable setup.
- `PRESERVE_HISTORICAL_WITH_FIXTURE`: Drive interpretation from immutable
  historical input.
- `PRESERVE_LOCAL_QUERY`: Keep an architecturally supported local query.
- `SPLIT_REQUIRED`: Remove the executable descendant and retain an independent
  native descendant.

## Corrected construction census

| Family | P07-11B evidence | DSL correction | P07-11C input |
| --- | ---: | ---: | ---: |
| Contract create | 32 | 13 | 45 |
| Contract call | 12 | 7 | 19 |
| Ethereum | 0 | 0 | 0 |
| Executable local query | 4 | 0 | 4 |
| **Total** | **48** | **20** | **68** |

The DSL correction is:

- `RepeatableIntegrationTests`: one managed create and three dynamic
  `SpecContract.call()` constructions;
- `Hip1195EnabledTest`: twelve managed creates and four direct
  `contractCallWithFunctionAbi()` constructions.

Native transactions that trigger an installed EVM hook are execution-dependent
behavior but are not double-counted as additional construction sites.

## Initial 48-construction manifest

### ContractUpdateSuite — 29 constructions

Source:
`suites/contract/hapi/ContractUpdateSuite.java`

Ownership root: nineteen registered HAPI tests. Concrete subtypes are twenty
ordinary `contractCreate`, one `contractCustomCreate`, seven ordinary
`contractCall`, and one `contractCallLocal`.

| Registered root | Purpose and assertions | Disposition | Implementation consequence |
| --- | --- | --- | --- |
| `updateMaxAutomaticAssociationsAndRequireKey` | Successful executable contract update | `REMOVE_OBSOLETE` | Delete root |
| `idVariantsTreatedAsExpected` | Successful contract update ID variants | `REMOVE_OBSOLETE` | Delete root |
| `updateStakingFieldsWorks` | Successful contract staking update | `REMOVE_OBSOLETE` | Delete root |
| `eip1014AddressAlwaysHasPriority` | Contract/EVM address update behavior | `REMOVE_OBSOLETE` | Delete root |
| `updateWithBothMemoSettersWorks` | Successful contract memo update | `REMOVE_OBSOLETE` | Delete root |
| `updatingExpiryWorks` | Successful contract expiry update | `REMOVE_OBSOLETE` | Delete root |
| `rejectsExpiryTooFarInTheFuture` | Contract-update validation after successful create | `REMOVE_OBSOLETE` | Existing deterministic rejection suite covers retired bodies without setup |
| `updateAutoRenewWorks` | Successful contract auto-renew update | `REMOVE_OBSOLETE` | Delete root |
| `updateAutoRenewAccountWorks` | Successful contract auto-renew-account update | `REMOVE_OBSOLETE` | Delete root |
| `updateAdminKeyWorks` | Successful contract admin-key update | `REMOVE_OBSOLETE` | Delete root |
| `immutableContractKeyFormIsStandard` | Successful immutable contract creation/query | `REMOVE_OBSOLETE` | Delete root |
| `canMakeContractImmutableWithEmptyKeyList` | Successful executable update | `REMOVE_OBSOLETE` | Delete root |
| `givenAdminKeyMustBeValid` | Contract admin-key validation after create | `REMOVE_OBSOLETE` | Dedicated retired-body rejection already exists |
| `fridayThe13thSpec` | Dynamic contract update/call regression reproduction | `REMOVE_OBSOLETE` | Delete dynamic closure |
| `updateDoesNotChangeBytecode` | Live bytecode persistence after update | `REMOVE_OBSOLETE` | Historical bytecode maps remain fixture-validated elsewhere |
| `tryContractUpdateWithMaxAutoAssociations` | Helper-generated successful update | `REMOVE_OBSOLETE` | Delete helper with callers |
| `playGame` | Contract create/call/local-query scenario | `REMOVE_OBSOLETE` | Delete dynamic helper closure |
| `cannotUpdateImmutableContractExceptExpiry` | Live immutable-contract update behavior | `REMOVE_OBSOLETE` | Delete root |
| `cannotUpdateContractExceptExpiryWithWrongKey` | Live wrong-key contract update behavior | `REMOVE_OBSOLETE` | Delete root |

Caller/consumer evidence: the class has no external reverse consumer and is
discovered only through its own registered tests. `LegacyContractAdminVectors`
already owns the shared immutable admin-key vector. Deterministic retired-body
rejection is independently covered by `HistoricalContractExecutionRejection`
and `P06aHistoricalStateReconnectTest`.

Final architecture: this class contains no historical record interpretation;
all assertions depend on live successful execution. Delete the file after the
reverse-consumer and discovery checks remain empty.

### ContractRecordsSanityCheckSuite — 8 constructions

Source:
`suites/contract/records/ContractRecordsSanityCheckSuite.java`

| Registered root | Constructions | Purpose and assertions | Disposition |
| --- | ---: | --- | --- |
| `contractDeleteRecordSanityChecks` | create 1 | Generate live delete transfer/fee record | `REMOVE_OBSOLETE` |
| `contractCreateRecordSanityChecks` | create 1 | Generate live create transfer/fee record | `REMOVE_OBSOLETE` |
| `contractCallWithSendRecordSanityChecks` | create 1, call 1 | Generate live payable-call record | `REMOVE_OBSOLETE` |
| `circularTransfersRecordSanityChecks` | create 1, calls 2 | Generate live nested transfer records | `REMOVE_OBSOLETE` |
| `contractUpdateRecordSanityChecks` | create 1 | Generate live update record | `REMOVE_OBSOLETE` |

Concrete subtypes: four ordinary `contractCreate`, one
`createDefaultContract`, one `contractCallWithFunctionAbi`, one
`contractCallWithTuple`, and one ordinary `contractCall`.

Caller/consumer evidence: the suite has no external reverse consumer. Its
assertions do not parse immutable Fixture A/B input; they generate fresh
records from successful execution. Historical result, sidecar, log, action,
storage, and mirror interpretation is already owned by extracted historical
fixtures and fixture lifecycle tests.

Final architecture: delete the execution-only file. No historical expected
value is moved or changed.

### Explicit query and rejection boundaries — 11 constructions

| Source and registered root | Constructions | Intent | Disposition | Consequence |
| --- | ---: | --- | --- | --- |
| `file/FileUpdateSuite.gasLimitOverMaxGasLimitFailsPrecheck` | create 1, local query 1 | Contract-local gas-limit precheck | `REMOVE_OBSOLETE` | Delete method; this is contract-query validation, not file behavior |
| `queries/AsNodeOperatorQueriesTestEmbedded.getSmartContractQuerySigNotRequired` | create 1 | Contract-info query authorization | `REMOVE_OBSOLETE` | Delete method; adjacent native query tests retain node-operator signature behavior |
| `queries/AsNodeOperatorQueriesTestEmbedded.getContractBytecodeQueryNoSigRequired` | create 1 | Contract-bytecode query authorization | `REMOVE_OBSOLETE` | Delete method |
| `queries/DisabledNodeOperatorTest.nodeOperatorQueryPortNotAccessibleForContractCall` | create 1, call 1, local queries 2 | Disabled node-operator contract-call-local port | `REMOVE_OBSOLETE` | Delete method; native file/schedule query port tests retain the transport invariant |
| `queries/DisabledNodeOperatorTest.nodeOperatorQueryPortNotAccessibleForContractBytecode` | create 1, call 1 | Disabled node-operator bytecode-query port | `REMOVE_OBSOLETE` | Delete method |
| `queries/RepeatableOperatorQueryTests.getSmartContractQueryNodeOperatorNotCharged` | create 1 | Contract-info query charging | `REMOVE_OBSOLETE` | Delete method; native file/schedule query charging tests retain the invariant |

None is `PRESERVE_LOCAL_QUERY`: executable contract-local simulation and
contract bytecode/info queries are not backed by retained live contract
services. None is converted to `INVALID_TRANSACTION_BODY`; the minimal
retired-transaction rejection invariant is already covered without successful
setup.

## Failing managed setup roots and corrected constructions

### RepeatableIntegrationTests — 4 constructions

Source: `suites/integration/RepeatableIntegrationTests.java`

Ownership graph:

```text
@Contract HRC755Contract
→ beforeAll
→ SIGNING_CONTRACT.getInfo()
→ managed ContractCreate
├─ controllingContractCanTriggerExecutionViaSystemContract
│  └─ SIGNING_CONTRACT.call()
├─ contractCanTriggerExecutionForItsOwnAssets
│  └─ SIGNING_CONTRACT.call()
└─ contractSignatureIsNoopIfNotControlling
   └─ SIGNING_CONTRACT.call()
```

| Root | Intent | Disposition | Consequence |
| --- | --- | --- | --- |
| `beforeAll` and `SIGNING_CONTRACT` | Reusable successful contract deployment | `SPLIT_REQUIRED` | Remove after its three consumers are removed |
| `controllingContractCanTriggerExecutionViaSystemContract` | Contract-originated schedule signature | `REMOVE_OBSOLETE` | Delete root |
| `contractCanTriggerExecutionForItsOwnAssets` | Contract-originated schedule signature | `REMOVE_OBSOLETE` | Delete root |
| `contractSignatureIsNoopIfNotControlling` | Contract-signature negative case | `REMOVE_OBSOLETE` | Delete root |

The retained roots `burnAtStakePeriodBoundaryHasExpectedRecord`,
`senderSignatureValidatedInQueries`,
`gasThrottleMimicsThroughputThrottleCongestionStatus`, and
`classifiableTakesPriorityOverUnclassifiable` do not consume
`SIGNING_CONTRACT`. They remain unchanged. This resolves the
`beforeAll` failure by deleting obsolete execution ownership, not by changing
its expected status.

### Hip1195EnabledTest — 16 constructions

Source: `suites/integration/hip1195/Hip1195EnabledTest.java`

The `beforeAll` root provisions twelve `@Contract` hook implementations via
`getInfo()`. All registered roots test live EVM hook execution, hook storage,
hook-created contracts, EVM call variants, EVM gas/result/log/action/sidecar
behavior, or native transfers mediated by those installed executable hooks.
Four roots also directly construct `contractCallWithFunctionAbi`.

| Closure | Construction | Intent | Disposition |
| --- | --- | --- | --- |
| Twelve `@Contract` fields and `beforeAll` | managed create 12 | Install live EVM hooks | `REMOVE_OBSOLETE` |
| `createOpHook_createsChildOwnedByHookOwner_and_onlyOwnerChecks` | ABI call 2 | Validate hook-created contract | `REMOVE_OBSOLETE` |
| `create2OpHook_createsChildOwnedByHookOwner_and_onlyOwnerChecks` | ABI call 2 | Validate CREATE2 hook-created contract | `REMOVE_OBSOLETE` |
| All remaining registered roots | hook-mediated native operations | Live executable hook behavior | `REMOVE_OBSOLETE` |

HIP-1195 EVM hooks are absent from the target runtime. Native token transfer,
allowance, association, custom-fee, validation, and authorization coverage
exists in direct Native Asset Service suites. The class has no external reverse
consumer or historical fixture interpretation. Delete the complete file after
the final reverse-consumer check.

## Historical and rejection architecture

- Historical result, log, action, sidecar, storage, bytecode, and mirror
  interpretation stays in the extracted historical fixture owners and
  authenticated Fixture A/B consumers. P07-11C does not synthesize live
  execution to regenerate historical evidence.
- `HistoricalContractExecutionRejection` remains the minimal explicit
  rejection owner for ContractCreate, ContractCall, ContractUpdate,
  ContractDelete, and EthereumTransaction bodies, followed by a native
  continuity operation.
- `P06aHistoricalStateReconnectTest` retains its fixture-bound rejection proof.
- Core HAPI operation implementations are unchanged in P07-11C.

## Implementation set

Delete:

- `ContractUpdateSuite.java`;
- `ContractRecordsSanityCheckSuite.java`;
- `Hip1195EnabledTest.java`;
- the three contract-signature roots plus `SIGNING_CONTRACT` and `beforeAll`
  from `RepeatableIntegrationTests`;
- the six explicit query/rejection methods listed above.

Remove only imports, constants, helpers, and registrations made dead by those
authorized deletions. Preserve all other methods in mixed files.

## Expected post-implementation census

Within the corrected 68-construction P07-11C input:

| Family | Before | Remove | Remain |
| --- | ---: | ---: | ---: |
| Contract create | 45 | 45 | 0 |
| Contract call | 19 | 19 | 0 |
| Ethereum | 0 | 0 | 0 |
| Executable local query | 4 | 4 | 0 |
| **Total** | **68** | **68** | **0** |

Repository-wide explicit rejection constructors outside this input remain
intentional and must be reported separately; they are not successful
execution dependencies.

## Repository-wide supplemental census

The implementation census found 43 initially unmanifested syntactic sites.
The complete wrapper sweep found one additional `contractUpdate` in
`AutoAccountCreationSuite`, correcting the supplemental total to **44**. This
is a census correction, not a reclassification of P07-11B's 177 removals.

### P07-11C non-lifecycle sites — 32

| Source | Registered root | Wrappers and count | Intent and native ownership | Disposition and consequence |
| --- | --- | ---: | --- | --- |
| `consensus/AtomicBatchConsensusServiceTest` | Eight `topicCreateWith*Contract*` roots | `createDefaultContract` 8 | Contract accounts as topic auto-renew accounts; adjacent account-based batch roots retain native topic authorization and failure behavior | `REMOVE_OBSOLETE`; delete the eight roots |
| `hip1299/UpdateNodeAccountTestEmbedded` | `updateNodeAccountIdSuccessfullyWithContractWithAdminKey`, `updateNodeAccountIdSuccessfullyWithContractWithoutAdminKey`, `updateNodeAccountIdWithContractWithAdminKeyWithZeroBalanceFails` | `createDefaultContract` 3 | Contract accounts as node accounts; account-key and zero-balance node-update roots remain | `REMOVE_OBSOLETE`; delete the three roots |
| `token/TokenTransactSpecs` | `cannotGiveNftsToDissociatedContractsOrAccounts`, `cannotSendFungibleToDissociatedContractsOrAccounts` | `createDefaultContract` 2 | Mixed contract/account dissociation assertions | `SPLIT_REQUIRED`; remove contract branches and retain direct account dissociation assertions |
| `crypto/CryptoGetInfoRegression` | `cryptoGetContractBalanceQueryAssociationThrottles` | `createDefaultContract` 1 | Contract-balance throttle behavior only | `REMOVE_OBSOLETE`; delete root |
| `crypto/AutoAccountCreationSuite` | `noStakePeriodStartIfNotStakingToNode` | `createDefaultContract` 1, `contractUpdate` 1 | Contract/account mutual staking behavior; no independent native assertion survives | `REMOVE_OBSOLETE`; delete root |
| `crypto/CryptoCreateSuite` | `canonicalEvmAddressesDeterminedByAliases` | DSL `@Contract` 1, `SpecContract.call()` 4 | Contract-originated canonical-address calls | `REMOVE_OBSOLETE`; native alias creation tests remain |
| `hip991/TopicCustomFeeSubmitMessageTest` | `messageSubmitToPublicTopicWithFee1token` | DSL `@Contract` 1, `SpecContract.call()` 1 | Mixed native topic custom fee plus unrelated contract transfer record | `SPLIT_REQUIRED`; retain native fee collection, remove contract branch |
| `hip991/AtomicTopicCustomFeeSubmitMessageTest` | `messageSubmitToPublicTopicWithFee1token` | DSL `@Contract` 1, `SpecContract.call()` 1 | Mixed native atomic topic creation/custom fee plus unrelated contract transfer record | `SPLIT_REQUIRED`; retain native fee collection, remove contract branch |
| `hip904/UnlimitedAutoAssociationSuite` | `autoAssociationThroughSystemContractChangesGasCost`, `autoAssociationThroughSystemContractDoesNotChargeDispatchPayer` | DSL `@Contract` 2, `SpecContract.call()` 3 | HTS system-contract gas and dispatch-payer behavior | `REMOVE_OBSOLETE`; direct native auto-association roots remain |
| `hip993/UnifiedConsTimeTest` | `childMintTimeIsParentConsensusTime` | DSL `@Contract` 1, `SpecContract.call()` 1 | Precompile child-mint timing | `REMOVE_OBSOLETE`; retained native consensus-time roots remain |

All sites are direct registered-root constructions except DSL parameter
injection, which dynamically generates the managed create before the root.
No non-lifecycle row is unresolved.

### P07-11D lifecycle, reconnect, and authenticated fixture sites — 12

| Source | Root or helper | Wrappers and count | Invariant | Disposition |
| --- | --- | ---: | --- | --- |
| `regression/system/LifecycleTest` | `assertAllGetInfoResponsesIncludeExternalizedLedgerId` | `contractCustomCreate` 1 | Query coverage across restart/upgrade lifecycle | `DEFER_P07_11D` |
| `reconnect/P06aHistoricalStateReconnectTest` | `historicalStateSurvivesNativeReconnect` | successful `contractCreate` 2, successful `contractCall` 1 | Produces authenticated historical STORAGE/BYTECODE/hook state before activation and reconnect | `DEFER_P07_11D` |
| same | `rejectedLegacyBodies` | `explicitContractCreate`, `contractCall`, `contractUpdate`, `contractDelete`, `explicitEthereumTransaction` 5 | Fail-closed proof after activation and reconnect | `DEFER_P07_11D` |
| `reconnect/AuthenticatedHistoricalFixtureConsumerTest` | `rejectedBodies` | `explicitContractCreate`, direct `HapiContractCall` 2 | Authenticated fixture rejection and native continuity | `DEFER_P07_11D` |
| `file/DiverseStateValidation` | `validateDiverseState` | `contractCallLocal` 1 | Read-only validation of saved historical contract state | `DEFER_P07_11D` |

The dedicated handoff records pre-state, consumers, risk, and required
validation. None of these files is behaviorally modified by P07-11C.

### Existing intentional rejection boundary

`HistoricalContractExecutionRejection.rejectsEveryExecutableBodyAndContinuesNatively`
contains five minimal unsupported-body constructors: create, call, update,
delete, and Ethereum. They are explicit rejection coverage, have no successful
setup, and remain `RETAIN_INTENTIONAL_REJECTION`.

## Corrected post-implementation boundary

- Original corrected P07-11C input removed: 68 constructions.
- Supplemental non-lifecycle sites removed or split: 32.
- Supplemental lifecycle/reconnect sites deferred: 12.
- Existing minimal rejection constructors retained: 5.
- Unresolved sites: 0.

The remaining suite-level executable syntax is therefore exactly seventeen
intentional sites: twelve P07-11D lifecycle/reconnect/fixture sites and five
minimal rejection constructors. Core DSL and HAPI operation implementations
are constructors, not registered executable ownership, and remain outside this
suite census.

## StreamValidationTest attribution

Three isolated worktrees used the same cleanup and
`:test-clients:test` command:

| Checkpoint | Tests | Expected `0.0.801` | Observed `0.0.801` | First/only failure |
| --- | ---: | ---: | ---: | --- |
| `50f0b7c025dd30c129627f7559b7e09e318cac0c` | 142 (1 skipped) | 3207432793891461 | 1958552273434530 | `StreamValidationTest.streamsAreValid` |
| `1b8c60fa52de3a763d1023ab0346813b436c9731` | 142 (1 skipped) | 3207432793891461 | 1958552273434530 | same |
| `b024af0048446d6d9e6401787c4f7c58cb782725` | 142 (1 skipped) | 3207204483389181 | 1958552273434530 | same |

`0.0.801` is the node reward account. `BalanceReconciliationValidator`
derives its expectation by summing transfer lists from the record stream; it
does not use a static suite-population ledger. It compares that derived total
with a live balance and has a narrow staking-boundary reconciliation that only
applies when the complete delta shape matches known sources and recipients.
The unchanged observed value and baseline failure prove the mismatch predates
P07-11C. The lower post-cleanup expectation shows removed transactions cease
contributing to the record-derived model as intended; there is no stale
P07-11C registration or expected-fee contribution to repair. P07-11C therefore
does not alter or weaken terminal balance validation.
