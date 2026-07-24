// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.services;

import com.hedera.node.app.spi.AppContext;
import com.swirlds.metrics.api.Metrics;
import edu.umd.cs.findbugs.annotations.NonNull;

/** Full-distribution factory for the executable legacy contract provider. */
public final class FullContractRuntimeProviderFactory implements ContractRuntimeProviderFactory {
    @Override
    public boolean executable() {
        return true;
    }

    @Override
    public @NonNull ContractRuntimeProvider create(
            @NonNull final AppContext appContext, @NonNull final Metrics metrics) {
        return FullContractRuntimeProvider.create(appContext, metrics);
    }
}
