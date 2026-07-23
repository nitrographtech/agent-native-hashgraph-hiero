// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.blocks.historical;

import static java.util.Objects.requireNonNull;

import com.hedera.pbj.runtime.io.buffer.Bytes;

/** An immutable 32-byte topic in a historical EVM log. */
public record HistoricalLogTopic(Bytes bytes) {
    public static final int LENGTH = 32;

    public HistoricalLogTopic {
        requireNonNull(bytes);
        if (bytes.length() != LENGTH) {
            throw new IllegalArgumentException("Historical log topic must be 32 bytes");
        }
    }
}
