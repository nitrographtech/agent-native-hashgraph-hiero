# P07-11B Method Implementation Ledger

## Authority and scope

This ledger is synchronized with the method-level implementation that starts from census commit
`d90650fe9026fda03a1a7cbeb0ab8a4f412dd2f7`. The authoritative suite inventory is
`P07_11B_MIXED_NATIVE_SUITE_OWNERSHIP.md`.

Every unchanged method in the 61-file census remains `RETAIN` until an entry below gives it a more
specific disposition. This default means only that P07-11B has not changed the method; it does not
broaden the method's ownership or move work assigned to P07-11C. Methods containing executable
operations must receive an explicit row before the implementation is complete.

Decisions:

- `RETAIN`: already exercises a retained native path.
- `REFACTOR`: retains the tested native invariant while replacing executable setup with native
  setup.
- `DELETE`: tests only retired execution.
- `DEFER_TO_11C`: owns historical interpretation or deterministic rejection and is not changed in
  P07-11B.

## Implementation rows

| Responsibility | Suite | Method | Executable setup | Retained behavior | Native replacement | Decision | Source/resource change | Coverage | Validation |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Fees, congestion, and throttling | `integration/CongestionPricingTest` | `canUpdateGasThrottleMultipliersDynamically` | Contract deployment and repeated `ContractCall` gas load | None; the method measures retired contract-gas congestion | None | `DELETE` | Method and its contract-only imports removed; no shared resource removed | Native congestion multiplier coverage remains in `canUpdateTransferThrottleMultipliersDynamically` | `:hedera-node:test-clients:compileJava` |
| Fees, congestion, and throttling | `integration/CongestionPricingTest` | `canUpdateTransferThrottleMultipliersDynamically` | None | Native transfer congestion multiplier behavior | Already uses `CryptoTransfer` | `RETAIN` | None | Existing method retained unchanged | `:hedera-node:test-clients:compileJava` |
| Accounts and aliases | `crypto/CryptoUpdateSuite` | `updateFailsWithContractKey` | Deploys a contract and installs its contract ID as an account key | None; contract-controlled account authorization is retired | None | `DELETE` | Method and released contract imports removed | Native ED25519/ECDSA and threshold-key authorization methods remain | `:hedera-node:test-clients:compileJava` |
| Accounts and aliases | `crypto/CryptoUpdateSuite` | `updateMaxAutoAssociationsWorks` | Deploys and updates a contract account | None; contract automatic associations and successful contract update are retired | None | `DELETE` | Method and released contract/query/token-movement imports removed | Native account maximum-association coverage remains in `updateForMaxAutoAssociationsForAccountsWorks` | `:hedera-node:test-clients:compileJava` |
| Accounts and aliases | `crypto/HollowAccountFinalizationSuite` | `hollowAccountCompletionWithEthereumTransaction` | Ethereum contract creation | None; Ethereum execution is retired | None | `DELETE` | Method and Ethereum builder imports removed | Native hollow-account completion methods remain | `:test-clients:compileJava :test-clients:compileTestJava` |
| Accounts and aliases | `crypto/HollowAccountFinalizationSuite` | `hollowAccountCompletionWithContractCreate` | `ContractCreate` paid by hollow account | None; contract creation is retired | None | `DELETE` | Method removed | Native transfer and token-transfer completion remain | `:test-clients:compileJava :test-clients:compileTestJava` |
| Accounts and aliases | `crypto/HollowAccountFinalizationSuite` | `hollowAccountCompletionWithContractCall` | `ContractCall` paid by hollow account | None; contract calls are retired | None | `DELETE` | Method removed | Native transaction completion remains | `:test-clients:compileJava :test-clients:compileTestJava` |
| Accounts and aliases | `crypto/HollowAccountFinalizationSuite` | `hollowAccountCompletionViaNonReqSigIsNotAllowed` | Contract call used as the non-required-signature path | None; the asserted distinction is execution-specific | None | `DELETE` | Method removed | Native required-signature completion and negative native authorization remain | `:test-clients:compileJava :test-clients:compileTestJava` |
| Accounts and aliases | `crypto/HollowAccountFinalizationSuite` | `precompileTransferFromHollowAccountWithNeededSigFailsAndDoesNotFinalizeAccount` | Deployed contract invokes HTS transfer precompile | None; HTS precompile execution is retired | None | `DELETE` | Method, ABI tuple construction, and released execution imports removed | Native hollow-account token and transfer coverage remains | `:test-clients:compileJava :test-clients:compileTestJava` |
| Shared owner | `contract/hapi/ContractUpdateSuite` | `ADMIN_KEY` field | Suite-owned shared identifier | Native account, airdrop, hollow-account, batch, and legacy contract tests need only the immutable key name | `LegacyContractAdminVectors.ADMIN_KEY` | `REFACTOR` | Public ownership moved to narrow immutable vector owner; suite keeps a private alias for its local methods | Consumers no longer import a suite class for shared data | `:hedera-node:test-clients:compileJava` |

## Group checkpoints

The first local Gradle probe could not start because the shell had neither `JAVA_HOME` nor a `java`
executable. A temporary, repository-external Temurin 25.0.2 JDK matching CI restored validation;
`:test-clients:compileJava` then passed. P07-11C historical/rejection owners and core HAPI
operations remain outside this implementation.
