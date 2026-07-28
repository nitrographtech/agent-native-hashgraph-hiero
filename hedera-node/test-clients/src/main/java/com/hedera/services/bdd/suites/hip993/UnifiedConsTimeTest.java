// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.suites.hip993;

import static com.hedera.services.bdd.junit.RepeatableReason.NEEDS_LAST_ASSIGNED_CONSENSUS_TIME;
import static com.hedera.services.bdd.junit.RepeatableReason.NEEDS_SYNCHRONOUS_HANDLE_WORKFLOW;
import static com.hedera.services.bdd.spec.HapiSpec.hapiTest;
import static com.hedera.services.bdd.spec.assertions.TransactionRecordAsserts.recordWith;
import static com.hedera.services.bdd.spec.queries.QueryVerbs.getTxnRecord;
import static com.hedera.services.bdd.spec.transactions.TxnUtils.asTimestamp;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.cryptoCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.cryptoTransfer;
import static com.hedera.services.bdd.spec.transactions.crypto.HapiCryptoTransfer.tinyBarsFromTo;
import static com.hedera.services.bdd.spec.utilops.CustomSpecAssert.allRunFor;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.usingEventBirthRound;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.withOpContext;
import static com.hedera.services.bdd.suites.HapiSuite.FUNDING;
import static com.hedera.services.bdd.suites.HapiSuite.GENESIS;
import static com.hedera.services.bdd.suites.HapiSuite.ONE_HBAR;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.BUSY;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hedera.services.bdd.junit.RepeatableHapiTest;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;

@DisplayName("given HIP-993 unified consensus times")
public class UnifiedConsTimeTest {
    /**
     * Tests that a user transaction gets the platform-assigned time. Requires a repeatable network
     * because we need virtual time to stand still between the point we submit the transaction and the
     * point we validate that its consensus time is the platform-assigned time
     */
    @DisplayName("user transaction gets platform assigned time")
    @RepeatableHapiTest(NEEDS_LAST_ASSIGNED_CONSENSUS_TIME)
    final Stream<DynamicTest> userTxnGetsPlatformAssignedTime() {
        return hapiTest(cryptoCreate("somebody").via("txn"), withOpContext((spec, opLog) -> {
            final var op = getTxnRecord("txn");
            allRunFor(spec, op);
            assertEquals(
                    asTimestamp(spec.consensusTime()),
                    op.getResponseRecord().getConsensusTimestamp(),
                    "User transaction should get platform-assigned time");
        }));
    }

    /**
     * Tests that a transaction reaching consensus from an earlier software version gets a {@code BUSY} record.
     * Must be run in repeatable mode because when bypassing ingest we don't cache the transaction id in the record
     * cache until consensus, so the immediately following query for the record will not find it unless "consensus"
     * happens synchronously as in repeatable mode.
     */
    @DisplayName("provides BUSY record if transaction reaches consensus after upgrade")
    @RepeatableHapiTest(NEEDS_SYNCHRONOUS_HANDLE_WORKFLOW)
    final Stream<DynamicTest> providesBusyRecordIfTransactionReachesAfterUpgrade() {
        return hapiTest(
                cryptoTransfer(tinyBarsFromTo(GENESIS, FUNDING, ONE_HBAR))
                        .memo("From a better time")
                        .setNode(4)
                        .withSubmissionStrategy(usingEventBirthRound(0))
                        .via("preUpgrade")
                        .hasAnyStatusAtAll(),
                getTxnRecord("preUpgrade")
                        .andAllChildRecords()
                        .hasNonStakingChildRecordCount(0)
                        .hasPriority(recordWith().status(BUSY)));
    }
}
