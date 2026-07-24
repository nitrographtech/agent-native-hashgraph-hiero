// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.store;

import com.hedera.node.app.service.entityid.ReadableEntityIdStore;
import com.hedera.node.app.service.entityid.WritableEntityIdStore;
import com.swirlds.state.spi.ReadableStates;
import com.swirlds.state.spi.WritableStates;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Map;
import java.util.function.BiFunction;

/** Profile-specific construction boundary for contract stores. */
public interface ContractStoreFactory {
    /** Higher priority wins when a full artifact includes both full and historical factories. */
    default int priority() {
        return 0;
    }

    @NonNull
    Map<Class<?>, BiFunction<ReadableStates, ReadableEntityIdStore, ?>> readableStores();

    @NonNull
    Map<Class<?>, BiFunction<WritableStates, WritableEntityIdStore, ?>> writableStores();
}
