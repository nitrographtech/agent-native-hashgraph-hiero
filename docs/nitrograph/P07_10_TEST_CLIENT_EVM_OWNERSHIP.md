# P07-10 Test-Client EVM Ownership Census

## Checkpoint

- Baseline: merged P07-9 commit `395a34e02753ba7a231aa432bdd1da4a80397c5d`
- Branch: `p07/remove-test-client-evm-residue`
- Scope: census and architecture gate only
- Production runtime changes: zero
- Test-client source deletions: zero

This census counts Java under `src/main`; the `test-clients` module intentionally
places executable suites and reusable HAPI tooling in that source set. Generated
output is excluded.

## Exact import inventory

| Owner | Besu importers | Tuweni importers | Unique files |
| --- | ---: | ---: | ---: |
| `test-clients` main source | 11 | 37 | 40 |
| Fixture tooling | 0 | 0 | 0 |
| Test source | 0 | 0 | 0 |
| Test fixtures | 0 | 0 | 0 |
| `platform-sdk/base-crypto` | 1 | 0 | 1 |
| Other repository production source | 0 | 0 | 0 |

The 40 direct test-client importers occupy 1,395,913 bytes. Thirty-two are
suite/helper files under `com.hedera.services.bdd.suites` (1,210,128 bytes);
eight shared translator, transaction, assertion, and utility files occupy
185,785 bytes.

### Besu importers

| Path (under `hedera-node/test-clients/src/main/java`) | Imported role | Classification | Disposition gate |
| --- | --- | --- | --- |
| `.../junit/support/translators/BaseTranslator.java` | `Address`, `Log` | `HISTORICAL_PARSER`, `DEAD_EXECUTION_TRANSLATOR` (mixed) | Split historical record translation from live contract translation before deleting |
| `.../suites/contract/Utils.java` | `Hash` | `DEAD_EXECUTION_SUITE` | Delete with contract-suite closure |
| `.../suites/contract/evm/Evm38ValidationSuite.java` | `Hash` | `DEAD_EXECUTION_SUITE` | Delete |
| `.../suites/contract/evm/batch/AtomicEvm38ValidationSuite.java` | `Hash` | `DEAD_EXECUTION_SUITE` | Delete |
| `.../suites/contract/hapi/ContractCallLocalSuite.java` | `Address` | `DEAD_EXECUTION_SUITE` | Delete |
| `.../suites/contract/opcodes/AtomicOpCodesSuite.java` | `Hash` | `DEAD_EXECUTION_SUITE` | Delete |
| `.../suites/contract/opcodes/ExtCodeHashOperationSuite.java` | `Hash` | `DEAD_EXECUTION_SUITE` | Delete |
| `.../suites/crypto/AutoCreateUtils.java` | `Address` | `GENERAL_BYTE_UTILITY` | Retain native account/alias semantics; replace address wrapper |
| `.../suites/regression/system/BesuNativeLibVerificationTest.java` | SECP256K1/SECP256R1 | `TEST_CLIENT_CRYPTO` | Retain until signing closure is replaced or isolated |
| `.../utils/EvmConversionUtils.java` | `Address`, `Log`, `LogTopic`, bloom | mixed conversion/translator ownership | Split by method |
| `.../utils/Signing.java` | native secp256k1 | `TEST_CLIENT_CRYPTO` | Retain: used by native ECDSA authorization tests as well as retired Ethereum builders |
| `platform-sdk/base-crypto/.../EcdsaSecp256k1Verifier.java` | native secp256k1 | `PROTECTED_PLATFORM_CRYPTO` | Permanently protected; out of scope |

### Tuweni importers by responsibility

| Responsibility | Exact files | Classification | Disposition gate |
| --- | --- | --- | --- |
| Shared transaction/assertion infrastructure | `ContractFnResultAsserts`, `HapiTxnOp`, `TxnUtils`, `HapiParserUtil` | `GENERAL_BYTE_UTILITY`, `HISTORICAL_PARSER` | Replace byte-only use method-by-method; preserve malformed/historical parsing |
| Retired Ethereum builder | `HapiEthereumContractCreate` | `DEAD_TRANSACTION_HELPER` | Delete after rejection vectors no longer use it |
| EVM validation | `Evm38ValidationSuite`, `Evm46ValidationSuite`, `AtomicEvm38ValidationSuite`, `AtomicEvm46ValidationSuite` | `DEAD_EXECUTION_SUITE` | Delete |
| Contract create/call/delete | `ContractCallLocalSuite`, `ContractCreateSuite`, `ContractDeleteSuite`, `AtomicContractCallSuite`, `AtomicContractCreateSuite`, `AtomicContractDeleteSuite` | `DEAD_EXECUTION_SUITE` | Delete |
| Opcode/storage execution | `AtomicOpCodesSuite`, `Create2OperationSuite`, `ExtCodeHashOperationSuite`, `SStoreSuite` | `DEAD_EXECUTION_SUITE` | Delete |
| Live records/logs/tracing | `RecordsSuite`, `AtomicRecordsSuite`, `TraceabilitySuite`, `EncodingUtils` | `DEAD_EXECUTION_SUITE`; some historical vector logic | Move only proven historical vectors, then delete live suites |
| Other retired contract execution | `ERC20ContractInteractions`, `AtomicBatchPrecompileSCTest`, `GasLimitThrottlingSuite` | `DEAD_EXECUTION_SUITE` | Delete |
| ABI result helpers | `AddressResult`, `BoolResult`, `ContractCallResult`, `ErrorMessageResult`, `FunctionParameters`, `SimpleBytesResult`, `HTSPrecompileResult` | `DEAD_EXECUTION_TRANSLATOR` | Delete with reverse consumers |
| Native account/fee support | `AutoCreateUtils`, `CryptoCreateSimpleFeesTest` | `GENERAL_BYTE_UTILITY` | Replace bytes without changing alias or fee semantics |
| Conversion utility | `EvmConversionUtils` | mixed | Split by method |

