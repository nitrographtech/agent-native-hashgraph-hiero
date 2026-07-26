# P07-10A signing ownership

## Method-level migration

| Former method | Consumers | Classification | Result |
|---|---|---|---|
| `Signing.signMessage(byte[], byte[])` | authorization, integration, batch authorization tests | `NATIVE_ECDSA_SIGNING` | Moved without semantic change to `NativeEcdsaSigning.signMessage(byte[], byte[])` |
| `Signing.signMessage(EthTxData, byte[])` | `HapiEthereumCall`, `HapiEthereumContractCreate` | retired executable Ethereum construction | Temporarily retained with its executable callers; next deletion candidate |
| `Signing.signMessage(EthTxData, byte[], boolean)` | malformed/executable Ethereum call construction | retired executable Ethereum construction | Temporarily retained with its executable caller; next deletion candidate |
| `Signing.signMessageEd25519(byte[], byte[])` | none | dead residue | Deleted |

`NativeEcdsaSigning` retains the exact compact recoverable secp256k1 signature
serialization and the historical `v = recoveryId + 27` behavior. It depends on the
test-client native secp256k1 binding, not on Besu datatypes or Tuweni. No dependency
was added to `platform-sdk/base-crypto`, and that protected implementation was not
modified.
