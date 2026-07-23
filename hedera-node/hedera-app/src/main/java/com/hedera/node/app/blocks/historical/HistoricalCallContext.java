// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.blocks.historical;

import static java.util.Objects.requireNonNull;

import com.hedera.pbj.runtime.io.buffer.Bytes;

/**
 * Immutable historical call metadata. The numeric values retain the unsigned
 * protobuf bit patterns used by the legacy wire format; this value performs no
 * execution or economic interpretation.
 */
public record HistoricalCallContext(long gas, long value, Bytes callData) {
    public HistoricalCallContext {
        requireNonNull(callData);
    }
}
