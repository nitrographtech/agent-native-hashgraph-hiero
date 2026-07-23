// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.suites.file;

import static com.hedera.services.bdd.spec.HapiSpec.hapiTest;
import static com.hedera.services.bdd.spec.transactions.TxnUtils.accountAllowanceHook;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.accountEvmHookStore;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.contractCall;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.contractCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.cryptoCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.uploadInitCode;
import static com.hedera.services.bdd.suites.HapiSuite.DEFAULT_PAYER;

import com.hedera.pbj.runtime.io.buffer.Bytes;
import com.hedera.services.bdd.junit.HapiTest;
import com.hedera.services.bdd.spec.HapiSpecOperation;
import com.hedera.services.bdd.suites.HapiSuite;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DynamicTest;

/**
 * Populates retained V0.65 contract maps through real remote-node transactions.
 *
 * <p>This suite is a fixture-generation input, not a lifecycle substitute. It must target a pinned
 * full-runtime node with {@code hooks.hooksEnabled=true}.
 */
public final class HistoricalContractStateFixtureCreation extends HapiSuite {
    private static final Logger LOG = LogManager.getLogger(HistoricalContractStateFixtureCreation.class);
    private static final String HOOK_CONTRACT = "StorageAccessHook";
    private static final String STORAGE_CONTRACT = "SimpleStorage";
    private static final String HOOK_OWNER = "p06aHookOwner";
    private static final long HOOK_ID = 234L;

    public static void main(final String... args) {
        new HistoricalContractStateFixtureCreation().runSuiteSync();
    }

    @Override
    public List<Stream<DynamicTest>> getSpecsInSuite() {
        return List.of(populatesV065Maps());
    }

    @HapiTest
    Stream<DynamicTest> populatesV065Maps() {
        final HapiSpecOperation createHookContract =
                contractCreate(HOOK_CONTRACT).gas(5_000_000L).via("p06aHookContractCreate");
        return hapiTest(
                uploadInitCode(STORAGE_CONTRACT),
                contractCreate(STORAGE_CONTRACT).gas(1_000_000L).via("p06aStorageContractCreate"),
                contractCall(STORAGE_CONTRACT, "set", BigInteger.valueOf(424_242L))
                        .gas(1_000_000L)
                        .via("p06aStorageWrite"),
                uploadInitCode(HOOK_CONTRACT),
                createHookContract,
                cryptoCreate(HOOK_OWNER)
                        .withHooks(accountAllowanceHook(HOOK_ID, HOOK_CONTRACT))
                        .via("p06aHookOwnerCreate"),
                accountEvmHookStore(HOOK_OWNER, HOOK_ID)
                        .putSlot(Bytes.wrap("p06a-slot"), Bytes.wrap("p06a-value"))
                        .signedBy(DEFAULT_PAYER, HOOK_OWNER)
                        .via("p06aHookStorageWrite"));
    }

    @Override
    protected Logger getResultsLogger() {
        return LOG;
    }
}
