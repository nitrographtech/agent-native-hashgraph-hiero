# Nitrograph Runtime Architecture

Status: P06B-7A engineering control artifact

```mermaid
flowchart TD
  HC[Hashgraph Consensus] --> NR

  subgraph NR[NATIVE RUNTIME]
    APP[Nitrograph application runtime]
    ACC[Network Accounts]
    ASSET[Native Asset Service]
    COORD[Coordination Layer]
    STREAMS[Records and Block Streams]
    NATIVE_JAR[Native HederaNode application jar]
    NATIVE_FACTORY[Historical runtime and store factories]
    NATIVE_JAR --> NATIVE_FACTORY --> APP
    APP --> ACC
    APP --> ASSET
    APP --> COORD
    APP --> STREAMS
  end

  subgraph HCMP[HISTORICAL READ-ONLY COMPATIBILITY]
    PROVIDER[HistoricalContractRuntimeProvider]
    SCHEMAS[HistoricalContractStateService and retained schemas]
    API[Neutral contract compatibility API<br/>record builders and read-only retained-map adapters]
    REJECT[Fail-closed legacy handlers]
    NEUTRAL[HistoricalLog<br/>HistoricalContractResult<br/>HistoricalContractAction<br/>HistoricalContractStateChanges<br/>HistoricalStorageChange<br/>HistoricalContractBytecode]
    PROVIDER --> SCHEMAS
    SCHEMAS --> API
    PROVIDER --> REJECT
    SCHEMAS --> NEUTRAL
    NEUTRAL --> STREAMS
  end

  APP --> PROVIDER

  subgraph FULL[FULL EXECUTABLE LEGACY RUNTIME — REMAINING, NON-NATIVE]
    FULL_PROVIDER[FullContractRuntimeProvider]
    FULL_JAR[Full HederaNode application jar]
    CSI[ContractServiceImpl]
    EVM[Besu/EVM/system contracts]
    FULL_JAR --> FULL_PROVIDER --> CSI --> EVM
  end

  subgraph FIXTURE[FIXTURE GENERATION ONLY]
    PINNED[Pinned pre-activation full node]
    PUBLIC_ID[Public compromised fixture identities]
    PINNED --> PUBLIC_ID
  end

  subgraph DEFERRED[DEFERRED NITROGRAPH SERVICES]
    CREDITS[Credits]
    STAKING[Network Staking]
    ID[Agent Identity]
    CAP[Capability Registry]
    DISC[Discovery]
    PAY[Payments / x402]
    JOBS[Jobs]
    RECEIPTS[Receipts]
    DELEGATION[Delegation]
    REP[Reputation]
  end
```

## Control classifications

- **Native runtime:** consensus-facing application services that may execute in Nitrograph mode.
- **Historical compatibility:** retained schemas and neutral values; readable and translatable,
  never executable.
- **Full executable legacy runtime:** retained only to preserve the upstream full distribution and
  controlled fixture generation. It is not selected in Nitrograph mode.
- **Removed:** direct executable runtime types from shared log, result, action, state/storage, and
  bytecode translation.
- **Remaining:** module dependencies and packaged jars supporting the combined full/native build.
- **P06B-7A split:** native and full application jars are physically distinct. The neutral
  compatibility API is owned by `app-service-contract`; the native jar contains only historical
  provider/store service metadata. Executable dependency jars remain packaged until P06B-7.
- **Deferred:** new Nitrograph services listed above; none are implemented by P06B.

The frozen invariant is: historical contract state remains readable and migratable but is never
executable on Nitrograph.
