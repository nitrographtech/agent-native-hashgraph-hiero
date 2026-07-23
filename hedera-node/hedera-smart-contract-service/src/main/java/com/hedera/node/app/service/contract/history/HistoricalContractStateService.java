// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.service.contract.history;

import com.hedera.node.app.service.contract.ContractService;
import com.swirlds.state.lifecycle.SchemaRegistry;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Registers retained contract state without initializing or exposing an executable contract runtime.
 */
public final class HistoricalContractStateService implements ContractService {
    @Override
    public void registerSchemas(@NonNull final SchemaRegistry registry) {
        registry.registerAll(new V0490ContractSchema(), new V065ContractSchema());
    }
}
