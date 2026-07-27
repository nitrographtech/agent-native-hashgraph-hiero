# P07-11D Lifecycle and Reconnect Handoff

## Boundary

P07-11C deliberately leaves the lifecycle, reconnect, restart, saved-state,
and authenticated historical-fixture owners below behaviorally unchanged.
Their executable syntax is not ordinary retained-native suite setup: it either
produces authenticated historical pre-state, proves fail-closed behavior
across a state transition, or validates historical state after restart.

P07-11D must resolve these sites only with the full fixture lifecycle,
restart, reconnect, protected-map, and mirror gates available. It must not
regenerate or alter immutable Fixture A or Fixture B.

## Deferred ownership

### LifecycleTest

- File:
  `suites/regression/system/LifecycleTest.java`
- Root:
  `assertAllGetInfoResponsesIncludeExternalizedLedgerId`
- Construction:
  one `contractCustomCreate`
- Current behavior:
  successfully creates a contract, then checks externalized ledger ID across
  account, file, topic, contract, token, and schedule info queries.
- Lifecycle invariant:
  ledger ID remains externally consistent through upgrade/restart tests that
  call this shared helper.
- Pre-state and consumers:
  unique entities are created in the live lifecycle network; callers are
  lifecycle upgrade/restart roots.
- Proposed disposition:
  split out contract creation and contract-info assertion while retaining the
  native account/file/topic/token/schedule ledger-ID assertions.
- Risk:
  changing the shared helper can silently weaken multiple lifecycle roots.
- Required validation:
  all lifecycle callers, upgrade/restart, native query coverage, saved-state
  continuity, and distribution lifecycle validation.

### P06aHistoricalStateReconnectTest

- File:
  `suites/reconnect/P06aHistoricalStateReconnectTest.java`
- Root:
  `historicalStateSurvivesNativeReconnect`
- Successful constructions:
  two `contractCreate` and one `contractCall`.
- Rejection helper:
  `rejectedLegacyBodies`, containing create, call, update, delete, and
  Ethereum unsupported-body constructors. It is invoked after activation and
  after reconnect.
- Current behavior:
  creates `SimpleStorage`, writes `424242`, creates `StorageAccessHook`, writes
  hook storage, activates native mode, advances state while a learner is down,
  reconnects the learner, and proves both native continuity and fail-closed
  legacy bodies.
- Lifecycle invariant:
  genuine signed-state transfer preserves authenticated historical contract
  maps while live execution remains retired.
- Required pre-state:
  preactivation executable runtime plus four-node fixture identities and
  reconnect configuration.
- Proposed disposition:
  replace live pre-state generation with the immutable authenticated Fixture B
  boundary; retain minimal rejection and native advancement around reconnect.
- Risk:
  high. An incorrect split can invalidate reconnect authenticity or alter
  protected STORAGE, BYTECODE, EVM_HOOK_STATES, or LAMBDA_STORAGE maps.
- Required validation:
  Fixture B archive/manifest, `STORAGE=424242`, protected inventory hash,
  learner-behind proof, teacher selection, signed-state transfer, validation,
  learner ACTIVE, synchronization, post-reconnect native operations, and
  deterministic rejection.

### AuthenticatedHistoricalFixtureConsumerTest

- File:
  `suites/reconnect/AuthenticatedHistoricalFixtureConsumerTest.java`
- Root:
  `retainedOperationsAndRetiredBodiesRemainValid`
- Helper:
  `rejectedBodies`
- Constructions:
  one `explicitContractCreate` and one direct `HapiContractCall`, both expecting
  `INVALID_TRANSACTION_BODY`.
- Current behavior:
  consumes an authenticated fixture, performs native account/token/topic
  operations, and proves two legacy bodies fail closed.
- Historical invariant:
  fixture consumption never invokes compatibility tooling and native
  continuity coexists with deterministic rejection.
- Proposed disposition:
  retain or consolidate into the eventual minimal rejection operation only
  after reconnect and fixture consumers share one documented owner.
- Risk:
  removing it prematurely loses authenticated-fixture rejection coverage.
- Required validation:
  Fixture A/B activation, native continuity, deterministic rejection, restart,
  reconnect, and protected maps.

### DiverseStateValidation

- File:
  `suites/file/DiverseStateValidation.java`
- Root:
  `validateDiverseState`
- Construction:
  one `contractCallLocal` against the saved `MULTI_CONTRACT`.
- Current behavior:
  reads saved file and contract state and invokes a local contract query to
  validate the historical lucky-number result.
- Historical invariant:
  diverse saved state remains readable after load.
- Required pre-state:
  the corresponding diverse-state snapshot and captured entity numbers,
  bytecode, and ABI.
- Proposed disposition:
  replace executable local simulation with direct historical state or
  parser/translator validation, while retaining file-state validation.
- Risk:
  a naive deletion would weaken saved-state compatibility coverage; retaining
  simulation would preserve an unsupported execution dependency.
- Required validation:
  saved-state load, bytecode/state inspection, historical parser assertions,
  fixture governance, and mirror compatibility.

## Census

| Category | Sites |
| --- | ---: |
| Successful lifecycle/reconnect constructions | 4 |
| Explicit rejection constructions | 7 |
| Historical local query | 1 |
| **Total deferred** | **12** |

These twelve sites were inventoried but not modified by P07-11C. A P07-11D
implementation must begin with exact reverse-consumer and lifecycle-call-graph
analysis and must run lifecycle, restart, reconnect, protected-map, and mirror
validation before merge.
