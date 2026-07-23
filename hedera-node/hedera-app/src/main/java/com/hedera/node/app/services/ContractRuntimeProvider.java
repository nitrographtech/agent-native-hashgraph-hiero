// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.services;

import com.hedera.node.app.service.contract.ContractService;
import com.hedera.node.app.spi.fees.QueryFeeCalculator;
import com.hedera.node.app.spi.fees.ServiceFeeCalculator;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Set;

/** Selects either the full executable contract runtime or retained read-only historical state. */
public interface ContractRuntimeProvider {
    @NonNull
    ContractService stateService();

    @NonNull
    ContractRuntimeHandlers handlers();

    @NonNull
    Set<ServiceFeeCalculator> serviceFeeCalculators();

    @NonNull
    Set<QueryFeeCalculator> queryFeeCalculators();

    void initializeMetrics();

    void verifyNativeLibraries();

    boolean executable();
}
