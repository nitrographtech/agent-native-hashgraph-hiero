// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.fixturetooling.tracing;

import com.hedera.node.app.service.contract.impl.exec.ActionSidecarContentTracer;
import com.hedera.node.app.service.contract.impl.exec.ActionSidecarContentTracerFactory;
import edu.umd.cs.findbugs.annotations.NonNull;

/** Creates a fresh live action collector for each fixture transaction. */
public final class FixtureActionTracerFactory implements ActionSidecarContentTracerFactory {
    @Override
    public @NonNull ActionSidecarContentTracer create() {
        return new EvmActionTracer(new ActionStack());
    }
}
