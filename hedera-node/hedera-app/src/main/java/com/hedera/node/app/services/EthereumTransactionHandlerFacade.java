// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.services;

import com.hedera.hapi.node.contract.EthereumTransactionBody;
import com.hedera.node.app.hapi.utils.ethereum.EthTxSigs;
import com.hedera.node.app.service.file.ReadableFileStore;
import com.hedera.node.app.spi.workflows.HandleContext;
import com.hedera.node.app.spi.workflows.TransactionHandler;
import com.swirlds.config.api.Configuration;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/** Implementation-neutral operations needed by shared workflows for an Ethereum transaction. */
public interface EthereumTransactionHandlerFacade extends TransactionHandler {
    @Nullable
    EthTxSigs maybeEthTxSigsFor(
            @NonNull EthereumTransactionBody op,
            @NonNull ReadableFileStore fileStore,
            @NonNull Configuration configuration);

    void handleThrottled(@NonNull HandleContext context);
}
