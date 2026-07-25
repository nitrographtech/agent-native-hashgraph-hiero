// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.suites.file;

import static com.hedera.services.bdd.spec.HapiSpec.hapiTest;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.contractCall;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.contractDelete;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.contractUpdate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.cryptoCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.explicitContractCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.explicitEthereumTransaction;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.INVALID_TRANSACTION_BODY;

import com.google.protobuf.ByteString;
import com.hedera.services.bdd.spec.HapiSpecOperation;
import com.hedera.services.bdd.suites.HapiSuite;
import java.util.List;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DynamicTest;

/**
 * Remote-node proof that every retained executable contract transaction body fails closed.
 *
 * <p>This suite must target the native-agent distribution after opening an authenticated
 * historical fixture.
 */
public final class HistoricalContractExecutionRejection extends HapiSuite {
    private static final Logger LOG = LogManager.getLogger(HistoricalContractExecutionRejection.class);
    private static final String HISTORICAL_CONTRACT = "0.0.1001";

    public static void main(final String... args) {
        new HistoricalContractExecutionRejection().runSuiteSync();
    }

    @Override
    public List<Stream<DynamicTest>> getSpecsInSuite() {
        return List.of(rejectsEveryExecutableBodyAndContinuesNatively());
    }

    Stream<DynamicTest> rejectsEveryExecutableBodyAndContinuesNatively() {
        final HapiSpecOperation rejectedCreate =
                explicitContractCreate("p06aRejectedCreate", (spec, body) -> body.setGas(100_000L))
                        .hasPrecheck(INVALID_TRANSACTION_BODY);
        final HapiSpecOperation rejectedCall = contractCall(HISTORICAL_CONTRACT)
                .gas(100_000L)
                .hasPrecheck(INVALID_TRANSACTION_BODY);
        final HapiSpecOperation rejectedUpdate = contractUpdate(HISTORICAL_CONTRACT)
                .memo("must-not-mutate")
                .hasPrecheck(INVALID_TRANSACTION_BODY);
        final HapiSpecOperation rejectedDelete = contractDelete(HISTORICAL_CONTRACT)
                .transferAccount(DEFAULT_PAYER)
                .hasPrecheck(INVALID_TRANSACTION_BODY);
        final HapiSpecOperation rejectedEthereum =
                explicitEthereumTransaction(
                                "p06aRejectedEthereum",
                                (spec, body) -> body.setEthereumData(ByteString.copyFrom(new byte[] {1})))
                        .hasPrecheck(INVALID_TRANSACTION_BODY);
        return hapiTest(
                rejectedCreate,
                rejectedCall,
                rejectedUpdate,
                rejectedDelete,
                rejectedEthereum,
                cryptoCreate("p06aNativeContinuityAfterRejection").via("p06aNativeAfterRejection"));
    }

    @Override
    protected Logger getResultsLogger() {
        return LOG;
    }
}
