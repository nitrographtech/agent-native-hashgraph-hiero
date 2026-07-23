// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.blocks.historical;

import static java.util.Objects.requireNonNull;

import com.hedera.pbj.runtime.io.buffer.Bytes;

/** Immutable opaque data carried by a historical EVM log. */
public record HistoricalLogData(Bytes bytes) {
    public HistoricalLogData {
        requireNonNull(bytes);
    }
}
