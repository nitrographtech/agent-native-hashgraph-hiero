// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.services;

import com.hedera.hapi.node.transaction.TransactionBody;
import com.hedera.node.config.data.ContractsConfig;
import com.swirlds.config.api.Configuration;
import edu.umd.cs.findbugs.annotations.NonNull;

/** Defines the explicit service composition selected for a node distribution. */
public record ServiceComposition(boolean contractServiceEnabled) {
    /** Returns the composition selected by the node configuration. */
    public static ServiceComposition from(@NonNull final Configuration configuration) {
        return new ServiceComposition(configuration.getConfigData(ContractsConfig.class).enabled());
    }

    /** Returns whether the transaction can be dispatched by this composition. */
    public boolean permits(@NonNull final TransactionBody body) {
        if (contractServiceEnabled) {
            return true;
        }
        return switch (body.data().kind()) {
            case CONTRACT_CREATE_INSTANCE,
                    CONTRACT_UPDATE_INSTANCE,
                    CONTRACT_CALL,
                    CONTRACT_DELETE_INSTANCE,
                    ETHEREUM_TRANSACTION,
                    HOOK_STORE,
                    HOOK_DISPATCH -> false;
            case SYSTEM_DELETE -> body.systemDeleteOrThrow().id().kind()
                    != com.hedera.hapi.node.file.SystemDeleteTransactionBody.IdOneOfType.CONTRACT_ID;
            case SYSTEM_UNDELETE -> body.systemUndeleteOrThrow().id().kind()
                    != com.hedera.hapi.node.file.SystemUndeleteTransactionBody.IdOneOfType.CONTRACT_ID;
            default -> true;
        };
    }
}
