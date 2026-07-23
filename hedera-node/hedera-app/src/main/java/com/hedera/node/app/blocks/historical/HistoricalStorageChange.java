// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.blocks.historical;

import static java.util.Objects.requireNonNull;

import com.hedera.hapi.streams.StorageChange;
import com.hedera.pbj.runtime.io.buffer.Bytes;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * An immutable, non-executable snapshot of a historical contract storage change.
 *
 * <p>Slot and value bytes retain their minimal, big-endian wire representation.
 * A {@code null} written value means the slot was only read; a present empty
 * value means zero was explicitly written.
 */
public record HistoricalStorageChange(
        Bytes slot, Bytes valueRead, @Nullable Bytes valueWritten) {
    private static final int MAX_EVM_WORD_BYTES = 32;

    public HistoricalStorageChange {
        requireNonNull(slot);
        requireNonNull(valueRead);
        requireEvmWord("slot", slot);
        requireEvmWord("valueRead", valueRead);
        if (valueWritten != null) {
            requireEvmWord("valueWritten", valueWritten);
        }
    }

    /** Creates a neutral snapshot without changing PBJ field presence. */
    public static HistoricalStorageChange fromPbj(final StorageChange change) {
        requireNonNull(change);
        return new HistoricalStorageChange(change.slot(), change.valueRead(), change.valueWritten());
    }

    /** Reconstructs the identical PBJ wire value at the stream boundary. */
    public StorageChange toPbj() {
        return new StorageChange(slot, valueRead, valueWritten);
    }

    private static void requireEvmWord(final String field, final Bytes value) {
        if (value.length() > MAX_EVM_WORD_BYTES) {
            throw new IllegalArgumentException(field + " must contain at most 32 bytes");
        }
    }
}
