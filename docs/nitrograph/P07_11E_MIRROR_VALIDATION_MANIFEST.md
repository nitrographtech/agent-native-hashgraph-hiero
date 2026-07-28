# P07-11E Mirror Validation Manifest

## Authority and boundary

This certification begins at
`9d72fe0dd79d4d3b5c5adada826509a2992f8c03`. P07-11B, P07-11C, and
P07-11D ownership decisions are closed. The official mirror importer is pinned
at `hiero-ledger/hiero-mirror-node@834a7a1cb9204b02c192098c60654d08eb85bc2a`.

The production artifact, stream schemas, PBJ/protobuf models, state keys,
serializers, immutable fixtures, and reconnect harness are unchanged by
P07-11B through P07-11D. Test-client cleanup changes which tests manufacture
records, not how the node produces or the mirror importer consumes them.

## Mirror-facing surfaces

| Surface | Producer | Serializer or translator | Consumer or validator | Retained invariant and representative coverage | Expected mirror-visible result | Validation |
| --- | --- | --- | --- | --- | --- | --- |
| Transaction bodies, hashes, timestamps, status, and identifiers | Retained HAPI account, token, topic, schedule, file, node, staking, and batch suites | Record stream and block stream writers; `RecordFileReader`; `BlockStreamReader` | Pinned importer `RecordFileParser`; transaction/body validators | Deterministic body/hash association, consensus order, entity IDs, and native status | Native transactions persist with their original body, result, hash, timestamp, and entity relationship | Full HAPI stream validation; official importer corpora |
| Account creation, updates, aliases, hollow completion, and transfers | Crypto/alias retained suites | `CryptoCreateTranslator`, `CryptoUpdateTranslator`, `CryptoTransferTranslator` | Mirror transaction/entity/account/transfer tables; balance reconciliation | Native aliases and account effects remain visible without contract-created aliases | Native entity and transfer rows only; no contract-created account side effect | Full test corpus and block/record parity |
| Token create, mint, burn, wipe, transfer, association, fees, NFT, airdrop, and allowance | Native Asset Service retained suites | Token translators, transfer lists, assessed-fee and association state changes | Mirror token, transfer, NFT, assessed-fee, and relationship persistence | Direct native token bodies preserve treasury, transfer, fee, association, and NFT semantics | Correct native token entities, balances, associations, NFT transfers, and fees | Full test corpus; importer Fixture A/B associations |
| Topic creation, submission, and custom fees | Topic and Coordination retained suites | `TopicCreateTranslator`, `SubmitMessageTranslator`, record/block serializers | Mirror topic message and assessed-fee consumers | Sequence, consensus timestamp, payer/collector, and native signature behavior | Ordered topic messages and native fee effects | Full test corpus; Fixture A/B importer |
| Schedule create, sign, delete, and native child execution | Retained schedule suites | Schedule translators and child-record association | Mirror schedule and transaction consumers | Native scheduled bodies retain signatory, expiry, deletion, and parent/child relationships | Schedule rows and native child records; no executable contract child result | Full test corpus and parity validation |
| Files and system files | File suites and lifecycle ledger-ID helper | File translators and file state changes | Mirror file transaction consumers; diverse-state validation | File contents, metadata, system-file changes, and externalized ledger ID survive lifecycle boundaries | File transactions and state changes without contract-local query output | Full test corpus; `DiverseStateValidation` compile/ownership check |
| Node administration and staking/rewards | Node and staking suites | Node translators; account transfer lists | Mirror node and staking/reward consumers; balance validator | Node IDs and retained staking/reward records remain interpretable | Native node and reward-related records | Full test corpus; known isolated node-reward reconciliation defect disclosed |
| Atomic batch | Native atomic-batch suites | `AtomicBatchTranslator`, inner transaction records and state changes | Mirror transaction and block consumers | Ordering, rollback, authorization, and native failure atomicity | Native batch and inner records only; no executable failure primitive | Full test corpus and block parity |
| Congestion and fee metadata | Native congestion/fee/throttle suites | Record fee fields, transfer lists, fee schedule state | Mirror transaction and assessed-fee consumers | Native framework fee/congestion behavior remains represented | Native fee transfers and statuses without EVM gas/result output | Full test corpus and balance/token validators |
| Record and block parity | All retained operations | Record stream writer; block stream writer and block translators | `TransactionRecordParityValidator`, block validators, pinned importer | The two stream forms describe the same retained transaction effects | Equivalent native transaction, state-change, and identifier semantics | Full HAPI validation plus importer block corpus |
| Historical contract results, logs, actions, bytecode, and state changes | Immutable Fixture A/B only | Historical record/sidecar/block schemas and compatibility translators | Pinned importer and authenticated fixture validators | Previously produced data remains readable after execution retirement | Historical rows remain ingestible; no new live successful result/action/state sidecar | Fixture A/B official importer regression |
| Reconnect-produced records | Certified Fixture B runtime and unchanged reconnect harness | Normal record/block stream writers after transferred state load | Pinned importer, fixture governance, reconnect evidence | Native operations remain interpretable after learner state transfer | Same native entities/statuses after reconnect; protected historical maps unchanged | Exact artifact/input/harness equivalence plus Fixture B importer |
| Unsupported executable bodies | Eight intentional rejection constructions | Normal rejected-transaction record/precheck path | HAPI status assertions; record parser where a record exists | Unsupported bodies fail closed without entities, state, results, logs, actions, or sidecars | `INVALID_TRANSACTION_BODY` precheck, except intentional `INVALID_CONTRACT_ID` record | Source inspection, census, full run, absence scan |

