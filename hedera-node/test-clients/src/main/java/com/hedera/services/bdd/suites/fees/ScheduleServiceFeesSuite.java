// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.suites.fees;

import static com.hedera.services.bdd.junit.RepeatableReason.NEEDS_SYNCHRONOUS_HANDLE_WORKFLOW;
import static com.hedera.services.bdd.spec.HapiSpec.customizedHapiTest;
import static com.hedera.services.bdd.spec.queries.QueryVerbs.getScheduleInfo;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.cryptoCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.cryptoTransfer;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.scheduleCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.scheduleDelete;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.scheduleSign;
import static com.hedera.services.bdd.spec.transactions.crypto.HapiCryptoTransfer.tinyBarsFromTo;
import static com.hedera.services.bdd.spec.utilops.EmbeddedVerbs.handleAnyRepeatableQueryPayment;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.safeValidateChargedUsdWithin;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.validateChargedUsdWithin;
import static com.hedera.services.bdd.suites.HapiSuite.ONE_HBAR;
import static com.hedera.services.bdd.suites.hip1261.utils.SimpleFeesScheduleConstantsInUsd.QUERY_BASE_FEE;
import static com.hedera.services.bdd.suites.hip1261.utils.SimpleFeesScheduleConstantsInUsd.SCHEDULE_SIGN_FEE;
import static com.hedera.services.bdd.suites.hip1261.utils.SimpleFeesScheduleConstantsInUsd.SIGNATURE_FEE_AFTER_MULTIPLIER;
import static com.hedera.services.bdd.suites.schedule.ScheduleUtils.OTHER_PAYER;
import static com.hedera.services.bdd.suites.schedule.ScheduleUtils.PAYING_SENDER;
import static com.hedera.services.bdd.suites.schedule.ScheduleUtils.RECEIVER;

import com.hedera.services.bdd.junit.HapiTestLifecycle;
import com.hedera.services.bdd.junit.LeakyRepeatableHapiTest;
import com.hedera.services.bdd.junit.support.TestLifecycle;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;

@HapiTestLifecycle
public class ScheduleServiceFeesSuite {
    private static final double BASE_FEE_SCHEDULE_CREATE = 0.01;
    private static final double BASE_FEE_SCHEDULE_SIGN = 0.001;
    private static final double BASE_FEE_SCHEDULE_DELETE = 0.001;
    private static final double BASE_FEE_SCHEDULE_INFO = 0.000102;
    @BeforeAll
    static void beforeAll(@NonNull final TestLifecycle testLifecycle) {
        testLifecycle.overrideInClass(Map.of(
                "scheduling.whitelist", "CryptoCreate,CryptoTransfer,FileDelete,FileUpdate,SystemDelete"));
    }

    @LeakyRepeatableHapiTest(value = NEEDS_SYNCHRONOUS_HANDLE_WORKFLOW, fees = "scheduled-contract-fees.json")
    @DisplayName("Schedule ops have expected USD fees")
    final Stream<DynamicTest> scheduleOpsBaseUSDFees() {
        final String SCHEDULE_NAME = "canonical";
        return customizedHapiTest(
                Map.of("memo.useSpecName", "false"),
                cryptoCreate(OTHER_PAYER),
                cryptoCreate(PAYING_SENDER),
                cryptoCreate(RECEIVER).receiverSigRequired(true),
                scheduleCreate(
                                SCHEDULE_NAME,
                                cryptoTransfer(tinyBarsFromTo(PAYING_SENDER, RECEIVER, 1L))
                                        .memo("")
                                        .fee(ONE_HBAR))
                        .payingWith(OTHER_PAYER)
                        .via("canonicalCreation")
                        .alsoSigningWith(PAYING_SENDER)
                        .adminKey(OTHER_PAYER),
                scheduleSign(SCHEDULE_NAME)
                        .via("canonicalSigning")
                        .payingWith(PAYING_SENDER)
                        .alsoSigningWith(RECEIVER),
                scheduleCreate(
                                "tbd",
                                cryptoTransfer(tinyBarsFromTo(PAYING_SENDER, RECEIVER, 1L))
                                        .memo("")
                                        .fee(ONE_HBAR))
                        .payingWith(PAYING_SENDER)
                        .adminKey(PAYING_SENDER),
                scheduleDelete("tbd").via("canonicalDeletion").payingWith(PAYING_SENDER),
                getScheduleInfo(SCHEDULE_NAME)
                        .payingWith(OTHER_PAYER)
                        .signedBy(OTHER_PAYER)
                        .via("getScheduleInfoBasic"),
                handleAnyRepeatableQueryPayment(),
                validateChargedUsdWithin("canonicalCreation", BASE_FEE_SCHEDULE_CREATE, 3.0),
                safeValidateChargedUsdWithin(
                        "canonicalSigning",
                        BASE_FEE_SCHEDULE_SIGN,
                        1.0,
                        // base plus one extra signature
                        SCHEDULE_SIGN_FEE + SIGNATURE_FEE_AFTER_MULTIPLIER,
                        0.1),
                validateChargedUsdWithin("canonicalDeletion", BASE_FEE_SCHEDULE_DELETE, 3.0),
                safeValidateChargedUsdWithin("getScheduleInfoBasic", BASE_FEE_SCHEDULE_INFO, 1.0, QUERY_BASE_FEE, 0.1));
    }
}
