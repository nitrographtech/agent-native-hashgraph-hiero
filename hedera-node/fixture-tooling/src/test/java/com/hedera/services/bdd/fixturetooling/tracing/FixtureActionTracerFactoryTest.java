// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.fixturetooling.tracing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import com.hedera.node.app.service.contract.impl.exec.ActionSidecarContentTracerFactory;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;

class FixtureActionTracerFactoryTest {
    @Test
    void serviceLoaderDiscoversOnlyFixtureFactoryAndCreatesTransactionScopedTracers() {
        final var factories = ServiceLoader.load(ActionSidecarContentTracerFactory.class).stream()
                .toList();

        assertEquals(1, factories.size());
        final var provider = factories.getFirst();
        final var first = provider.get().create();
        final var second = provider.get().create();

        assertInstanceOf(FixtureActionTracerFactory.class, provider.get());
        assertInstanceOf(EvmActionTracer.class, first);
        assertInstanceOf(EvmActionTracer.class, second);
        assertNotSame(first, second);
    }
}
