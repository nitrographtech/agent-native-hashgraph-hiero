# P07 System-Contract Native-Service Boundaries

This artifact defines the protected side of the P07-4 deletion boundary.

```text
DELETE                                      PRESERVE

EVM address + ABI selector
        |
        v
system-contract translator/call
        |
        v
synthetic/direct native dispatch  -X->      native transaction handlers
                                             |
                                             +-- account service
                                             +-- token / Native Asset Service
                                             +-- schedule service
                                             +-- exchange-rate data
                                             +-- native randomness
                                             +-- keys and authorization
                                             +-- balances and relationships
                                             +-- native fees and records
```

Removing an EVM façade removes only its ability to originate native-service
work from an EVM message frame. It does not remove or rename the native
transaction type, handler, store, state key, service name, codec, schema, or
record builder.

| EVM façade | Deleted routing | Protected implementation |
|---|---|---|
| HTS `0x167` / `0x16c` and redirects | Token translators, calls, decoders, synthetic dispatch | Token handlers, stores, relationships, balances, allowances, fees, records |
| HAS `0x16a` and account redirects | Account translators and calls | Account handlers, stores, keys, authorization, allowances, records |
| HSS `0x16b` and schedule redirects | Schedule translators and calls | Schedule handlers, stores, capacity rules, records |
| Exchange rate `0x168` | EVM result encoding | Exchange-rate state and native consumers |
| PRNG `0x169` | EVM result encoding | Native randomness and platform entropy consumers |

## Permanently protected compatibility

- `ContractService` persisted service name;
- `STORAGE`, `BYTECODE`, `EVM_HOOK_STATES`, and `LAMBDA_STORAGE`;
- state IDs, keys, codecs, schema versions, and schema registration order;
- V0.49 and V0.65 schemas;
- PBJ/protobuf contract and Ethereum models;
- neutral stream-builder and historical store interfaces;
- historical result, log, action, state/storage, and bytecode values;
- signed record, sidecar, block-stream, and mirror semantics;
- deterministic native rejection of legacy executable bodies.

## Former-address behavior

After removal, no Hedera system contract is registered. Calls to former
addresses follow the existing missing/non-extant target behavior of the
remaining EVM processor. P07-4 does not add a replacement compatibility
contract or a new error protocol.

Generic EVM cryptographic precompiles remain for the later EVM wave.
Platform and native cryptography, including `platform-sdk/base-crypto`, is
outside P07-4.

## Fixture impact

`HistoricalContractStateFixtureCreation` uploads and creates
`SimpleStorage`, calls `SimpleStorage.set(424242)`, uploads and creates
`StorageAccessHook`, creates an account with hook metadata, and writes hook
storage through a native operation. It contains no HTS, HAS, HSS,
exchange-rate, PRNG, redirect, or precompile call.

Therefore the authenticated fixture is classified:

- ordinary contract creation: present;
- ordinary contract call: present;
- system-contract call: absent;
- precompile call: absent;
- native-service redirect: absent.

The fixture action tracer remains in fixture tooling and its three actions,
two state-change groups, two bytecode records, and seven sidecars stay
protected.
