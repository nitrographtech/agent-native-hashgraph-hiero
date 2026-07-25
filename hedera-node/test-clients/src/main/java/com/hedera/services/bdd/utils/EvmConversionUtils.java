// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.utils;

import static com.esaulpaugh.headlong.abi.Address.toChecksumAddress;
import static java.util.Objects.requireNonNull;

import com.hedera.hapi.block.stream.trace.EvmTransactionLog;
import com.hedera.hapi.node.base.AccountID;
import com.hedera.hapi.node.base.ContractID;
import com.hedera.hapi.node.base.ScheduleID;
import com.hedera.hapi.node.base.TokenID;
import com.hedera.hapi.node.state.token.Account;
import com.hedera.node.app.service.token.AliasUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.evm.log.Log;
import org.hyperledger.besu.evm.log.LogTopic;
import org.hyperledger.besu.evm.log.LogsBloomFilter;

/**
 * Test-client address and historical-log conversions retained temporarily after executable engine
 * retirement. This residue is not used by either production application profile and is scheduled
 * for neutralization in P07-9.
 */
public final class EvmConversionUtils {
    public static final int NUM_LONG_ZEROS = 12;

    private EvmConversionUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static com.esaulpaugh.headlong.abi.Address headlongAddressOf(@NonNull final AccountID id) {
        requireNonNull(id);
        return asHeadlongAddress(
                id.hasAccountNum()
                        ? asEvmAddress(id.accountNumOrThrow())
                        : id.aliasOrThrow().toByteArray());
    }

    public static com.esaulpaugh.headlong.abi.Address headlongAddressOf(@NonNull final ContractID id) {
        requireNonNull(id);
        return asHeadlongAddress(
                id.hasContractNum()
                        ? asEvmAddress(id.contractNumOrThrow())
                        : id.evmAddressOrThrow().toByteArray());
    }

    public static com.esaulpaugh.headlong.abi.Address headlongAddressOf(@NonNull final ScheduleID id) {
        return asHeadlongAddress(asEvmAddress(requireNonNull(id).scheduleNum()));
    }

    public static com.esaulpaugh.headlong.abi.Address headlongAddressOf(
            @NonNull final com.hederahashgraph.api.proto.java.ScheduleID id) {
        return asHeadlongAddress(asEvmAddress(requireNonNull(id).getScheduleNum()));
    }

    public static com.esaulpaugh.headlong.abi.Address headlongAddressOf(@NonNull final TokenID id) {
        return asHeadlongAddress(asEvmAddress(requireNonNull(id).tokenNum()));
    }

    public static com.esaulpaugh.headlong.abi.Address headlongAddressOf(@NonNull final Account account) {
        return asHeadlongAddress(explicitAddressOf(requireNonNull(account)));
    }

    public static com.esaulpaugh.headlong.abi.Address asHeadlongAddress(@NonNull final byte[] explicit) {
        return com.esaulpaugh.headlong.abi.Address.wrap(
                toChecksumAddress(Bytes.wrap(requireNonNull(explicit)).toUnsignedBigInteger()));
    }

    public static byte[] asEvmAddress(final long num) {
        return copyToLeftPaddedByteArray(num, new byte[20]);
    }

    public static Address priorityAddressOf(@NonNull final Account account) {
        return Address.wrap(Bytes.wrap(explicitAddressOf(requireNonNull(account))));
    }

    public static byte[] copyToLeftPaddedByteArray(long value, final byte[] destination) {
        for (int i = 7, j = destination.length - 1; i >= 0; i--, j--) {
            destination[j] = (byte) (value & 0xffL);
            value >>= 8;
        }
        return destination;
    }

    public static byte[] explicitFromHeadlong(@NonNull final com.esaulpaugh.headlong.abi.Address address) {
        final byte[] raw = requireNonNull(address).value().toByteArray();
        final byte[] bytes20 = new byte[20];
        System.arraycopy(
                raw, Math.max(0, raw.length - 20), bytes20, Math.max(0, 20 - raw.length), Math.min(20, raw.length));
        return bytes20;
    }

    public static long numberOfLongZero(@NonNull final byte[] explicit) {
        requireNonNull(explicit);
        long value = 0;
        for (int i = 12; i < 20; i++) {
            value = (value << 8) | (explicit[i] & 0xffL);
        }
        return value;
    }

    public static byte[] explicitAddressOf(@NonNull final Account account) {
        final var evmAddress =
                AliasUtils.extractEvmAddress(requireNonNull(account).alias());
        return evmAddress != null
                ? evmAddress.toByteArray()
                : asEvmAddress(account.accountIdOrThrow().accountNumOrThrow());
    }

    public static com.hedera.pbj.runtime.io.buffer.Bytes bloomForAll(@NonNull final List<Log> logs) {
        return com.hedera.pbj.runtime.io.buffer.Bytes.wrap(LogsBloomFilter.builder()
                .insertLogs(requireNonNull(logs))
                .build()
                .toArray());
    }

    public static com.hedera.pbj.runtime.io.buffer.Bytes bloomFor(@NonNull final Log log) {
        return com.hedera.pbj.runtime.io.buffer.Bytes.wrap(
                LogsBloomFilter.builder().insertLog(requireNonNull(log)).build().toArray());
    }

    public static byte[] removeIfAnyLeading0x(final com.hedera.pbj.runtime.io.buffer.Bytes contents) {
        final byte[] prefix = {'0', 'x'};
        final long offset = requireNonNull(contents).matchesPrefix(prefix) ? prefix.length : 0;
        return contents.getBytes(offset, contents.length() - offset).toByteArray();
    }

    public static Log asBesuLog(
            @NonNull final EvmTransactionLog log,
            @NonNull final List<com.hedera.pbj.runtime.io.buffer.Bytes> paddedTopics) {
        requireNonNull(log);
        requireNonNull(paddedTopics);
        return new Log(
                Address.wrap(Bytes.wrap(asEvmAddress(log.contractIdOrThrow().contractNumOrThrow()))),
                Bytes.wrap(log.data().toByteArray()),
                paddedTopics.stream()
                        .map(bytes -> LogTopic.create(Bytes.wrap(bytes.toByteArray())))
                        .toList());
    }
}
