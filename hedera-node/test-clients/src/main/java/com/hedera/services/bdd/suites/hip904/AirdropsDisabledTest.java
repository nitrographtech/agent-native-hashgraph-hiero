// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.suites.hip904;

import static com.google.protobuf.ByteString.copyFromUtf8;
import static com.hedera.services.bdd.junit.TestTags.SERIAL;
import static com.hedera.services.bdd.spec.HapiSpec.hapiTest;
import static com.hedera.services.bdd.spec.assertions.AccountInfoAsserts.accountWith;
import static com.hedera.services.bdd.spec.assertions.AutoAssocAsserts.accountTokenPairsInAnyOrder;
import static com.hedera.services.bdd.spec.assertions.ContractFnResultAsserts.resultWith;
import static com.hedera.services.bdd.spec.assertions.ContractInfoAsserts.contractWith;
import static com.hedera.services.bdd.spec.assertions.TransactionRecordAsserts.recordWith;
import static com.hedera.services.bdd.spec.queries.QueryVerbs.contractCallLocal;
import static com.hedera.services.bdd.spec.queries.QueryVerbs.getAccountInfo;
import static com.hedera.services.bdd.spec.queries.QueryVerbs.getContractBytecode;
import static com.hedera.services.bdd.spec.queries.QueryVerbs.getContractInfo;
import static com.hedera.services.bdd.spec.queries.QueryVerbs.getTxnRecord;
import static com.hedera.services.bdd.spec.queries.crypto.ExpectedTokenRel.relationshipWith;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.contractCall;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.contractCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.cryptoCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.cryptoTransfer;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.mintToken;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.tokenAirdrop;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.tokenClaimAirdrop;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.tokenCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.uploadInitCode;
import static com.hedera.services.bdd.spec.transactions.contract.HapiParserUtil.asHeadlongAddress;
import static com.hedera.services.bdd.spec.transactions.token.CustomFeeSpecs.royaltyFeeNoFallback;
import static com.hedera.services.bdd.spec.transactions.token.HapiTokenClaimAirdrop.pendingAirdrop;
import static com.hedera.services.bdd.spec.transactions.token.TokenMovement.moving;
import static com.hedera.services.bdd.spec.transactions.token.TokenMovement.movingUnique;
import static com.hedera.services.bdd.spec.utilops.CustomSpecAssert.allRunFor;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.newKeyNamed;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.sourcing;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.withOpContext;
import static com.hedera.services.bdd.suites.HapiSuite.DEFAULT_PAYER;
import static com.hedera.services.bdd.suites.HapiSuite.GENESIS;
import static com.hedera.services.bdd.suites.HapiSuite.ONE_HBAR;
import static com.hedera.services.bdd.suites.HapiSuite.ONE_HUNDRED_HBARS;
import static com.hedera.services.bdd.suites.HapiSuite.TOKEN_TREASURY;
import static com.hedera.services.bdd.suites.HapiSuite.flattened;
import static com.hedera.services.bdd.suites.contract.Utils.asHexedSolidityAddress;
import static com.hedera.services.bdd.suites.contract.Utils.captureChildCreate2MetaFor;
import static com.hedera.services.bdd.suites.contract.Utils.captureOneChildCreate2MetaFor;
import static com.hedera.services.bdd.suites.contract.Utils.contractIdFromHexedMirrorAddress;
import static com.hedera.services.bdd.suites.contract.hapi.ContractUpdateSuite.ADMIN_KEY;
import static com.hedera.services.bdd.suites.crypto.AutoAccountCreationSuite.A_TOKEN;
import static com.hedera.services.bdd.suites.crypto.AutoAccountCreationSuite.LAZY_MEMO;
import static com.hedera.services.bdd.suites.crypto.AutoAccountCreationSuite.NFT_CREATE;
import static com.hedera.services.bdd.suites.crypto.AutoAccountCreationSuite.NFT_INFINITE_SUPPLY_TOKEN;
import static com.hedera.services.bdd.suites.crypto.AutoAccountCreationSuite.PARTY;
import static com.hedera.services.bdd.suites.crypto.AutoAccountCreationSuite.TOKEN_A_CREATE;
import static com.hedera.services.bdd.suites.crypto.CryptoTransferSuite.COUNTERPARTY;
import static com.hedera.services.bdd.suites.crypto.CryptoTransferSuite.HODL_XFER;
import static com.hedera.services.bdd.suites.token.TokenAssociationSpecs.MULTI_KEY;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.CONTRACT_REVERT_EXECUTED;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.INVALID_SOLIDITY_ADDRESS;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.NOT_SUPPORTED;
import static com.hederahashgraph.api.proto.java.TokenSupplyType.FINITE;
import static com.hederahashgraph.api.proto.java.TokenType.FUNGIBLE_COMMON;
import static com.hederahashgraph.api.proto.java.TokenType.NON_FUNGIBLE_UNIQUE;

