// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.hedera.hapi.node.contract.ContractCallTransactionBody;
import com.hedera.hapi.node.contract.ContractCreateTransactionBody;
import com.hedera.hapi.node.contract.EthereumTransactionBody;
import com.hedera.hapi.node.token.CryptoTransferTransactionBody;
import com.hedera.hapi.node.transaction.TransactionBody;
import com.hedera.node.app.fees.FeeManager;
import com.hedera.node.app.spi.workflows.PureChecksContext;
import com.hedera.node.app.workflows.dispatcher.TransactionDispatcher;
import com.hedera.node.app.workflows.dispatcher.TransactionHandlers;
import com.hedera.node.config.ConfigProvider;
import com.hedera.node.config.VersionedConfiguration;
import com.hedera.node.config.data.ContractsConfig;
import org.junit.jupiter.api.Test;

class ServiceCompositionTest {
    private final ServiceComposition full = new ServiceComposition(true);
    private final ServiceComposition nativeAgent = new ServiceComposition(false);

    @Test
    void fullDistributionPermitsContractAndEthereumOperations() {
        assertThat(full.permits(contractCreate())).isTrue();
        assertThat(full.permits(contractCall())).isTrue();
        assertThat(full.permits(ethereumTransaction())).isTrue();
    }

    @Test
    void nativeAgentDistributionRejectsContractAndEthereumOperations() {
        assertThat(nativeAgent.permits(contractCreate())).isFalse();
        assertThat(nativeAgent.permits(contractCall())).isFalse();
        assertThat(nativeAgent.permits(ethereumTransaction())).isFalse();
    }

    @Test
    void nativeAgentDistributionPreservesNativeOperations() {
        final var transfer = TransactionBody.newBuilder()
                .cryptoTransfer(CryptoTransferTransactionBody.DEFAULT)
                .build();
        assertThat(nativeAgent.permits(transfer)).isTrue();
    }

    @Test
    void nativeAgentDispatcherDeterministicallyRejectsRemovedOperationsAtPrecheck() {
        final var configProvider = mock(ConfigProvider.class);
        final var configuration = mock(VersionedConfiguration.class);
        final var contractsConfig = mock(ContractsConfig.class);
        final var context = mock(PureChecksContext.class);
        given(configProvider.getConfiguration()).willReturn(configuration);
        given(configuration.getConfigData(ContractsConfig.class)).willReturn(contractsConfig);
        given(contractsConfig.enabled()).willReturn(false);
        final var dispatcher =
                new TransactionDispatcher(mock(TransactionHandlers.class), mock(FeeManager.class), configProvider);

        for (final var removedOperation :
                new TransactionBody[] {contractCreate(), contractCall(), ethereumTransaction()}) {
            given(context.body()).willReturn(removedOperation);
            assertThatThrownBy(() -> dispatcher.dispatchPureChecks(context)).hasMessage("INVALID_TRANSACTION_BODY");
        }
    }

    private static TransactionBody contractCreate() {
        return TransactionBody.newBuilder()
                .contractCreateInstance(ContractCreateTransactionBody.DEFAULT)
                .build();
    }

    private static TransactionBody contractCall() {
        return TransactionBody.newBuilder()
                .contractCall(ContractCallTransactionBody.DEFAULT)
                .build();
    }

    private static TransactionBody ethereumTransaction() {
        return TransactionBody.newBuilder()
                .ethereumTransaction(EthereumTransactionBody.DEFAULT)
                .build();
    }
}
