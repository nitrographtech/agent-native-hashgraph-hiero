// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.blocks.historical;

import static com.hedera.node.app.hapi.utils.MiscCryptoUtils.keccak256DigestOf;
import static java.util.Objects.requireNonNull;

import com.hedera.pbj.runtime.io.buffer.Bytes;
import java.util.List;

/** Computes the canonical Ethereum log bloom without an EVM runtime data type. */
public final class HistoricalLogBloom {
    private static final int BLOOM_BYTES = 256;
    private static final int BLOOM_BITS = BLOOM_BYTES * Byte.SIZE;

    private HistoricalLogBloom() {}

    public static Bytes forLog(final HistoricalLog log) {
        requireNonNull(log);
        final byte[] bloom = new byte[BLOOM_BYTES];
        insert(bloom, log.address().bytes());
        log.topics().forEach(topic -> insert(bloom, topic.bytes()));
        return Bytes.wrap(bloom);
    }

    public static Bytes forAll(final List<HistoricalLog> logs) {
        requireNonNull(logs);
        final byte[] bloom = new byte[BLOOM_BYTES];
        logs.forEach(log -> {
            final byte[] logBloom = forLog(log).toByteArray();
            for (int i = 0; i < BLOOM_BYTES; i++) {
                bloom[i] |= logBloom[i];
            }
        });
        return Bytes.wrap(bloom);
    }

    private static void insert(final byte[] bloom, final Bytes value) {
        final byte[] hash = keccak256DigestOf(value.toByteArray());
        for (int i = 0; i < 6; i += 2) {
            final int bit = ((hash[i] & 0xff) << Byte.SIZE | (hash[i + 1] & 0xff)) & (BLOOM_BITS - 1);
            bloom[BLOOM_BYTES - 1 - bit / Byte.SIZE] |= (byte) (1 << (bit % Byte.SIZE));
        }
    }
}