import com.google.protobuf.ByteString;
import com.hedera.services.bdd.junit.HapiTest;
import com.hedera.services.bdd.junit.HapiTestLifecycle;
import com.hedera.services.bdd.junit.support.TestLifecycle;
import com.hedera.services.bdd.spec.HapiSpecOperation;
import com.hedera.services.bdd.spec.transactions.crypto.HapiCryptoTransfer;
import com.hedera.services.bdd.spec.utilops.CustomSpecAssert;
import com.hedera.services.bdd.suites.contract.Utils;
import com.hederahashgraph.api.proto.java.AccountID;
import com.hederahashgraph.api.proto.java.TokenID;
import com.hederahashgraph.api.proto.java.TokenSupplyType;
import com.hederahashgraph.api.proto.java.TokenTransferList;
import com.hederahashgraph.api.proto.java.TokenType;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;

/**
 * Tests expected behavior when the {@code entities.unlimitedAutoAssociationsEnabled} feature flag is off for
 * <a href="https://hips.hedera.com/hip/hip-904">HIP-904, "Frictionless Airdrops"</a>.
 */
@Tag(SERIAL)
@HapiTestLifecycle
public class AirdropsDisabledTest {
    private static final Logger LOG = LogManager.getLogger(AirdropsDisabledTest.class);

    private static final String owner = "owner";
    private static final String receiver = "receiver";
    private static final String fungibleToken = "fungibleToken";

    @BeforeAll
    static void beforeAll(@NonNull final TestLifecycle testLifecycle) {
        testLifecycle.overrideInClass(Map.of(
                "tokens.airdrops.enabled", "false",
                "entities.unlimitedAutoAssociationsEnabled", "false",
                "tokens.airdrops.claim.enabled", "false"));
        testLifecycle.doAdhoc(
                cryptoCreate(owner).balance(ONE_HUNDRED_HBARS),
                cryptoCreate(receiver),
                tokenCreate(fungibleToken).tokenType(TokenType.FUNGIBLE_COMMON).treasury(owner));
    }

    @HapiTest
    @DisplayName("airdrop feature flag is disabled")
    final Stream<DynamicTest> airdropNotSupported() {
        return hapiTest(tokenAirdrop(moving(10, fungibleToken).between(owner, receiver))
                .payingWith(owner)
                .hasPrecheck(NOT_SUPPORTED));
    }

    @HapiTest
    @DisplayName("airdrop claim feature flag is disabled")
    final Stream<DynamicTest> airdropClaimNotSupported() {
        return hapiTest(tokenClaimAirdrop(pendingAirdrop(owner, receiver, fungibleToken))
                .hasPrecheck(NOT_SUPPORTED));
    }

