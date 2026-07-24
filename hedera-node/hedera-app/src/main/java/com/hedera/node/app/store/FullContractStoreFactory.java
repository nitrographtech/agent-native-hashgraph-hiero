// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.store;

import com.hedera.node.app.service.contract.ReadableEvmHookStore;
import com.hedera.node.app.service.contract.impl.state.ContractStateStore;
import com.hedera.node.app.service.contract.impl.state.ReadableContractStateStore;
import com.hedera.node.app.service.contract.impl.state.ReadableEvmHookStoreImpl;
import com.hedera.node.app.service.contract.impl.state.WritableContractStateStore;
import com.hedera.node.app.service.contract.impl.state.WritableEvmHookStore;
import com.hedera.node.app.service.entityid.ReadableEntityIdStore;
import com.hedera.node.app.service.entityid.WritableEntityIdStore;
import com.swirlds.state.spi.ReadableStates;
import com.swirlds.state.spi.WritableStates;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

/** Full profile factory retaining executable contract store implementations. */
public final class FullContractStoreFactory implements ContractStoreFactory {
    @Override
    public int priority() {
        return 100;
    }

    @Override
    public Map<Class<?>, BiFunction<ReadableStates, ReadableEntityIdStore, ?>> readableStores() {
        final Map<Class<?>, BiFunction<ReadableStates, ReadableEntityIdStore, ?>> stores = new LinkedHashMap<>();
        stores.put(
                com.hedera.node.app.service.contract.ReadableContractStateStore.class, ReadableContractStateStore::new);
        stores.put(ContractStateStore.class, ReadableContractStateStore::new);
        stores.put(ReadableEvmHookStore.class, (states, counters) -> new ReadableEvmHookStoreImpl(states));
        return Map.copyOf(stores);
    }

    @Override
    public Map<Class<?>, BiFunction<WritableStates, WritableEntityIdStore, ?>> writableStores() {
        return Map.of(
                WritableContractStateStore.class,
                WritableContractStateStore::new,
                WritableEvmHookStore.class,
                WritableEvmHookStore::new);
    }
}
