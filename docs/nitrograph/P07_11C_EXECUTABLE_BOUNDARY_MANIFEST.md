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

## Post-manifest repository-wide correction

The implementation census found that the P07-11B evidence and the original
P07-11C input omitted additional executable owners outside its 48 legacy-verb
rows and two known failing classes. The repository-wide suite scan found:

- 28 legacy wrapper occurrences across twelve additional files;
- 79 DSL `@Contract` or `SpecContract.call()` occurrences across sixteen
  additional files.

This is a taxonomy/scope correction, not a reclassification of any of the 177
P07-11B removals.

The following additional files have zero native, rejection, historical, query,
lifecycle, or external reverse-consumer ownership and are therefore
`REMOVE_OBSOLETE`:

- `hip1195/Hip1195StreamParityTest.java`;
- `hip1195/HookTimingBalanceOrderTest.java`;
- `hip993/NaturalDispatchOrderingTest.java`;
- `hip993/ThrottleOnDispatchTest.java`;
- `throttling/ThrottleCapacityReclamationTest.java`;
- `hip551/contracts/precompile/AtomicBatchScheduleTest.java`;
- `hip551/contracts/precompile/AtomicBatchTokenTest.java`;
- `hip551/contracts/precompile/AtomicBatchAddress16cTest.java`;
- `hip551/contracts/precompile/AtomicBatchTokenAirdropTest.java`;
- `integration/RepeatableHip1215Tests.java`.

`hip1195/lambdaplex/LambdaplexVerbs.java` has no executable caller after the
HIP-1195 suite removals. Its only external consumer is
`lambdaplex/Fraction.java`, which requires only deterministic decimal-to-base
unit conversion. That neutral conversion moves directly into `Fraction`; the
execution-oriented Lambdaplex owner and its otherwise unconsumed domain types
are `REMOVE_OBSOLETE`.

The following additional mixed owners remain to be reconciled before P07-11C
can close:

- contract-key methods in `AtomicBatchConsensusServiceTest`;
- contract-account methods in `UpdateNodeAccountTestEmbedded`;
- contract-account cases in `TokenTransactSpecs`,
  `CryptoGetInfoRegression`, and `AutoAccountCreationSuite`;
- the contract-address method in `CryptoCreateSuite`;
- contract-mediated topic-fee methods in
  `TopicCustomFeeSubmitMessageTest` and
  `AtomicTopicCustomFeeSubmitMessageTest`;
- contract-mediated association methods in
  `UnlimitedAutoAssociationSuite`;
- the child-mint method in `UnifiedConsTimeTest`;
- the contract-based lifecycle liveness operation in `LifecycleTest`;
- fixture/reconnect constructions in `P06aHistoricalStateReconnectTest`,
  `AuthenticatedHistoricalFixtureConsumerTest`, and
  `DiverseStateValidation`.

The explicit rejection operations in
`HistoricalContractExecutionRejection` remain intentional.

These mixed owners cannot be silently folded into the original 68-construction
input. Their retained-native, lifecycle, and fixture callers require an
amended ownership decision. Until that decision is complete, the expected-zero
census above applies only to the original corrected input, not to the whole
repository.

## Validation issue requiring attribution

After the original corrected input was removed:

- targeted retained `RepeatableIntegrationTests` passed four of four;
- both former managed-contract `beforeAll` failures disappeared;
- the full test-client target executed 140 tests successfully and skipped one;
- final `StreamValidationTest` balance reconciliation failed deterministically
  for reward account `0.0.801`, expecting `3207204483389181` tinybar and
  observing `1958552273434530`.

A clean rerun after deleting generated test state reproduced the same mismatch.
This is not an executable-operation expectation, but it appeared only after
the P07-11C removal set allowed the complete target to reach terminal stream
validation. P07-11C cannot claim validation closure until the mismatch is
proven pre-existing/environmental or the relationship between removed fee
generating tests and the validator is resolved without changing production
semantics.
