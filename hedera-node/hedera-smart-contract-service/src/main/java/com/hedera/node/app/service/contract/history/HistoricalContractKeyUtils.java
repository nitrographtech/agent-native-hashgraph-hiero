// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.service.contract.history;

import com.hedera.hapi.node.base.HookId;
import com.hedera.hapi.node.state.hooks.EvmHookSlotKey;
import com.hedera.pbj.runtime.io.buffer.Bytes;
import edu.umd.cs.findbugs.annotations.NonNull;

/** Byte-preserving helpers required to interpret retained historical hook storage keys. */
public final class HistoricalContractKeyUtils {
    public static final Bytes ZERO_KEY = Bytes.fromHex("00");

    private HistoricalContractKeyUtils() {}

    public static @NonNull EvmHookSlotKey minimalKey(@NonNull final HookId hookId, @NonNull final Bytes key) {
        return new EvmHookSlotKey(hookId, minimalKey(key));
    }

    public static @NonNull Bytes minimalKey(@NonNull final Bytes key) {
        final var length = key.length();
        if (length == 0) {
            return ZERO_KEY;
        }
        int firstNonZero = 0;
        while (firstNonZero < length && key.getByte(firstNonZero) == 0) {
            firstNonZero++;
        }
        return firstNonZero == length ? ZERO_KEY : key.slice(firstNonZero, length - firstNonZero);
    }
}
