// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.service.contract.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.hedera.hapi.node.base.ContractID;
import com.hedera.hapi.node.base.HookEntityId;
import com.hedera.hapi.node.base.HookId;
import com.hedera.hapi.node.state.contract.Bytecode;
import com.hedera.hapi.node.state.contract.SlotKey;
import com.hedera.hapi.node.state.contract.SlotValue;
import com.hedera.hapi.node.state.hooks.EvmHookSlotKey;
import com.hedera.hapi.node.state.hooks.EvmHookState;
import com.hedera.pbj.runtime.io.buffer.Bytes;
import com.swirlds.state.spi.ReadableKVState;
import com.swirlds.state.spi.ReadableStates;
import org.junit.jupiter.api.Test;

class HistoricalReadableContractStoresTest {
    private static final ContractID CONTRACT_ID =
            ContractID.newBuilder().contractNum(123L).build();
    private static final HookId HOOK_ID = HookId.newBuilder()
            .entityId(HookEntityId.newBuilder().contractId(CONTRACT_ID))
            .hookId(1)
            .build();
    private static final SlotKey SLOT_KEY = new SlotKey(CONTRACT_ID, Bytes.wrap(new byte[] {1}));
    private static final EvmHookSlotKey HOOK_SLOT_KEY = new EvmHookSlotKey(HOOK_ID, Bytes.wrap(new byte[] {2}));
    private static final SlotValue SLOT_VALUE = new SlotValue(Bytes.wrap(new byte[] {3}), Bytes.EMPTY, Bytes.EMPTY);

    @Test
    void exposesRetainedContractMapsWithoutExecutableStores() {
        final var states = mock(ReadableStates.class);
        final ReadableKVState<SlotKey, SlotValue> storage = mock(ReadableKVState.class);
        final ReadableKVState<ContractID, Bytecode> bytecode = mock(ReadableKVState.class);
        final ReadableKVState<EvmHookSlotKey, SlotValue> hookStorage = mock(ReadableKVState.class);
        final var code = new Bytecode(Bytes.wrap(new byte[] {4}));
        given(states.<SlotKey, SlotValue>get(V0490ContractSchema.STORAGE_STATE_ID))
                .willReturn(storage);
        given(states.<ContractID, Bytecode>get(V0490ContractSchema.BYTECODE_STATE_ID))
                .willReturn(bytecode);
        given(states.<EvmHookSlotKey, SlotValue>get(V065ContractSchema.EVM_HOOK_STORAGE_STATE_ID))
                .willReturn(hookStorage);
        given(storage.get(SLOT_KEY)).willReturn(SLOT_VALUE);
        given(bytecode.get(CONTRACT_ID)).willReturn(code);
        given(storage.size()).willReturn(1L);
        given(hookStorage.size()).willReturn(2L);
        given(bytecode.size()).willReturn(3L);

        final var subject = new HistoricalReadableContractStateStore(states);

        assertThat(subject.getSlotValue(SLOT_KEY)).isSameAs(SLOT_VALUE);
        assertThat(subject.getBytecode(CONTRACT_ID)).isSameAs(code);
        assertThat(subject.getNumSlots()).isEqualTo(3L);
        assertThat(subject.getNumBytecodes()).isEqualTo(3L);
    }

    @Test
    void exposesRetainedHookMapsWithoutExecutableStores() {
        final var states = mock(ReadableStates.class);
        final ReadableKVState<HookId, EvmHookState> hookStates = mock(ReadableKVState.class);
        final ReadableKVState<EvmHookSlotKey, SlotValue> hookStorage = mock(ReadableKVState.class);
        final var hookState = EvmHookState.DEFAULT;
        given(states.<HookId, EvmHookState>get(V065ContractSchema.EVM_HOOK_STATES_STATE_ID))
                .willReturn(hookStates);
        given(states.<EvmHookSlotKey, SlotValue>get(V065ContractSchema.EVM_HOOK_STORAGE_STATE_ID))
                .willReturn(hookStorage);
        given(hookStates.get(HOOK_ID)).willReturn(hookState);
        given(hookStorage.get(HOOK_SLOT_KEY)).willReturn(SLOT_VALUE);

        final var subject = new HistoricalReadableEvmHookStore(states);

        assertThat(subject.getEvmHook(HOOK_ID)).isSameAs(hookState);
        assertThat(subject.getSlotValue(HOOK_SLOT_KEY)).isSameAs(SLOT_VALUE);
    }
}
