# P07 four-node post-write fixture release

- Fixture tag: `p07-four-node-postwrite-v065-storage424242-v1`
- Release: `https://github.com/nitrographtech/agent-native-hashgraph-hiero/releases/tag/p07-four-node-postwrite-v065-storage424242-v1`
- Generator tag: `p07-executable-fixture-compat-v1`
- Generator source: `64da043f766da29d0fd3e20e3f51051fdd31f5a1`
- Generator archive SHA-256:
  `6af7596d365e62561df913032daaaf4437c3eafeb75e567e33057e8eab5b7a7a`
- Generation date: 2026-07-25
- Fixture asset: `p07-four-node-postwrite-v065-storage424242-v1.tar.gz`
- Fixture size: 26,911,183 bytes
- Fixture SHA-256: `0fd75fd4da408787ddef6b5a73300ce8018e330eb2f3a8e98d11ef9fbceceaa3`
- Manifest SHA-256: `967798ff82aad71ac7b9f6b4464279479f4e2a201098a02995cfd6cd70e30439`
- Manifested files: 393 (394 including `SHA256SUMS`)
- Saved-state round: 2143
- State root:
  `0c3b973439f127093ad1d64bd9777d9a365a6d18522e6e46c6bd0fa41fa9f4e8eba8b4eac33b0cda2bfa82cf175a0894`
- Combined protected inventory:
  `5f74ae63338dfb5510d0dc09a7afafe64f5ee00f9d0c7ed5c040cb5fe82dfa48`
- SBOM SHA-256: `9890781193882c19cd5a7525ea19caf53f93ca4054b42fe6f386737950809bd5`
- License report SHA-256:
  `90e391407442435530984fd5686195205cfb0175e37439f9585f652f6668cca3`

The four-member roster contains nodes 0–3 with weight one. Public certificate fingerprints and
individual map fingerprints are frozen in `metadata.json`. Run one was selected after two clean
publishable generations proved semantic equivalence.

The archive was independently extracted and its full checksum manifest, topology, protected
inventory metadata, and private-material policy passed:

```text
tar -xzf p07-four-node-postwrite-v065-storage424242-v1.tar.gz
./p07-four-node-postwrite-v065-storage424242-v1/verify.sh \
  p07-four-node-postwrite-v065-storage424242-v1
```

The exact P07-8 non-executable application consumed the selected state on four nodes, reached
`ACTIVE`, saved and restarted it, and completed a genuine reconnect. The release is additive,
non-production, immutable, and does not replace the preactivation fixture.
