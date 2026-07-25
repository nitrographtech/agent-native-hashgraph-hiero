# P07-9 Conversion Utility Ownership

## Result

The residual implementation-owned `ConversionUtils` had no production
application, fixture-tooling, historical-adapter, PBJ-owner, or mirror-runtime
consumer. It moved to the test-client-owned
`com.hedera.services.bdd.utils.EvmConversionUtils`.

No compatibility alias remains in the removed implementation package.
Besu/Tuweni types did not enter `app-service-contract`.

## Method-level migration

| Method or constant | Dependency character | Consumers | P07-9 disposition |
| --- | --- | --- | --- |
| `NUM_LONG_ZEROS` | Primitive constant | Contract test utility | Moved to test-client utility. |
| `headlongAddressOf(AccountID)` | PBJ/headlong | DSL/state-change helpers | Moved. |
| `headlongAddressOf(ContractID)` | PBJ/headlong | DSL contract helper | Moved. |
| `headlongAddressOf(ScheduleID)` | PBJ/headlong | Test-client helpers | Moved. |
| `headlongAddressOf(proto ScheduleID)` | protobuf/headlong | Test-client helpers | Moved. |
| `headlongAddressOf(TokenID)` | PBJ/headlong | DSL token helper | Moved. |
| `headlongAddressOf(Account)` | PBJ token state/headlong | DSL account helper | Moved. |
| `asHeadlongAddress(byte[])` | Headlong; Tuweni numeric conversion | HAPI and state-change helpers | Moved. |
| `asEvmAddress(long)` | Primitive byte conversion | HAPI parser and crypto suites | Moved. |
| `priorityAddressOf(Account)` | Besu `Address` | Record translator | Moved. |
| `copyToLeftPaddedByteArray(long, byte[])` | Primitive helper | Internal implementation of `asEvmAddress` | Moved as an internal dependency. |
| `explicitFromHeadlong(Address)` | Headlong | HAPI assertions/operations | Moved. |
| `numberOfLongZero(byte[])` | Primitive helper | `UtilVerbs` | Moved. |
| `explicitAddressOf(Account)` | PBJ token state | Record translator | Moved. |
| `bloomForAll(List<Log>)` | Besu logs/bloom; PBJ bytes | Record translators | Moved. |
| `bloomFor(Log)` | Besu logs/bloom; PBJ bytes | Base translator | Moved. |
| `removeIfAnyLeading0x(PBJ Bytes)` | PBJ bytes | Contract-create/Ethereum record translators | Moved. |
| `asBesuLog(EvmTransactionLog, List<PBJ Bytes>)` | PBJ trace log; Besu/Tuweni log construction | Base translator | Moved. |
| `asLongZeroAddress(long)` | Besu/Tuweni | No consumer | Deleted. |
| `numberOfLongZero(Besu Address)` | Besu | No consumer | Deleted. |

The moved class is explicitly tooling ownership. Its remaining Besu/Tuweni
surface is a candidate for the next measured test-client cleanup wave, not a
neutral compatibility API.
