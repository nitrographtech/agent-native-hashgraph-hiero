// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.suites.reconnect;

import static com.hedera.services.bdd.junit.TestTags.ND_RECONNECT;
import static com.hedera.services.bdd.junit.hedera.NodeSelector.byNodeId;
import static com.hedera.services.bdd.spec.HapiSpec.defaultHapiSpec;
import static com.hedera.services.bdd.spec.transactions.TxnUtils.accountAllowanceHook;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.accountEvmHookStore;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.contractCall;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.contractCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.contractDelete;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.contractUpdate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.createTopic;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.cryptoCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.cryptoTransfer;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.explicitContractCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.explicitEthereumTransaction;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.submitMessageTo;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.tokenCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.uploadInitCode;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.blockingOrder;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.freezeOnly;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.overriding;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.sleepFor;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.waitForActive;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.waitForActiveNetworkWithReassignedPorts;
import static com.hedera.services.bdd.suites.HapiSuite.DEFAULT_PAYER;
import static com.hedera.services.bdd.suites.HapiSuite.FUNDING;
import static com.hedera.services.bdd.suites.HapiSuite.GENESIS;
import static com.hedera.services.bdd.suites.HapiSuite.RELAYER;
import static com.hedera.services.bdd.suites.regression.system.LifecycleTest.RESTART_TIMEOUT;
import static com.hedera.services.bdd.suites.regression.system.LifecycleTest.RESTART_TO_ACTIVE_TIMEOUT;
import static com.hedera.services.bdd.suites.regression.system.LifecycleTest.SHUTDOWN_TIMEOUT;
import static com.hedera.services.bdd.suites.regression.system.LifecycleTest.confirmFreezeAndShutdown;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.INVALID_TRANSACTION_BODY;

import com.google.protobuf.ByteString;
import com.hedera.pbj.runtime.io.buffer.Bytes;
import com.hedera.services.bdd.junit.HapiTest;
import com.hedera.services.bdd.junit.hedera.subprocess.SubProcessNode;
import com.hedera.services.bdd.spec.HapiSpecOperation;
import com.hedera.services.bdd.spec.utilops.FakeNmt;
import com.hedera.services.bdd.spec.utilops.lifecycle.ops.TryToStartNodesOp;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;

/**
 * Generates real historical contract maps, activates native mode, and forces a real
 * four-node reconnect with compromised-by-design fixture identities.
 */
@Tag(ND_RECONNECT)
public class P06aHistoricalStateReconnectTest {
    private static final long RECOVERING_NODE_ID = 2L;
    private static final int ACTIVATION_CONFIG_VERSION = 1;
    private static final Map<String, String> NATIVE_OVERRIDES =
            Map.of("contracts.enabled", "false", "hooks.hooksEnabled", "true");
    private static final Map<String, String> RECONNECT_OVERRIDES = Map.of(
            "contracts.enabled",
            "false",
            "hooks.hooksEnabled",
            "true",
            "event.preconsensus.databaseDirectory",
            "p06a-reconnect-preconsensus-events");

    @HapiTest
    final Stream<DynamicTest> historicalStateSurvivesNativeReconnect() {
        return defaultHapiSpec("P06aHistoricalStateSurvivesNativeReconnect")
                .given(
                        overriding("hooks.hooksEnabled", "true"),
                        uploadInitCode("SimpleStorage"),
                        contractCreate("SimpleStorage").gas(1_000_000L).via("p06aMultiStorageCreate"),
                        contractCall("SimpleStorage", "set", BigInteger.valueOf(424_242L))
                                .gas(1_000_000L)
                                .via("p06aMultiStorageWrite"),
                        uploadInitCode("StorageAccessHook"),
                        contractCreate("StorageAccessHook").gas(5_000_000L).via("p06aMultiHookCreate"),
                        cryptoCreate("p06aMultiHookOwner")
                                .withHooks(accountAllowanceHook(234L, "StorageAccessHook"))
                                .via("p06aMultiHookOwnerCreate"),
                        accountEvmHookStore("p06aMultiHookOwner", 234L)
                                .putSlot(Bytes.wrap("p06a-slot"), Bytes.wrap("p06a-value"))
                                .signedBy(DEFAULT_PAYER, "p06aMultiHookOwner")
                                .via("p06aMultiHookStorageWrite"),
                        cryptoCreate(RELAYER),
                        tokenCreate("p06aMultiNativeAsset").treasury(DEFAULT_PAYER),
                        createTopic("p06aMultiCoordinationTopic"),
                        submitMessageTo("p06aMultiCoordinationTopic").message("p06a-before-activation"))
                .when(
                        freezeOnly().startingIn(5).seconds().payingWith(GENESIS).deferStatusResolution(),
                        cryptoTransfer(
                                com.hedera.services.bdd.spec.transactions.crypto.HapiCryptoTransfer.tinyBarsFromTo(
                                        GENESIS, FUNDING, 1L)),
                        confirmFreezeAndShutdown(),
                        FakeNmt.restartNetwork(ACTIVATION_CONFIG_VERSION, NATIVE_OVERRIDES),
                        waitForActiveNetworkWithReassignedPorts(RESTART_TIMEOUT),
                        rejectedLegacyBodies("afterActivation"),
                        cryptoCreate("p06aAfterActivation"),
                        submitMessageTo("p06aMultiCoordinationTopic").message("p06a-after-activation"),
                        FakeNmt.shutdownWithin(byNodeId(RECOVERING_NODE_ID), SHUTDOWN_TIMEOUT),
                        nativeAdvancement(),
                        sleepFor(Duration.ofSeconds(180).toMillis()),
                        new TryToStartNodesOp(
                                byNodeId(RECOVERING_NODE_ID),
                                ACTIVATION_CONFIG_VERSION,
                                SubProcessNode.ReassignPorts.NO,
                                RECONNECT_OVERRIDES),
                        waitForActive(byNodeId(RECOVERING_NODE_ID), RESTART_TO_ACTIVE_TIMEOUT))
                .then(
                        cryptoCreate("p06aAfterReconnect").setNode("5"),
                        submitMessageTo("p06aMultiCoordinationTopic").message("p06a-after-reconnect"),
                        rejectedLegacyBodies("afterReconnect"));
    }

    private static HapiSpecOperation nativeAdvancement() {
        return blockingOrder(IntStream.range(0, 100)
                .mapToObj(i -> cryptoCreate("p06aWhileNodeDown" + i).setNode("3"))
                .toArray(HapiSpecOperation[]::new));
    }

    private static HapiSpecOperation rejectedLegacyBodies(final String suffix) {
        return blockingOrder(
                explicitContractCreate("p06aRejectedCreate" + suffix, (spec, body) -> body.setGas(100_000L))
                        .hasPrecheck(INVALID_TRANSACTION_BODY),
                contractCall("SimpleStorage").gas(100_000L).hasPrecheck(INVALID_TRANSACTION_BODY),
                contractUpdate("SimpleStorage").memo("must-not-mutate").hasPrecheck(INVALID_TRANSACTION_BODY),
                contractDelete("SimpleStorage").transferAccount(DEFAULT_PAYER).hasPrecheck(INVALID_TRANSACTION_BODY),
                explicitEthereumTransaction(
                                "p06aRejectedEthereum" + suffix,
                                (spec, body) -> body.setEthereumData(ByteString.copyFrom(new byte[] {1})))
                        .hasPrecheck(INVALID_TRANSACTION_BODY));
    }
}
