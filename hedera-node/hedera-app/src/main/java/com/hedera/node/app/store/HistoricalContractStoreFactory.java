// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.store;

import com.hedera.node.app.service.contract.ReadableContractStateStore;
import com.hedera.node.app.service.contract.ReadableEvmHookStore;
import com.hedera.node.app.service.contract.history.HistoricalReadableContractStateStore;
import com.hedera.node.app.service.contract.history.HistoricalReadableEvmHookStore;
import com.hedera.node.app.service.entityid.ReadableEntityIdStore;
import com.hedera.node.app.service.entityid.WritableEntityIdStore;
import com.swirlds.state.spi.ReadableStates;
import com.swirlds.state.spi.WritableStates;
import java.util.Map;
import java.util.function.BiFunction;

/** Native profile factory exposing retained contract state through read-only adapters only. */
public final class HistoricalContractStoreFactory implements ContractStoreFactory {
    @Override
    public Map<Class<?>, BiFunction<ReadableStates, ReadableEntityIdStore, ?>> readableStores() {
        return Map.of(
                ReadableContractStateStore.class,
                (states, counters) -> new HistoricalReadableContractStateStore(states),
                ReadableEvmHookStore.class,
                (states, counters) -> new HistoricalReadableEvmHookStore(states));
    }

    @Override
    public Map<Class<?>, BiFunction<WritableStates, WritableEntityIdStore, ?>> writableStores() {
        return Map.of();
    }
}
