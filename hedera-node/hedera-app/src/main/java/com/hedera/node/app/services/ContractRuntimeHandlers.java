// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.services;

import com.hedera.node.app.spi.workflows.QueryHandler;
import com.hedera.node.app.spi.workflows.TransactionHandler;
import edu.umd.cs.findbugs.annotations.NonNull;

/** Contract-facing handlers exposed to shared workflows without executable implementation types. */
public record ContractRuntimeHandlers(
        @NonNull TransactionHandler contractCreateHandler,
        @NonNull TransactionHandler contractUpdateHandler,
        @NonNull TransactionHandler contractCallHandler,
        @NonNull TransactionHandler contractDeleteHandler,
        @NonNull TransactionHandler contractSystemDeleteHandler,
        @NonNull TransactionHandler contractSystemUndeleteHandler,
        @NonNull TransactionHandler ethereumTransactionHandler,
        @NonNull TransactionHandler hookStoreHandler,
        @NonNull TransactionHandler hookDispatchHandler,
        @NonNull QueryHandler contractGetBySolidityIdHandler,
        @NonNull QueryHandler contractCallLocalHandler,
        @NonNull QueryHandler contractGetInfoHandler,
        @NonNull QueryHandler contractGetBytecodeHandler,
        @NonNull QueryHandler contractGetRecordsHandler) {}
