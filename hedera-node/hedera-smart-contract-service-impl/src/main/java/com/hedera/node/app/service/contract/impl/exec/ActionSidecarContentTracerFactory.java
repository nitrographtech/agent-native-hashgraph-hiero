// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.service.contract.impl.exec;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Tooling-only extension point for supplying a live action collector.
 *
 * <p>The normal full runtime has no provider and uses {@code NoTracer}. Authenticated fixture
 * generation adds exactly one provider on its augmented tooling classpath.
 */
public interface ActionSidecarContentTracerFactory {
    /** Returns a fresh transaction-scoped collector. */
    @NonNull
    ActionSidecarContentTracer create();
}
