// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.suites.reconnect;

import static com.hedera.services.bdd.junit.TestTags.ADHOC;
import static com.hedera.services.bdd.spec.HapiSpec.defaultHapiSpec;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.createTopic;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.cryptoCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.explicitContractCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.submitMessageTo;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.tokenCreate;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.blockingOrder;
import static com.hedera.services.bdd.suites.HapiSuite.DEFAULT_PAYER;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.INVALID_TRANSACTION_BODY;

import com.hedera.services.bdd.junit.HapiTest;
import com.hedera.services.bdd.spec.transactions.contract.HapiContractCall;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;

/**
 * Consumer-only validation for an authenticated post-write historical fixture.
 *
 * <p>The surrounding harness installs and verifies the immutable fixture before this suite runs.
 * This suite performs only retained native operations and fail-closed legacy-body assertions. It
 * never generates fixture state or invokes executable compatibility tooling.
 */
@Tag(ADHOC)
public class AuthenticatedHistoricalFixtureConsumerTest {
    @HapiTest
    final Stream<DynamicTest> retainedOperationsAndRetiredBodiesRemainValid() {
        return defaultHapiSpec("AuthenticatedHistoricalFixtureConsumer")
                .given(
                        cryptoCreate("p07FixtureConsumerAccount"),
                        tokenCreate("p07FixtureConsumerAsset").treasury(DEFAULT_PAYER),
                        createTopic("p07FixtureConsumerCoordinationTopic"),
                        submitMessageTo("p07FixtureConsumerCoordinationTopic")
                                .message("p07-fixture-consumer"))
                .when(rejectedBodies())
                .then();
    }

    private static com.hedera.services.bdd.spec.HapiSpecOperation rejectedBodies() {
        return blockingOrder(
                explicitContractCreate("p07RejectedCreate", (spec, body) -> body.setGas(100_000L))
                        .hasPrecheck(INVALID_TRANSACTION_BODY),
                new HapiContractCall("00000000000000000000000000000000000003ea")
                        .gas(100_000L)
                        .hasPrecheck(INVALID_TRANSACTION_BODY));
    }
}
