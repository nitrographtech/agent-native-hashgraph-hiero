# P07-9 Residual Contract Ownership Census

## Scope and baseline

This is a census-only checkpoint. It records the residue after P07-8 and does
not authorize or perform source deletion.

- Baseline branch: `agent-native`
- P07-8 merge commit: `6f91c0121616960dc5e60471f995a0607587966a`
- Census branch: `p07/remove-residual-contract-implementation`
- Residual `app-service-contract-impl` production files: 5
- Production Java files importing Besu: 12
- Production Java files importing Tuweni: 37

The counts use production source paths matching `**/src/main/**/*.java`.
Generated output and test source sets are excluded.

## Residual module inventory

| File | Package / module | Consumers and purpose | External types | Classification | Proposed owner and feasibility |
| --- | --- | --- | --- | --- | --- |
| `hedera-node/hedera-smart-contract-service-impl/src/main/java/com/hedera/node/app/service/contract/impl/schemas/V0490ContractSchema.java` | `com.hedera.node.app.service.contract.impl.schemas` | Deprecated old-FQN forwarder to the neutral `history.V0490ContractSchema`. `hedera-state-validator` imports its `BYTECODE_STATE_ID`. It preserves a historical source/API name; schema definitions are already neutral. | None | `HISTORICAL_SCHEMA_FORWARDER`, `MOVE_TO_NEUTRAL_API` | Move unchanged into `app-service-contract`, retaining the exact FQN. No schema, state ID, key, codec, or registration change is required. |
| `hedera-node/hedera-smart-contract-service-impl/src/main/java/com/hedera/node/app/service/contract/impl/schemas/V065ContractSchema.java` | `com.hedera.node.app.service.contract.impl.schemas` | Deprecated old-FQN forwarder to the neutral `history.V065ContractSchema`. Imported by `EmbeddedVerbs`, `LambdaplexVerbs`, and `Hip1195BasicTests`. | None | `HISTORICAL_SCHEMA_FORWARDER`, `MOVE_TO_NEUTRAL_API` | Move unchanged into `app-service-contract`, retaining the exact FQN. |
| `hedera-node/hedera-smart-contract-service-impl/src/main/java/com/hedera/node/app/service/contract/impl/utils/ConversionUtils.java` | `com.hedera.node.app.service.contract.impl.utils` | Used only by `test-clients`: transaction translators, DSL entities, assertion helpers, HAPI operations, state-change helpers, and contract/crypto suites. It has no application, historical-adapter, PBJ-owner, or mirror-runtime consumer. | Besu `Address`, `Log`, `LogTopic`, `LogsBloomFilter`; Tuweni `Bytes`; PBJ/HAPI/headlong/token types | `BESU_RESIDUE`, `TUWENI_RESIDUE`, `NEUTRAL_COMPATIBILITY_UTILITY` | Split at the test-client boundary. Move generic address helpers to an existing test-client utility; fold Besu log/bloom helpers into their test translator owner. Do not introduce Besu or Tuweni into the neutral API. |
| `hedera-node/hedera-smart-contract-service-impl/src/main/java/com/hedera/node/app/service/contract/impl/utils/OpcodeUtils.java` | `com.hedera.node.app.service.contract.impl.utils` | No production or test consumer remains. It maps six EVM opcodes to historical `CallOperationType` values. | HAPI `CallOperationType` only | `DEAD_EXECUTION_RESIDUE`, `DELETE_IN_P07_9` | Delete after an exact reverse-reference check in the implementation wave. |
| `hedera-node/hedera-smart-contract-service-impl/src/main/java/module-info.java` | `com.hedera.node.app.service.contract.impl` | Temporary module shell exporting the schema and utility packages. It retains Besu EVM/datatypes and Tuweni module requirements solely for `ConversionUtils`. | Besu, Tuweni, contract API, HAPI, PBJ, headlong, token | `MODULE_SHELL`, `DELETE_IN_P07_9` | Delete with the Gradle project after the two forwarders and test-client utilities move. |

There are no fixture-tooling, authenticated-fixture generation, mirror runtime,
or production application consumers of `ConversionUtils` or `OpcodeUtils`.
The compatibility release is immutable and built from its pinned historical
source; it does not consume the post-P07-8 module.

## Dependency and reverse-dependency graph

```text
app-service-contract-impl
├── V0490ContractSchema (old-FQN forwarder)
│   └── app-service-contract/history/V0490ContractSchema
│       └── permanent historical BYTECODE and STORAGE definitions
├── V065ContractSchema (old-FQN forwarder)
│   └── app-service-contract/history/V065ContractSchema
│       └── permanent EVM_HOOK_STATES and LAMBDA_STORAGE definitions
├── ConversionUtils
│   ├── test-clients translators and helpers
│   ├── Besu datatypes/EVM log types
│   └── Tuweni Bytes
├── OpcodeUtils
│   └── no remaining consumer
└── module-info
    └── temporary exports and dependency edges
```

Reverse graph:

```text
hedera-state-validator ─┐
test-clients ───────────┼── old schema FQNs ── app-service-contract-impl
test-clients ───────────┴── ConversionUtils ─── app-service-contract-impl

settings.gradle.kts ─────── project/artifact mapping
test-clients module-info ── JPMS requires edge
hedera-app packaging ────── defensive exclusions for the already-unpackaged jar/classes
```

After moving the two forwarders without changing their FQNs, the permanent
direction becomes:

```text
hedera-app / state-validator / test-clients
    → app-service-contract
    → neutral historical schema owners
```

No permanent historical component needs a dependency on executable
implementation ownership.

## Repository-wide Besu census

