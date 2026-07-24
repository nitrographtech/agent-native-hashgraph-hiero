// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.service.contract.history;

import static java.util.Objects.requireNonNull;

import com.hedera.hapi.node.base.HookId;
import com.hedera.hapi.node.state.contract.SlotValue;
import com.hedera.hapi.node.state.hooks.EvmHookSlotKey;
import com.hedera.hapi.node.state.hooks.EvmHookState;
import com.hedera.node.app.service.contract.ReadableEvmHookStore;
import com.swirlds.state.spi.ReadableKVState;
import com.swirlds.state.spi.ReadableStates;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/** Read-only adapter over retained historical EVM hook metadata. */
public final class HistoricalReadableEvmHookStore implements ReadableEvmHookStore {
    private final ReadableKVState<HookId, EvmHookState> hookStates;
    private final ReadableKVState<EvmHookSlotKey, SlotValue> hookStorage;

    public HistoricalReadableEvmHookStore(@NonNull final ReadableStates states) {
        hookStates = requireNonNull(states).get(V065ContractSchema.EVM_HOOK_STATES_STATE_ID);
        hookStorage = states.get(V065ContractSchema.EVM_HOOK_STORAGE_STATE_ID);
    }

    @Override
    public @Nullable EvmHookState getEvmHook(@NonNull final HookId hookId) {
        return hookStates.get(requireNonNull(hookId));
    }

    @Override
    public @Nullable SlotValue getSlotValue(@NonNull final EvmHookSlotKey key) {
        return hookStorage.get(requireNonNull(key));
    }
}
