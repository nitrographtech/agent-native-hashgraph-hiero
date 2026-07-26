// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.junit.support.fixtures;

/** Stable identities used when translating retained historical contract results. */
public final class HistoricalContractResultFixtures {
    public static final String OVERSIZED_CONTRACT_ACTIONS_MEMO = "RecordsSuite.oversizedContractActionsAreClipped";
    public static final String PROPAGATED_REVERT_TEST_ID = "TraceabilitySuite.actionsShowPropagatedRevert";

    private HistoricalContractResultFixtures() {
        throw new UnsupportedOperationException("Utility class");
    }
}
