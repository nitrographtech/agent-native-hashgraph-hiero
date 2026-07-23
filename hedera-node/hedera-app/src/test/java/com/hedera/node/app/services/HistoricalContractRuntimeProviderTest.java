// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hedera.node.app.service.contract.history.HistoricalContractStateService;
import org.junit.jupiter.api.Test;

class HistoricalContractRuntimeProviderTest {
    private final HistoricalContractRuntimeProvider subject = new HistoricalContractRuntimeProvider();

    @Test
    void exposesOnlyHistoricalStateAndNoExecutableFacilities() {
        assertThat(subject.stateService()).isInstanceOf(HistoricalContractStateService.class);
        assertThat(subject.executable()).isFalse();
        assertThat(subject.serviceFeeCalculators()).isEmpty();
        assertThat(subject.queryFeeCalculators()).isEmpty();
        subject.initializeMetrics();
        subject.verifyNativeLibraries();
    }

    @Test
    void everyLegacyTransactionHandlerRejectsBeforeExecution() {
        final var handlers = subject.handlers();
        assertThatThrownBy(() -> handlers.contractCreateHandler().pureChecks(null))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> handlers.contractUpdateHandler().pureChecks(null))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> handlers.contractCallHandler().pureChecks(null))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> handlers.contractDeleteHandler().pureChecks(null))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> handlers.contractSystemDeleteHandler().pureChecks(null))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> handlers.contractSystemUndeleteHandler().pureChecks(null))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> handlers.ethereumTransactionHandler().pureChecks(null))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> handlers.hookStoreHandler().pureChecks(null))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> handlers.hookDispatchHandler().pureChecks(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