## Dependency closures

### Conversion and translation

```text
EvmConversionUtils
├── neutral address helpers (byte[] / PBJ / headlong)
│   ├── native account and token DSL
│   ├── rejection request construction
│   └── state-change inspection
├── Besu Address conversion
│   ├── AutoCreateUtils (native alias behavior)
│   └── BaseTranslator
└── Besu Log / bloom conversion
    ├── BaseTranslator
    ├── ContractCreateTranslator
    ├── ContractCallTranslator
    └── EthereumTransactionTranslator
```

The address half is not dead execution ownership. The log/bloom half feeds
live-execution translators that must be separated from any retained historical
record translator before removal.

### Ethereum parser, RLP, and signing

```text
HapiEthereumContractCreate / HapiEthereumCall
    → Signing
    → Besu native secp256k1

EthTxData / EthTxSigs
    → headlong RLP
    → historical/malformed Ethereum transaction parsing
    → deterministic rejection vectors
```

Headlong RLP is not a Besu or Tuweni dependency. It remains a bounded historical
parser dependency unless reverse-consumer deletion proves it unused. A new RLP
codec is not justified.

`Signing` is mixed: retired Ethereum transaction generation consumes it, but
native ECDSA authorization, account, topic-fee, airdrop, and integration suites
also consume it. Deleting it with Ethereum suites would remove required native
coverage. Redirecting it to `platform-sdk/base-crypto` would create an
unreviewed dependency direction and is forbidden in P07-10.

### Tuweni byte and integer closure

```text
tuweni.bytes.Bytes / Bytes32
├── dead contract suites and ABI result wrappers
├── general transaction byte wrapping and hex conversion
├── native alias/fee tests
└── historical/rejection parser helpers

tuweni.units.UInt256
└── contract traceability EncodingUtils
    └── live traceability suite (dead after execution retirement)
```

No current source imports Tuweni RLP. `UInt256` is confined to the dead
traceability closure. Most retained Tuweni use is byte wrapping, slicing,
padding, or hex conversion and is replaceable, but only after boundary tests
for width, signedness, leading zeroes, empty input, and malformed hex.

### Fixture and compatibility boundary

```text
fixture-tooling
    → test-clients project
    → retained full-node fixture consumer and verification infrastructure

p07-executable-fixture-compat-v1
    → immutable pinned source archive
    → no current-repository source dependency
```

Fixture tooling has zero direct Besu/Tuweni imports. The immutable compatibility
release is independently sourced and verified by tag/archive hash; current
source may delete code retained in that release.

## Active build and JPMS ownership

`test-clients` currently requires:

- `org.hyperledger.besu.datatypes`
- `org.hyperledger.besu.evm`
- `org.hyperledger.besu.internal.crypto`
- `org.hyperledger.besu.nativelib.secp256k1`
- `tuweni.bytes`
- `tuweni.units`

`base-crypto` independently requires only
`org.hyperledger.besu.nativelib.secp256k1`. Repository dependency constraints
still publish Besu datatypes, EVM, secp256k1, Tuweni bytes, and Tuweni units.
No runtime application module requires Besu or Tuweni; packaging policies
continue excluding those artifacts.

## Architecture gate and bounded implementation

The census supports a bounded P07-10, but not a blind deletion of all 40
importers.

1. Delete the cohesive live contract/EVM suite and ABI-helper closure.
2. Remove live ContractCreate/ContractCall/Ethereum builders only after replacing
   the small deterministic rejection vectors that consume them.
3. Split `BaseTranslator` and `EvmConversionUtils`; retain neutral address and
   historical parsing behavior while deleting Besu log/bloom translation.
4. Replace byte-only Tuweni use in retained native/shared helpers with PBJ/JDK
   primitives and explicit boundary tests.
5. Retain headlong RLP for historical/malformed Ethereum parsing unless its final
   reverse graph becomes empty.
6. Retain test-client secp256k1 signing temporarily because it supports native
   ECDSA behavior; do not route it through protected platform internals.
7. Never modify the one protected `base-crypto` Besu importer.

Stop if the suite closure still owns authenticated fixture consumption,
historical sidecar interpretation, deterministic rejection, or required native
ECDSA coverage. Importer-count reduction is an outcome, not the deletion rule.