    @HapiTest
    final Stream<DynamicTest> royaltyCollectorsCanUseAutoAssociation() {
        final var uniqueWithRoyalty = "uniqueWithRoyalty";
        final var firstFungible = "firstFungible";
        final var secondFungible = "secondFungible";
        final var firstRoyaltyCollector = "firstRoyaltyCollector";
        final var secondRoyaltyCollector = "secondRoyaltyCollector";
        final var plentyOfSlots = 10;
        final var exchangeAmount = 12 * 15;
        final var firstRoyaltyAmount = exchangeAmount / 12;
        final var secondRoyaltyAmount = exchangeAmount / 15;
        final var netExchangeAmount = exchangeAmount - firstRoyaltyAmount - secondRoyaltyAmount;

        return hapiTest(
                cryptoCreate(TOKEN_TREASURY),
                cryptoCreate(firstRoyaltyCollector).maxAutomaticTokenAssociations(plentyOfSlots),
                cryptoCreate(secondRoyaltyCollector).maxAutomaticTokenAssociations(plentyOfSlots),
                cryptoCreate(PARTY).maxAutomaticTokenAssociations(plentyOfSlots),
                cryptoCreate(COUNTERPARTY).maxAutomaticTokenAssociations(plentyOfSlots),
                newKeyNamed(MULTI_KEY),
                getAccountInfo(PARTY).savingSnapshot(PARTY),
                getAccountInfo(COUNTERPARTY).savingSnapshot(COUNTERPARTY),
                getAccountInfo(firstRoyaltyCollector).savingSnapshot(firstRoyaltyCollector),
                getAccountInfo(secondRoyaltyCollector).savingSnapshot(secondRoyaltyCollector),
                tokenCreate(firstFungible)
                        .treasury(TOKEN_TREASURY)
                        .tokenType(FUNGIBLE_COMMON)
                        .initialSupply(123456789),
                tokenCreate(secondFungible)
                        .treasury(TOKEN_TREASURY)
                        .tokenType(FUNGIBLE_COMMON)
                        .initialSupply(123456789),
                cryptoTransfer(
                        moving(1000, firstFungible).between(TOKEN_TREASURY, COUNTERPARTY),
                        moving(1000, secondFungible).between(TOKEN_TREASURY, COUNTERPARTY)),
                tokenCreate(uniqueWithRoyalty)
                        .tokenType(NON_FUNGIBLE_UNIQUE)
                        .treasury(TOKEN_TREASURY)
                        .supplyKey(MULTI_KEY)
                        .withCustom(royaltyFeeNoFallback(1, 12, firstRoyaltyCollector))
                        .withCustom(royaltyFeeNoFallback(1, 15, secondRoyaltyCollector))
                        .initialSupply(0L),
                mintToken(uniqueWithRoyalty, List.of(copyFromUtf8("HODL"))),
                cryptoTransfer(movingUnique(uniqueWithRoyalty, 1L).between(TOKEN_TREASURY, PARTY)),
                cryptoTransfer(
                                movingUnique(uniqueWithRoyalty, 1L).between(PARTY, COUNTERPARTY),
                                moving(12 * 15L, firstFungible).between(COUNTERPARTY, PARTY),
                                moving(12 * 15L, secondFungible).between(COUNTERPARTY, PARTY))
                        .fee(ONE_HBAR)
                        .via(HODL_XFER),
                getTxnRecord(HODL_XFER)
                        .hasPriority(recordWith()
                                .autoAssociated(accountTokenPairsInAnyOrder(List.of(
                                        /* The counterparty auto-associates to the non-fungible type */
                                        Pair.of(COUNTERPARTY, uniqueWithRoyalty),
                                        /* The sending party auto-associates to both fungibles */
                                        Pair.of(PARTY, firstFungible),
                                        Pair.of(PARTY, secondFungible),
                                        /* Both royalty collectors auto-associate to both fungibles */
                                        Pair.of(firstRoyaltyCollector, firstFungible),
                                        Pair.of(secondRoyaltyCollector, firstFungible),
                                        Pair.of(firstRoyaltyCollector, secondFungible),
                                        Pair.of(secondRoyaltyCollector, secondFungible))))),
                getAccountInfo(PARTY)
                        .has(accountWith()
                                .newAssociationsFromSnapshot(
                                        PARTY,
                                        List.of(
                                                relationshipWith(uniqueWithRoyalty)
                                                        .balance(0),
                                                relationshipWith(firstFungible).balance(netExchangeAmount),
                                                relationshipWith(secondFungible).balance(netExchangeAmount)))),
                getAccountInfo(COUNTERPARTY)
                        .has(accountWith()
                                .newAssociationsFromSnapshot(
                                        PARTY,
                                        List.of(
                                                relationshipWith(uniqueWithRoyalty)
                                                        .balance(1),
                                                relationshipWith(firstFungible).balance(1000L - exchangeAmount),
                                                relationshipWith(secondFungible).balance(1000L - exchangeAmount)))),
                getAccountInfo(firstRoyaltyCollector)
                        .has(accountWith()
                                .newAssociationsFromSnapshot(
                                        PARTY,
                                        List.of(
                                                relationshipWith(firstFungible).balance(exchangeAmount / 12),
                                                relationshipWith(secondFungible).balance(exchangeAmount / 12)))),
                getAccountInfo(secondRoyaltyCollector)
                        .has(accountWith()
                                .newAssociationsFromSnapshot(
                                        PARTY,
                                        List.of(
                                                relationshipWith(firstFungible).balance(exchangeAmount / 15),
                                                relationshipWith(secondFungible).balance(exchangeAmount / 15)))));
    }
}
