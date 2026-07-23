// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.services;

import com.hedera.node.app.service.contract.ContractService;
import com.hedera.node.app.service.contract.history.HistoricalContractStateService;
import com.hedera.node.app.spi.fees.QueryFeeCalculator;
import com.hedera.node.app.spi.fees.ServiceFeeCalculator;
import com.hedera.node.app.spi.workflows.HandleContext;
import com.hedera.node.app.spi.workflows.PreHandleContext;
import com.hedera.node.app.spi.workflows.PureChecksContext;
import com.hedera.node.app.spi.workflows.WarmupContext;
import java.util.Set;

/** Read-only historical-state provider used by the native-agent distribution. */
public final class HistoricalContractRuntimeProvider implements ContractRuntimeProvider {
    private static final UnsupportedLegacyHandler UNSUPPORTED = new UnsupportedLegacyHandler();
    private final ContractService service = new HistoricalContractStateService();
    private final ContractRuntimeHandlers handlers = new ContractRuntimeHandlers(
            UNSUPPORTED,
            UNSUPPORTED,
            UNSUPPORTED,
            UNSUPPORTED,
            UNSUPPORTED,
            UNSUPPORTED,
            UNSUPPORTED,
            UNSUPPORTED,
            UNSUPPORTED,
            UNSUPPORTED,
            UNSUPPORTED,
            UNSUPPORTED,
            UNSUPPORTED,
            UNSUPPORTED);

    @Override
    public ContractService stateService() {
        return service;
    }

    @Override
    public ContractRuntimeHandlers handlers() {
        return handlers;
    }

    @Override
    public Set<ServiceFeeCalculator> serviceFeeCalculators() {
        return Set.of();
    }

    @Override
    public Set<QueryFeeCalculator> queryFeeCalculators() {
        return Set.of();
    }

    @Override
    public void initializeMetrics() {}

    @Override
    public void verifyNativeLibraries() {}

    @Override
    public boolean executable() {
        return false;
    }

    private static final class UnsupportedLegacyHandler
            implements EthereumTransactionHandlerFacade, com.hedera.node.app.spi.workflows.QueryHandler {
        private static UnsupportedOperationException rejected() {
            return new UnsupportedOperationException("Legacy contract execution is disabled");
        }

        @Override
        public void preHandle(PreHandleContext context) {
            throw rejected();
        }

        @Override
        public void pureChecks(PureChecksContext context) {
            throw rejected();
        }

        @Override
        public void warm(WarmupContext context) {
            throw rejected();
        }

        @Override
        public void handle(HandleContext context) {
            throw rejected();
        }

        @Override
        public com.hedera.node.app.hapi.utils.ethereum.EthTxSigs maybeEthTxSigsFor(
                com.hedera.hapi.node.contract.EthereumTransactionBody op,
                com.hedera.node.app.service.file.ReadableFileStore fileStore,
                com.swirlds.config.api.Configuration configuration) {
            throw rejected();
        }

        @Override
        public void handleThrottled(HandleContext context) {
            throw rejected();
        }

        @Override
        public com.hedera.hapi.node.base.QueryHeader extractHeader(com.hedera.hapi.node.transaction.Query query) {
            throw rejected();
        }

        @Override
        public com.hedera.hapi.node.transaction.Response createEmptyResponse(
                com.hedera.hapi.node.base.ResponseHeader header) {
            throw rejected();
        }

        @Override
        public boolean requiresNodePayment(com.hedera.hapi.node.base.ResponseType responseType) {
            throw rejected();
        }

        @Override
        public boolean needsAnswerOnlyCost(com.hedera.hapi.node.base.ResponseType responseType) {
            throw rejected();
        }

        @Override
        public void validate(com.hedera.node.app.spi.workflows.QueryContext context) {
            throw rejected();
        }

        @Override
        public com.hedera.hapi.node.transaction.Response findResponse(
                com.hedera.node.app.spi.workflows.QueryContext context,
                com.hedera.hapi.node.base.ResponseHeader header) {
            throw rejected();
        }
    }
}