Twelve production Java files import Besu:

| Ownership | Files | Purpose / imported types | P07-9 status |
| --- | ---: | --- | --- |
| Residual contract implementation | 1 | `ConversionUtils`: address and historical/test log conversion (`Address`, `Log`, `LogTopic`, `LogsBloomFilter`) | Remove from this module; relocate/split under test-client ownership. |
| Test clients | 10 | Translators and legacy EVM suites use `Address`, `Log`, or `Hash`; native-library verification/signing use Besu native secp256k1/secp256r1 wrappers | Not historical runtime ownership. Inventory for a bounded test/tooling cleanup after module collapse. |
| `platform-sdk/base-crypto` | 1 | `EcdsaSecp256k1Verifier` uses `LibSecp256k1` for platform cryptographic verification | `PROTECTED_PLATFORM_CRYPTO`; out of scope and must not change. |

Exact test-client importers:

- `junit/support/translators/BaseTranslator.java`
- `suites/contract/Utils.java`
- `suites/contract/evm/Evm38ValidationSuite.java`
- `suites/contract/evm/batch/AtomicEvm38ValidationSuite.java`
- `suites/contract/hapi/ContractCallLocalSuite.java`
- `suites/contract/opcodes/AtomicOpCodesSuite.java`
- `suites/contract/opcodes/ExtCodeHashOperationSuite.java`
- `suites/crypto/AutoCreateUtils.java`
- `suites/regression/system/BesuNativeLibVerificationTest.java`
- `utils/Signing.java`

Protected importer:

- `platform-sdk/base-crypto/src/main/java/org/hiero/base/crypto/engine/EcdsaSecp256k1Verifier.java`

## Repository-wide Tuweni census

Thirty-seven production Java files import Tuweni:

| Ownership | Files | Purpose | P07-9 status |
| --- | ---: | --- | --- |
| Residual contract implementation | 1 | `ConversionUtils` byte/address/log conversion | Remove from this module; relocate or neutralize in test clients. |
| Test clients | 36 | HAPI transaction utilities, retained legacy EVM suites, traceability encoding, and ABI result helpers use `Bytes`, `Bytes32`, or `UInt256` | No production application or historical-adapter ownership. Retain only where still needed by test/tooling, then address in a separate measured cleanup. |
| Platform SDK / base crypto | 0 | None | No protected Tuweni exception was found. |

The 36 test-client importers span:

- transaction/assertion utilities (6);
- contract/EVM/HAPI/opcode/record suites (18);
- traceability, crypto, throttling, and precompile suites (6);
- ABI result helper types (6).

The exact machine-readable source of truth for implementation work is:

```bash
rg -l '^import org\.hyperledger\.besu\.' --glob '**/src/main/**/*.java'
rg -l '^import org\.apache\.tuweni\.' --glob '**/src/main/**/*.java'
```

## Historical and protected ownership

Permanent historical ownership already resides in
`hedera-smart-contract-service` (`app-service-contract`):

- neutral schema implementations for V0.49 and V0.65;
- persisted service name and state definitions;
- neutral contract stream-builder APIs;
- historical read-only store interfaces and adapters;
- historical runtime and store factories.

The schema forwarders contain no implementation behavior. Moving their source
file ownership while retaining their exact FQNs does not change persisted state
or wire compatibility.

The only protected repository-wide Besu dependency identified by this census is
the `platform-sdk/base-crypto` secp256k1 verifier. P07-9 must neither modify it
nor use its existence to retain unrelated contract/test dependencies.

## Architecture gate

1. **Can legitimate historical components move without changing FQNs or persisted contracts?** Yes. Move the two deprecated schema forwarders into `app-service-contract` with their existing `com.hedera.node.app.service.contract.impl.schemas` FQNs. Their neutral parent schemas, state keys, IDs, codecs, and registration behavior remain unchanged.
2. **Can `app-service-contract-impl` then be physically deleted?** Yes. `ConversionUtils` is test-client residue, `OpcodeUtils` is unreferenced, and the descriptor is only a module shell.
3. **Which Besu dependencies remain after module deletion?** Test-client legacy EVM/tooling imports and the protected `platform-sdk/base-crypto` secp256k1 import. No historical runtime Besu requirement remains.
4. **Which Tuweni dependencies remain after module deletion?** Test-client-only legacy EVM, HAPI, traceability, and ABI helper imports. No platform or historical runtime Tuweni requirement remains.
5. **Are any remaining dependencies protected exclusively by platform crypto?** Yes: Besu native secp256k1 in `platform-sdk/base-crypto`. No Tuweni dependency is protected by that boundary.
6. **Is there a safe bounded P07-9 deletion set?** Yes.

## Recommended bounded implementation wave

1. Move the two schema forwarders unchanged into `app-service-contract` and
   verify old-FQN linkage plus schema/state inventory.
2. Move or split `ConversionUtils` into test-client ownership, keeping
   Besu/Tuweni-specific conversion out of neutral and runtime modules.
3. Delete unreferenced `OpcodeUtils`.
4. Delete the residual implementation module descriptor and Gradle project.
5. Remove the settings mapping, test-client JPMS/project dependency, and
   obsolete application packaging exclusions.
6. Recount repository Besu/Tuweni importers and validate that remaining
   ownership is test/tooling or the protected platform crypto exception.
7. Run the complete protected compatibility, runtime, reconnect, and mirror
   gates before claiming module collapse complete.

Repository-wide Besu/Tuweni deletion is not part of this bounded set. The
test-client residue should be handled as a separately measured follow-up, and
the protected platform cryptography dependency requires separate authorization.
