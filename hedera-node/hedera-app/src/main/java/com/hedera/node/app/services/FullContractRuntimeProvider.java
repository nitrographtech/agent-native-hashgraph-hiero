// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.services;

import com.hedera.node.app.service.contract.ContractService;
import com.hedera.node.app.service.contract.impl.ContractServiceImpl;
import com.hedera.node.app.service.contract.impl.handlers.EthereumTransactionHandler;
import com.hedera.node.app.spi.fees.QueryFeeCalculator;
import com.hedera.node.app.spi.fees.ServiceFeeCalculator;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Set;

/** Adapter preserving the full distribution's executable contract behavior. */
public final class FullContractRuntimeProvider implements ContractRuntimeProvider {
    private final ContractServiceImpl service;
    private final ContractRuntimeHandlers handlers;

    public FullContractRuntimeProvider(@NonNull final ContractServiceImpl service) {
        this.service = service;
        final var h = service.handlers();
        this.handlers = new ContractRuntimeHandlers(
                h.contractCreateHandler(),
                h.contractUpdateHandler(),
                h.contractCallHandler(),
                h.contractDeleteHandler(),
                h.contractSystemDeleteHandler(),
                h.contractSystemUndeleteHandler(),
                new ExecutableEthereumHandler(h.ethereumTransactionHandler()),
                h.hookStoreHandler(),
                h.hookDispatchHandler(),
                h.contractGetBySolidityIDHandler(),
                h.contractCallLocalHandler(),
                h.contractGetInfoHandler(),
                h.contractGetBytecodeHandler(),
                h.contractGetRecordsHandler());
    }

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
        return service.serviceFeeCalculators();
    }

    @Override
    public Set<QueryFeeCalculator> queryFeeCalculators() {
        return service.queryFeeCalculators();
    }

    @Override
    public void initializeMetrics() {
        service.createMetrics();
    }

    @Override
    public void verifyNativeLibraries() {
        service.nativeLibVerifier().verifyNativeLibs();
    }

    @Override
    public boolean executable() {
        return true;
    }

    private record ExecutableEthereumHandler(EthereumTransactionHandler delegate)
            implements EthereumTransactionHandlerFacade {
        @Override
        public void preHandle(com.hedera.node.app.spi.workflows.PreHandleContext context)
                throws com.hedera.node.app.spi.workflows.PreCheckException {
            delegate.preHandle(context);
        }

        @Override
        public void pureChecks(com.hedera.node.app.spi.workflows.PureChecksContext context)
                throws com.hedera.node.app.spi.workflows.PreCheckException {
            delegate.pureChecks(context);
        }

        @Override
        public void warm(com.hedera.node.app.spi.workflows.WarmupContext context) {
            delegate.warm(context);
        }

        @Override
        public com.hedera.node.app.spi.fees.Fees calculateFees(com.hedera.node.app.spi.fees.FeeContext context) {
            return delegate.calculateFees(context);
        }

        @Override
        public void handle(com.hedera.node.app.spi.workflows.HandleContext context) {
            delegate.handle(context);
        }

        @Override
        public com.hedera.node.app.hapi.utils.ethereum.EthTxSigs maybeEthTxSigsFor(
                com.hedera.hapi.node.contract.EthereumTransactionBody op,
                com.hedera.node.app.service.file.ReadableFileStore fileStore,
                com.swirlds.config.api.Configuration configuration) {
            return delegate.maybeEthTxSigsFor(op, fileStore, configuration);
        }

        @Override
        public void handleThrottled(com.hedera.node.app.spi.workflows.HandleContext context) {
            delegate.handleThrottled(context);
        }
    }
}
