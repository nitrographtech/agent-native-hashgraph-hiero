// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.blocks.historical;

import static java.util.Objects.requireNonNull;

import com.hedera.pbj.runtime.io.buffer.Bytes;

/** An immutable 20-byte Ethereum address used only for historical data translation. */
public record HistoricalEthereumAddress(Bytes bytes) {
    public static final int LENGTH = 20;

    public HistoricalEthereumAddress {
        requireNonNull(bytes);
        if (bytes.length() != LENGTH) {
            throw new IllegalArgumentException("Historical Ethereum address must be 20 bytes");
        }
    }

    /** Returns the canonical long-zero address for the supplied Hedera entity number. */
    public static HistoricalEthereumAddress fromEntityNumber(final long entityNumber) {
        final byte[] address = new byte[LENGTH];
        for (int i = 0; i < Long.BYTES; i++) {
            address[LENGTH - 1 - i] = (byte) (entityNumber >>> (Byte.SIZE * i));
        }
        return new HistoricalEthereumAddress(Bytes.wrap(address));
    }
}
