// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.services;

import com.hedera.node.app.spi.AppContext;
import com.swirlds.metrics.api.Metrics;
import edu.umd.cs.findbugs.annotations.NonNull;

/** Native-only factory for the read-only historical compatibility provider. */
public final class HistoricalContractRuntimeProviderFactory implements ContractRuntimeProviderFactory {
    @Override
    public boolean executable() {
        return false;
    }

    @Override
    public @NonNull ContractRuntimeProvider create(
            @NonNull final AppContext appContext, @NonNull final Metrics metrics) {
        return new HistoricalContractRuntimeProvider();
    }
}
