# P06A Release-Safe Fixture Identity Map

Status: investigation complete; publication blocked.

Classification: **B. IDENTITY_CONTINUITY_REQUIRED**

This document contains no private-key contents or private-key-derived values.

## Fixture identity inventory

The frozen pre-activation corpus contains one private signing-key file:

- `data/keys/s-private-node1.pem`

It contains one public signing certificate:

- `data/keys/s-public-node1.pem`

The saved state at round 10738 contains a one-entry active roster in
`currentRoster.json`. Its `gossipCaCertificate` SHA-256 is
`09318a0e1bb9193fcfb7e3c49cc2a5390fac0eef98604d8401a70ce5f72fbc20`,
which exactly matches the DER SHA-256 of `s-public-node1.pem`.

No PEM private-key marker or private-key filename reference was found in the saved-state
directory. The private key is therefore local runtime material, but its corresponding
public identity is committed into the active roster represented by the saved state.

## Startup dependency

The node startup path is:

1. `ServicesMain` obtains the active roster and invokes
   `CryptoStatic.initNodeSecurity(configuration, selfId, rosterEntries)`.
2. `EnhancedKeyStoreLoader` reads the local signing private key from
   `paths.keysDirPath`. A missing signing key fails `verify()` and `keysAndCerts()`.
3. The signing certificate is not selected from the local public PEM. It is selected
   from the active roster entry by node ID.
4. `PlatformBuilder.withKeysAndCerts()` signs a test message with the loaded private key
   and verifies it with the roster certificate. Startup throws if they do not match.

The local node also receives a freshly generated agreement key and certificate during
loading, but that does not remove the requirement for the roster-matching signing
private key. Event and state signing subsequently use the resulting `KeysAndCerts`.

## Persisted and local boundaries

| Material | Boundary | Consequence |
|---|---|---|
| Signing private key | Local filesystem only | Must never be published |
| Signing public certificate | Active roster in saved state; public PEM copy | Fixes the accepted signing identity |
| Node ID | Active roster/local startup selection | Must remain node 0 |
| Application contract state | Persisted state | Unrelated to key loading and must remain unchanged |
| Saved-state root | Commits to persisted state, including roster state | Cannot retain the same root if the roster certificate is replaced |
| Agreement key | Generated locally during startup | Does not substitute for the signing identity |
| Event/state signatures | Runtime output | Require the roster-matching signing private key |

The private key is not itself part of the historical state root. Its corresponding
public certificate is roster-bound state, so replacing the local private key alone
causes the platform key-pair verification to fail. Replacing the roster certificate
would mutate persisted state and change the historical state root.

## Classification decision

The fixture is classified **IDENTITY_CONTINUITY_REQUIRED**, not
`LOCAL_RUNTIME_SECRET_ONLY`.

An unrelated ephemeral activation key cannot satisfy the existing active-roster
certificate. The exact corresponding signing keypair is required for node startup,
event signing, and state signing under the preserved roster identity. The approved
investigation therefore stops before creating an activation workspace.

No attempt was made to:

- copy or expose the private key;
- generate or inject a substitute key;
- modify the roster, address book, node ID, or saved state;
- rewrite persisted artifacts;
- construct an archive, tag, release, or upload.

## Publication consequence

The current corpus cannot pass the release publication gate. Publishing the signing
private key is prohibited, while omitting it prevents independently downloaded
lifecycle fixtures from restarting as the preserved node identity.

Releasing state and streams for offline interpretation could be considered as a
different, narrower artifact, but it could not support the currently claimed
activation, restart, reconnect, or synchronization matrix and is not authorized by
this investigation.
