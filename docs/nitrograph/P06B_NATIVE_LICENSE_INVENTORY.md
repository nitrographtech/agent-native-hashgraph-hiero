# P06B Native License Inventory

Status: packaging isolation implemented; lifecycle evidence is recorded separately.

## Baseline and final scope

The inventory covers the exact jars under the native distribution's `data/apps` and `data/lib`
directories. It does not assert that removing a jar removes a license family from the product.
The full executable distribution retains every component removed from native packaging.

| Inventory | Artifacts | Bytes | Inventory SHA-256 |
|---|---:|---:|---|
| P06B-7 baseline | 205 | 130,610,581 | `669d066def5639f3ccd4e5873dc5922ee0ef1dbbe9616476140aad8aac95b3c4` |
| First checkpoint | 199 | 127,945,555 | `82d0303d528c68544fc84dd92d9e7ac807b9acf1e8ead4e7fc3de48f9adf8bf0` |
| Final native artifact | 191 | 95,093,936 | `fb4673ec8047330e47ed361abfc8dbf6b29eac4d82d441b45dc50f06753e86b0` |

## Removed components

The following coordinates are removed only from the native distribution:

- project `:app-service-contract-impl`
- `org.hyperledger.besu:besu-datatypes:25.2.2`
- `org.hyperledger.besu:besu-native-common:1.3.0`
- `org.hyperledger.besu:evm:25.2.2`
- `org.hyperledger.besu.internal:algorithms:25.2.2`
- `org.hyperledger.besu.internal:rlp:25.2.2`
- `org.hyperledger.besu:arithmetic:1.1.2`
- `org.hyperledger.besu:blake2bf:1.1.2`
- `org.hyperledger.besu:gnark:1.1.2`
- `org.hyperledger.besu:secp256k1:1.3.0`
- `org.hyperledger.besu:secp256r1:1.1.2`
- `org.apache.tuweni:tuweni-bytes:2.4.2`
- `org.apache.tuweni:tuweni-units:2.4.2`
- `tech.pegasys:jc-kzg-4844:1.0.0`

These components are Apache-2.0 components. Apache-2.0 remains present in the native distribution
through other retained components, so no top-level license family is claimed as removed.

## Retained contract-adjacent components

- `app-service-contract` is retained for historical schemas, state keys, neutral record-builder
  contracts, and read-only retained-map adapters.
- `headlong` is retained as a generic ABI/RLP data utility used by native token hooks and
  historical Ethereum-format decoding. It does not provide an EVM engine.
- Bouncy Castle is retained for generic account-signature, hashing, and key handling.
- `hedera-cryptography-hints` and `hedera-cryptography-wraps` are retained for consensus history and
  hints services; they are not contract execution libraries.
- JNA, libsodium, zstd, and Netty native transport artifacts remain for generic native services.

## Findings

- Unresolved license findings: **zero**.
- Unresolved binary provenance for removed candidates: **zero**; the Gradle dependency graph and
  exact hashes identify every removed component.
- Waivers introduced by P06B-7: **zero**.
- Full-distribution license obligations are unchanged.
