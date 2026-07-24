// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.service.contract.history;

import static java.util.Objects.requireNonNull;

import com.hedera.hapi.node.base.ContractID;
import com.hedera.hapi.node.state.contract.Bytecode;
import com.hedera.hapi.node.state.contract.SlotKey;
import com.hedera.hapi.node.state.contract.SlotValue;
import com.hedera.hapi.node.state.hooks.EvmHookSlotKey;
import com.hedera.node.app.service.contract.ReadableContractStateStore;
import com.swirlds.state.spi.ReadableKVState;
import com.swirlds.state.spi.ReadableStates;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/** Read-only adapter over the retained V0.49 and V0.65 contract maps. */
public final class HistoricalReadableContractStateStore implements ReadableContractStateStore {
    private final ReadableKVState<SlotKey, SlotValue> storage;
    private final ReadableKVState<ContractID, Bytecode> bytecode;
    private final ReadableKVState<EvmHookSlotKey, SlotValue> hookStorage;

    public HistoricalReadableContractStateStore(@NonNull final ReadableStates states) {
        requireNonNull(states);
        storage = states.get(V0490ContractSchema.STORAGE_STATE_ID);
        bytecode = states.get(V0490ContractSchema.BYTECODE_STATE_ID);
        hookStorage = states.get(V065ContractSchema.EVM_HOOK_STORAGE_STATE_ID);
    }

    @Override
    public @Nullable Bytecode getBytecode(@NonNull final ContractID contractId) {
        return bytecode.get(requireNonNull(contractId));
    }

    @Override
    public @Nullable SlotValue getSlotValue(@NonNull final SlotKey key) {
        return storage.get(requireNonNull(key));
    }

    @Override
    public long getNumSlots() {
        return storage.size() + hookStorage.size();
    }

    @Override
    public long getNumBytecodes() {
        return bytecode.size();
    }
}
