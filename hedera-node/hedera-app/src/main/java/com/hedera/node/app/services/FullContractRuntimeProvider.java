// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.services;

import com.hedera.node.app.service.contract.ContractService;
import com.hedera.node.app.service.contract.impl.ContractServiceImpl;
import com.hedera.node.app.spi.AppContext;
import com.hedera.node.app.spi.fees.QueryFeeCalculator;
import com.hedera.node.app.spi.fees.ServiceFeeCalculator;
import com.swirlds.metrics.api.Metrics;
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
                new RemovedLegacyExecutableHandler(),
                h.hookStoreHandler(),
                h.hookDispatchHandler(),
                h.contractGetBySolidityIDHandler(),
                h.contractCallLocalHandler(),
                h.contractGetInfoHandler(),
                h.contractGetBytecodeHandler(),
                h.contractGetRecordsHandler());
    }

    /**
     * Creates the executable provider. This is the sole standard-node construction boundary for
     * {@link ContractServiceImpl}; native startup never invokes this method.
     */
    public static FullContractRuntimeProvider create(
            @NonNull final AppContext appContext, @NonNull final Metrics metrics) {
        return new FullContractRuntimeProvider(new ContractServiceImpl(appContext, metrics));
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

    private static final class RemovedLegacyExecutableHandler
            implements com.hedera.node.app.spi.workflows.TransactionHandler {
        private static com.hedera.node.app.spi.workflows.PreCheckException rejectedPreCheck() {
            return new com.hedera.node.app.spi.workflows.PreCheckException(
                    com.hedera.hapi.node.base.ResponseCodeEnum.INVALID_TRANSACTION_BODY);
        }

        @Override
        public void preHandle(com.hedera.node.app.spi.workflows.PreHandleContext context)
                throws com.hedera.node.app.spi.workflows.PreCheckException {
            throw rejectedPreCheck();
        }

        @Override
        public void pureChecks(com.hedera.node.app.spi.workflows.PureChecksContext context)
                throws com.hedera.node.app.spi.workflows.PreCheckException {
            throw rejectedPreCheck();
        }

        @Override
        public void warm(com.hedera.node.app.spi.workflows.WarmupContext context) {
            throw new UnsupportedOperationException("Ethereum transaction execution was removed");
        }

        @Override
        public void handle(com.hedera.node.app.spi.workflows.HandleContext context) {
            throw new com.hedera.node.app.spi.workflows.HandleException(
                    com.hedera.hapi.node.base.ResponseCodeEnum.INVALID_TRANSACTION_BODY);
        }
    }
}