## Eight intentional rejection sites

| Owner | Construction | Expected status | Record/output consequence | Status |
| --- | --- | --- | --- | --- |
| `AuthenticatedHistoricalFixtureConsumerTest` | explicit contract create | `INVALID_TRANSACTION_BODY` precheck | No record-side successful result, entity, state, log, action, or sidecar; later native account/token/topic operations remain valid | Intentional |
| same | direct `HapiContractCall` | `INVALID_TRANSACTION_BODY` precheck | Same | Intentional |
| `HistoricalContractExecutionRejection` | explicit contract create | `INVALID_TRANSACTION_BODY` precheck | No successful output; native continuity account follows | Intentional |
| same | contract call | `INVALID_TRANSACTION_BODY` precheck | Same | Intentional |
| same | contract update | `INVALID_TRANSACTION_BODY` precheck | Same | Intentional |
| same | contract delete | `INVALID_TRANSACTION_BODY` precheck | Same | Intentional |
| same | Ethereum transaction | `INVALID_TRANSACTION_BODY` precheck | Same | Intentional |
| `Issue1765Suite.recordOfInvalidContractUpdateSanityChecks` | invalid contract update | `INVALID_CONTRACT_ID` consensus status | One deterministic failed record with memo and fee transfer validation; no contract mutation or execution output | Intentional |

## Removed-output classification

Live retained suites have zero successful contract create, contract call,
Ethereum execution, and contract-local query sites. Contract result, log,
action, bytecode, and state-change shapes remaining in schemas, translators, or
Fixture A/B are `HISTORICAL_PARSER_COMPATIBILITY` or
`IMMUTABLE_HISTORICAL_FIXTURE`, not live support. Core constructor classes are
unregistered infrastructure pending dependency/resource closure and are not
suite execution roots.

## Corpus authority

- Fixture A proves historical results (`3`), logs (`1`), actions (`3`),
  semantic sidecars (`7`), and complete blocks (`139`) remain ingestible.
- Fixture B proves historical results (`3`), logs (`1`), no actions, semantic
  sidecars (`4`), complete blocks (`131`), and protected state inventory remain
  ingestible after the post-write boundary.
- The certified native runtime corpus proves zero executed contract results,
  zero actions, zero execution sidecars, retained native records, and valid
  blocks. P07-11E additionally uses the clean exact-head full HAPI stream run
  for broad retained-native record validation.

## Readiness gates

P07-11E may classify PR #30 `READY_FOR_REVIEW` only after the official importer
regressions, fixture authentication, executable/removed-output scans,
compilation, clean full test attribution, exact-head CI, documentation review,
and PR-description review are complete. The known `StreamValidationTest`
`0.0.801` mismatch and unrelated Spotless drift must be disclosed, not changed.
