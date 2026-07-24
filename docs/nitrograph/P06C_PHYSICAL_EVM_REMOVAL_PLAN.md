# P06C Physical EVM Removal Plan

Status: plan only. P06C is not authorized by P06B-7.

## Separation of removal states

1. **Removed from native packaging:** the contract implementation, Besu, Tuweni, EVM engine,
   executable precompile/native libraries, system-contract runtime, and standalone entry points.
2. **Retained in the full distribution:** the complete executable legacy stack and its tests.
3. **Retained in repository source:** executable implementation modules, integrations, and tooling
   needed to keep the full distribution and fixture generation reproducible.
4. **Fixture generation only:** pinned full-runtime node setup, public compromised-by-design
   identities, and executable generation suites.
5. **Tests only:** executable handler, system-contract, standalone, and precompile suites.
6. **Retained indefinitely:** historical schemas, state keys, service names, codecs, PBJ/protobuf
   models, neutral historical values, record/sidecar/block translation, and fixture provenance.

## Candidate source sequence

| Source/module | P06C disposition | Prerequisite |
|---|---|---|
| `hedera-smart-contract-service-impl` | delete after full-runtime retirement | fixture generator relocated; full tests retired |
| Besu/EVM integration code | delete | no full executable distribution |
| system-contract implementations | delete | full handler retirement |
| standalone `TransactionExecutors` | delete | standalone tooling retired or relocated |
| Solidity and executable Ethereum helpers | delete | fixture generation externalized |
| executable contract tests | relocate or delete | equivalent historical fixture gates retained |
| `app-service-contract` historical API | retain | permanent compatibility boundary |
| V0.49/V0.65 schemas and state IDs | retain | permanent saved-state compatibility |
| historical PBJ/protobuf stream models | retain | record/sidecar/block/mirror compatibility |

## Irreversible sequence

1. Freeze the final full-runtime fixture-generation commit and reproducible tooling.
2. Move fixture generation to an explicitly isolated tooling repository or artifact workflow.
3. Retire standalone execution and its application entry points.
4. Retire the full executable distribution and full provider factories.
5. Remove executable service modules and their Gradle projects.
6. Remove now-unused third-party dependencies, module descriptors, notices, and CI jobs.
7. Re-run repository-wide source, binary, license, and SBOM absence checks.

## Final gates

- Repository builds without executable contract source modules.
- Native artifact semantics and exact historical compatibility remain unchanged.
- All four retained maps remain readable and unchanged.
- Save, restart, replay, real reconnect, and state synchronization pass.
- Five legacy bodies reject with `INVALID_TRANSACTION_BODY`.
- Official record, sidecar, block-stream, and mirror ingestion pass.
- No executable source, binary, service provider, reflection target, or native library remains.
- Consensus, platform-sdk, persisted identifiers, codecs, state IDs, and schema versions remain
  unchanged.
- Full-runtime retirement is explicit, reviewed, and irreversible.

