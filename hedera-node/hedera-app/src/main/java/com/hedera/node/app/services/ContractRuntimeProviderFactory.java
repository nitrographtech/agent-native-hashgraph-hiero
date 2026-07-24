// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.services;

import com.hedera.node.app.spi.AppContext;
import com.swirlds.metrics.api.Metrics;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ServiceLoader;

/** Selects a profile-specific contract runtime without linking common startup to its implementation. */
public interface ContractRuntimeProviderFactory {
    /**
     * Returns whether this factory creates an executable legacy runtime.
     */
    boolean executable();

    /**
     * Creates the profile-specific runtime provider.
     */
    @NonNull
    ContractRuntimeProvider create(@NonNull AppContext appContext, @NonNull Metrics metrics);

    /**
     * Loads the single factory matching the requested runtime profile.
     */
    static @NonNull ContractRuntimeProvider createFor(
            final boolean executable, @NonNull final AppContext appContext, @NonNull final Metrics metrics) {
        final var matches = ServiceLoader.load(ContractRuntimeProviderFactory.class).stream()
                .map(ServiceLoader.Provider::get)
                .filter(factory -> factory.executable() == executable)
                .toList();
        if (matches.size() != 1) {
            throw new IllegalStateException("Expected exactly one contract runtime provider factory for executable="
                    + executable + ", found " + matches.size());
        }
        return matches.getFirst().create(appContext, metrics);
    }
}
