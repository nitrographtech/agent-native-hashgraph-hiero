// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.suites.crypto;

import static com.hedera.node.app.hapi.utils.EthSigsUtils.recoverAddressFromPubKey;
import static com.hedera.services.bdd.junit.ContextRequirement.FEE_SCHEDULE_OVERRIDES;
import static com.hedera.services.bdd.junit.EmbeddedReason.NEEDS_STATE_ACCESS;
import static com.hedera.services.bdd.junit.TestTags.CRYPTO;
import static com.hedera.services.bdd.junit.TestTags.ONLY_EMBEDDED;
import static com.hedera.services.bdd.junit.hedera.embedded.EmbeddedMode.CONCURRENT;
import static com.hedera.services.bdd.spec.HapiSpec.customizedHapiTest;
import static com.hedera.services.bdd.spec.HapiSpec.hapiTest;
import static com.hedera.services.bdd.spec.assertions.AccountDetailsAsserts.accountDetailsWith;
import static com.hedera.services.bdd.spec.assertions.AccountInfoAsserts.accountWith;
import static com.hedera.services.bdd.spec.queries.QueryVerbs.getAccountBalance;
import static com.hedera.services.bdd.spec.queries.QueryVerbs.getAccountDetails;
import static com.hedera.services.bdd.spec.queries.QueryVerbs.getAliasedAccountInfo;
import static com.hedera.services.bdd.spec.queries.QueryVerbs.getTxnRecord;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.createWellKnownFungibleToken;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.createWellKnownNonFungibleToken;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.cryptoApproveAllowance;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.cryptoCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.cryptoTransfer;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.cryptoUpdate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.explicit;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.mintToken;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.tokenAssociate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.tokenCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.tokenDissociate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.wellKnownTokenEntities;
import static com.hedera.services.bdd.spec.transactions.crypto.HapiCryptoTransfer.tinyBarsFromTo;
import static com.hedera.services.bdd.spec.transactions.token.CustomFeeSpecs.fixedHbarFeeInheritingRoyaltyCollector;
import static com.hedera.services.bdd.spec.transactions.token.CustomFeeSpecs.fixedHtsFeeInheritingRoyaltyCollector;
import static com.hedera.services.bdd.spec.transactions.token.CustomFeeSpecs.fractionalFee;
import static com.hedera.services.bdd.spec.transactions.token.CustomFeeSpecs.fractionalFeeNetOfTransfers;
import static com.hedera.services.bdd.spec.transactions.token.CustomFeeSpecs.royaltyFeeNoFallback;
import static com.hedera.services.bdd.spec.transactions.token.CustomFeeSpecs.royaltyFeeWithFallback;
import static com.hedera.services.bdd.spec.transactions.token.TokenMovement.moving;
import static com.hedera.services.bdd.spec.transactions.token.TokenMovement.movingUnique;
import static com.hedera.services.bdd.spec.utilops.CustomSpecAssert.allRunFor;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.blockingOrder;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.inParallel;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.newKeyNamed;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.overriding;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.overridingTwo;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.reduceFeeFor;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.sleepFor;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.withOpContext;
import static com.hedera.services.bdd.suites.HapiSuite.CIVILIAN_PAYER;
import static com.hedera.services.bdd.suites.HapiSuite.DEFAULT_PAYER;
import static com.hedera.services.bdd.suites.HapiSuite.GENESIS;
import static com.hedera.services.bdd.suites.HapiSuite.ONE_HBAR;
import static com.hedera.services.bdd.suites.HapiSuite.ONE_HUNDRED_HBARS;
import static com.hedera.services.bdd.suites.HapiSuite.SECP_256K1_SHAPE;
import static com.hedera.services.bdd.suites.HapiSuite.SECP_256K1_SOURCE_KEY;
import static com.hedera.services.bdd.suites.HapiSuite.THREE_MONTHS_IN_SECONDS;
import static com.hedera.services.bdd.suites.HapiSuite.TOKEN_TREASURY;
import static com.hedera.services.bdd.suites.crypto.AutoAccountCreationSuite.LAZY_MEMO;
import static com.hedera.services.bdd.suites.crypto.AutoAccountCreationSuite.VALID_ALIAS;
import static com.hedera.services.bdd.suites.crypto.CryptoApproveAllowanceSuite.ANOTHER_SPENDER;
import static com.hedera.services.bdd.suites.crypto.CryptoApproveAllowanceSuite.FUNGIBLE_TOKEN;
import static com.hedera.services.bdd.suites.crypto.CryptoApproveAllowanceSuite.FUNGIBLE_TOKEN_MINT_TXN;
import static com.hedera.services.bdd.suites.crypto.CryptoApproveAllowanceSuite.NFT_TOKEN_MINT_TXN;
import static com.hedera.services.bdd.suites.crypto.CryptoApproveAllowanceSuite.NON_FUNGIBLE_TOKEN;
import static com.hedera.services.bdd.suites.crypto.CryptoApproveAllowanceSuite.OWNER;
import static com.hedera.services.bdd.suites.crypto.CryptoApproveAllowanceSuite.SECOND_SPENDER;
import static com.hedera.services.bdd.suites.crypto.CryptoApproveAllowanceSuite.SPENDER;
import static com.hedera.services.bdd.suites.crypto.CryptoApproveAllowanceSuite.THIRD_SPENDER;
import static com.hedera.services.bdd.suites.file.FileUpdateSuite.CIVILIAN;
import static com.hedera.services.bdd.suites.hip1261.utils.FeesChargingUtils.validateFees;
import static com.hedera.services.bdd.suites.hip1261.utils.SimpleFeesScheduleConstantsInUsd.CRYPTO_UPDATE_FEE;
import static com.hedera.services.bdd.suites.token.TokenTransactSpecs.SUPPLY_KEY;
import static com.hedera.services.bdd.suites.token.TokenTransactSpecs.TRANSFER_TXN;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.CrsPublication;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.CryptoCreate;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.CryptoTransfer;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.CryptoUpdate;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.HintsKeyPublication;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.HintsPartialSignature;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.HintsPreprocessingVote;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.HistoryAssemblySignature;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.HistoryProofKeyPublication;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.HistoryProofVote;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.HookDispatch;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.NodeStakeUpdate;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.StateSignatureTransaction;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.BUSY;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.INSUFFICIENT_ACCOUNT_BALANCE;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.INSUFFICIENT_PAYER_BALANCE;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.INSUFFICIENT_SENDER_ACCOUNT_BALANCE_FOR_CUSTOM_FEE;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.INVALID_ACCOUNT_ID;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.MAX_ALLOWANCES_EXCEEDED;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.OK;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.REQUESTED_NUM_AUTOMATIC_ASSOCIATIONS_EXCEEDS_ASSOCIATION_LIMIT;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.SUCCESS;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.TRANSACTION_REQUIRES_ZERO_TOKEN_BALANCES;
import static com.hederahashgraph.api.proto.java.SubType.DEFAULT;
import static com.hederahashgraph.api.proto.java.TokenType.FUNGIBLE_COMMON;
import static com.hederahashgraph.api.proto.java.TokenType.NON_FUNGIBLE_UNIQUE;

import com.google.protobuf.ByteString;
import com.hedera.hapi.node.hooks.legacy.HookDispatchTransactionBody;
import com.hedera.hapi.services.auxiliary.hints.legacy.CrsPublicationTransactionBody;
import com.hedera.hapi.services.auxiliary.hints.legacy.HintsKeyPublicationTransactionBody;
import com.hedera.hapi.services.auxiliary.hints.legacy.HintsPartialSignatureTransactionBody;
import com.hedera.hapi.services.auxiliary.hints.legacy.HintsPreprocessingVoteTransactionBody;
import com.hedera.hapi.services.auxiliary.history.legacy.HistoryProofKeyPublicationTransactionBody;
import com.hedera.hapi.services.auxiliary.history.legacy.HistoryProofSignatureTransactionBody;
import com.hedera.hapi.services.auxiliary.history.legacy.HistoryProofVoteTransactionBody;
import com.hedera.services.bdd.junit.HapiTest;
import com.hedera.services.bdd.junit.LeakyEmbeddedHapiTest;
import com.hedera.services.bdd.junit.OrderedInIsolation;
import com.hedera.services.bdd.junit.TargetEmbeddedMode;
import com.hedera.services.bdd.spec.HapiSpec;
import com.hedera.services.bdd.spec.HapiSpecOperation;
import com.hedera.services.bdd.spec.transactions.crypto.HapiCryptoTransfer;
import com.hederahashgraph.api.proto.java.HederaFunctionality;
import com.hederahashgraph.api.proto.java.NodeStakeUpdateTransactionBody;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;
import com.hederahashgraph.api.proto.java.TokenSupplyType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;

@Tag(ONLY_EMBEDDED)
@Tag(CRYPTO)
@TargetEmbeddedMode(CONCURRENT)
@OrderedInIsolation
public class LeakyCryptoTestsSuite {
    private static final Logger log = LogManager.getLogger(LeakyCryptoTestsSuite.class);

    private static final String FACTORY_MIRROR_CONTRACT = "FactoryMirror";
    public static final String AUTO_ACCOUNT = "autoAccount";
    public static final String LAZY_ACCOUNT_RECIPIENT = "lazyAccountRecipient";
    public static final String PAY_TXN = "payTxn";
    public static final String CREATE_TX = "createTX";

    @Order(16)
    @LeakyEmbeddedHapiTest(
            reason = NEEDS_STATE_ACCESS,
            overrides = {"ledger.maxAutoAssociations", "ledger.autoRenewPeriod.minDuration"})
    final Stream<DynamicTest> autoAssociationPropertiesWorkAsExpected() {
        final var shortLivedAutoAssocUser = "shortLivedAutoAssocUser";
        final var longLivedAutoAssocUser = "longLivedAutoAssocUser";
        final var payerBalance = 100 * ONE_HUNDRED_HBARS;
        final var updateWithExpiredAccount = "updateWithExpiredAccount";
        final var baseFee = 0.0002167032;
        return customizedHapiTest(
                Map.of("memo.useSpecName", "false"),
                overridingTwo(
                        "ledger.maxAutoAssociations", "100",
                        "ledger.autoRenewPeriod.minDuration", "1"),
                cryptoCreate(longLivedAutoAssocUser).balance(payerBalance).autoRenewSecs(THREE_MONTHS_IN_SECONDS),
                cryptoCreate(shortLivedAutoAssocUser).balance(payerBalance).autoRenewSecs(1),
                cryptoUpdate(longLivedAutoAssocUser)
                        .payingWith(longLivedAutoAssocUser)
                        .maxAutomaticAssociations(101)
                        .hasKnownStatus(REQUESTED_NUM_AUTOMATIC_ASSOCIATIONS_EXCEEDS_ASSOCIATION_LIMIT),
                cryptoUpdate(shortLivedAutoAssocUser)
                        .payingWith(shortLivedAutoAssocUser)
                        .maxAutomaticAssociations(10)
                        .via(updateWithExpiredAccount),
                validateFees(updateWithExpiredAccount, baseFee, CRYPTO_UPDATE_FEE));
    }

    @Order(1)
    @LeakyEmbeddedHapiTest(
            reason = NEEDS_STATE_ACCESS,
            overrides = {"ledger.autoRenewPeriod.minDuration"})
    final Stream<DynamicTest> cannotDissociateFromExpiredTokenWithNonZeroBalance() {
        final var civilian = "civilian";
        final long initialSupply = 100L;
        final long nonZeroXfer = 10L;
        final var numTokens = 10;
        final IntFunction<String> tokenNameFn = i -> "fungible" + i;
        final String[] assocOrder = new String[numTokens];
        Arrays.setAll(assocOrder, tokenNameFn);
        final String[] dissocOrder = new String[numTokens];
        Arrays.setAll(dissocOrder, i -> tokenNameFn.apply(numTokens - 1 - i));

        return hapiTest(
                overriding("ledger.autoRenewPeriod.minDuration", "1"),
                cryptoCreate(TOKEN_TREASURY),
                cryptoCreate(civilian).balance(0L),
                blockingOrder(IntStream.range(0, numTokens)
                        .mapToObj(i -> tokenCreate(tokenNameFn.apply(i))
                                .autoRenewAccount(DEFAULT_PAYER)
                                .autoRenewPeriod(1L)
                                .initialSupply(initialSupply)
                                .treasury(TOKEN_TREASURY))
                        .toArray(HapiSpecOperation[]::new)),
                tokenAssociate(civilian, List.of(assocOrder)),
                blockingOrder(IntStream.range(0, numTokens)
                        .mapToObj(i -> cryptoTransfer(
                                moving(nonZeroXfer, tokenNameFn.apply(i)).between(TOKEN_TREASURY, civilian)))
                        .toArray(HapiSpecOperation[]::new)),
                sleepFor(2_000L),
                tokenDissociate(civilian, dissocOrder).hasKnownStatus(TRANSACTION_REQUIRES_ZERO_TOKEN_BALANCES));
    }

    @Order(2)
    @LeakyEmbeddedHapiTest(
            reason = NEEDS_STATE_ACCESS,
            overrides = {"hedera.allowances.maxTransactionLimit", "hedera.allowances.maxAccountLimit"})
    final Stream<DynamicTest> cannotExceedAccountAllowanceLimit() {
        return hapiTest(
                overridingTwo(
                        "hedera.allowances.maxAccountLimit", "3",
                        "hedera.allowances.maxTransactionLimit", "5"),
                newKeyNamed(SUPPLY_KEY),
                cryptoCreate(OWNER).balance(ONE_HUNDRED_HBARS).maxAutomaticTokenAssociations(10),
                cryptoCreate(SPENDER).balance(ONE_HUNDRED_HBARS),
                cryptoCreate(ANOTHER_SPENDER).balance(ONE_HUNDRED_HBARS),
                cryptoCreate(SECOND_SPENDER).balance(ONE_HUNDRED_HBARS),
                cryptoCreate(TOKEN_TREASURY).balance(100 * ONE_HUNDRED_HBARS).maxAutomaticTokenAssociations(10),
                tokenCreate(FUNGIBLE_TOKEN)
                        .tokenType(FUNGIBLE_COMMON)
                        .supplyType(TokenSupplyType.FINITE)
                        .supplyKey(SUPPLY_KEY)
                        .maxSupply(1000L)
                        .initialSupply(10L)
                        .treasury(TOKEN_TREASURY),
                tokenCreate(NON_FUNGIBLE_TOKEN)
                        .maxSupply(10L)
                        .initialSupply(0)
                        .supplyType(TokenSupplyType.FINITE)
                        .tokenType(NON_FUNGIBLE_UNIQUE)
                        .supplyKey(SUPPLY_KEY)
                        .treasury(TOKEN_TREASURY),
                tokenAssociate(OWNER, FUNGIBLE_TOKEN),
                tokenAssociate(OWNER, NON_FUNGIBLE_TOKEN),
                mintToken(
                                NON_FUNGIBLE_TOKEN,
                                List.of(
                                        ByteString.copyFromUtf8("a"),
                                        ByteString.copyFromUtf8("b"),
                                        ByteString.copyFromUtf8("c")))
                        .via(NFT_TOKEN_MINT_TXN),
                mintToken(FUNGIBLE_TOKEN, 500L).via(FUNGIBLE_TOKEN_MINT_TXN),
                cryptoTransfer(movingUnique(NON_FUNGIBLE_TOKEN, 1L, 2L, 3L).between(TOKEN_TREASURY, OWNER)),
                cryptoApproveAllowance()
                        .payingWith(OWNER)
                        .addCryptoAllowance(OWNER, SPENDER, 100L)
                        .addCryptoAllowance(OWNER, SECOND_SPENDER, 100L)
                        .addTokenAllowance(OWNER, FUNGIBLE_TOKEN, SPENDER, 100L)
                        .addNftAllowance(OWNER, NON_FUNGIBLE_TOKEN, SPENDER, false, List.of(1L))
                        .fee(ONE_HUNDRED_HBARS),
                getAccountDetails(OWNER)
                        .payingWith(GENESIS)
                        .has(accountDetailsWith()
                                .cryptoAllowancesCount(2)
                                .tokenAllowancesCount(1)
                                .nftApprovedForAllAllowancesCount(0)),
                cryptoCreate(THIRD_SPENDER).balance(ONE_HUNDRED_HBARS),
                cryptoApproveAllowance()
                        .payingWith(OWNER)
                        .addCryptoAllowance(OWNER, ANOTHER_SPENDER, 100L)
                        .fee(ONE_HUNDRED_HBARS)
                        .hasKnownStatus(MAX_ALLOWANCES_EXCEEDED));
    }

    @Order(3)
    @LeakyEmbeddedHapiTest(
            reason = NEEDS_STATE_ACCESS,
            overrides = {"hedera.allowances.maxTransactionLimit", "hedera.allowances.maxAccountLimit"})
    final Stream<DynamicTest> cannotExceedAllowancesTransactionLimit() {
        return hapiTest(
                newKeyNamed(SUPPLY_KEY),
                overridingTwo(
                        "hedera.allowances.maxTransactionLimit", "4",
                        "hedera.allowances.maxAccountLimit", "5"),
                cryptoCreate(OWNER).balance(ONE_HUNDRED_HBARS).maxAutomaticTokenAssociations(10),
                cryptoCreate(SPENDER).balance(ONE_HUNDRED_HBARS),
                cryptoCreate(ANOTHER_SPENDER).balance(ONE_HUNDRED_HBARS),
                cryptoCreate(SECOND_SPENDER).balance(ONE_HUNDRED_HBARS),
                cryptoCreate(TOKEN_TREASURY).balance(100 * ONE_HUNDRED_HBARS).maxAutomaticTokenAssociations(10),
                tokenCreate(FUNGIBLE_TOKEN)
                        .tokenType(FUNGIBLE_COMMON)
                        .supplyType(TokenSupplyType.FINITE)
                        .supplyKey(SUPPLY_KEY)
                        .maxSupply(1000L)
                        .initialSupply(10L)
                        .treasury(TOKEN_TREASURY),
                tokenCreate(NON_FUNGIBLE_TOKEN)
                        .maxSupply(10L)
                        .initialSupply(0)
                        .supplyType(TokenSupplyType.FINITE)
                        .tokenType(NON_FUNGIBLE_UNIQUE)
                        .supplyKey(SUPPLY_KEY)
                        .treasury(TOKEN_TREASURY),
                tokenAssociate(OWNER, FUNGIBLE_TOKEN),
                tokenAssociate(OWNER, NON_FUNGIBLE_TOKEN),
                mintToken(
                                NON_FUNGIBLE_TOKEN,
                                List.of(
                                        ByteString.copyFromUtf8("a"),
                                        ByteString.copyFromUtf8("b"),
                                        ByteString.copyFromUtf8("c")))
                        .via(NFT_TOKEN_MINT_TXN),
                mintToken(FUNGIBLE_TOKEN, 500L).via(FUNGIBLE_TOKEN_MINT_TXN),
                cryptoTransfer(movingUnique(NON_FUNGIBLE_TOKEN, 1L, 2L, 3L).between(TOKEN_TREASURY, OWNER)),
                cryptoApproveAllowance()
                        .payingWith(OWNER)
                        .addCryptoAllowance(OWNER, SPENDER, 100L)
                        .addCryptoAllowance(OWNER, ANOTHER_SPENDER, 100L)
                        .addCryptoAllowance(OWNER, SECOND_SPENDER, 100L)
                        .addTokenAllowance(OWNER, FUNGIBLE_TOKEN, SPENDER, 100L)
                        .addNftAllowance(OWNER, NON_FUNGIBLE_TOKEN, SPENDER, false, List.of(1L))
                        .hasPrecheckFrom(OK, MAX_ALLOWANCES_EXCEEDED)
                        .hasKnownStatus(MAX_ALLOWANCES_EXCEEDED),
                cryptoApproveAllowance()
                        .payingWith(OWNER)
                        .addNftAllowance(OWNER, NON_FUNGIBLE_TOKEN, SPENDER, false, List.of(1L, 1L, 1L, 1L, 1L))
                        .hasPrecheckFrom(OK, MAX_ALLOWANCES_EXCEEDED)
                        .hasKnownStatus(MAX_ALLOWANCES_EXCEEDED),
                cryptoApproveAllowance()
                        .payingWith(OWNER)
                        .addCryptoAllowance(OWNER, SPENDER, 100L)
                        .addCryptoAllowance(OWNER, SPENDER, 200L)
                        .addCryptoAllowance(OWNER, SPENDER, 100L)
                        .addCryptoAllowance(OWNER, SPENDER, 200L)
                        .addCryptoAllowance(OWNER, SPENDER, 200L)
                        .hasPrecheckFrom(OK, MAX_ALLOWANCES_EXCEEDED)
                        .hasKnownStatus(MAX_ALLOWANCES_EXCEEDED),
                cryptoApproveAllowance()
                        .payingWith(OWNER)
                        .addTokenAllowance(OWNER, FUNGIBLE_TOKEN, SPENDER, 100L)
                        .addTokenAllowance(OWNER, FUNGIBLE_TOKEN, SPENDER, 100L)
                        .addTokenAllowance(OWNER, FUNGIBLE_TOKEN, SPENDER, 100L)
                        .addTokenAllowance(OWNER, FUNGIBLE_TOKEN, SPENDER, 100L)
                        .addTokenAllowance(OWNER, FUNGIBLE_TOKEN, SPENDER, 100L)
                        .hasPrecheckFrom(OK, MAX_ALLOWANCES_EXCEEDED)
                        .hasKnownStatus(MAX_ALLOWANCES_EXCEEDED));
    }

    /**
     * Characterize the current behavior of the network when submitting internal txs via a normal payer account.
     * <p>
     * (FUTURE) Revisit this with superuser payer.
     */
    @HapiTest
    final Stream<DynamicTest> internalTxsCannotBeSubmittedByUserAccounts() {
        return hapiTest(
                cryptoCreate(CIVILIAN_PAYER),
                explicit(
                                StateSignatureTransaction,
                                (spec, b) ->
                                        b.setStateSignatureTransaction(com.hedera.hapi.platform.event.legacy
                                                .StateSignatureTransaction.getDefaultInstance()))
                        .payingWith(CIVILIAN_PAYER)
                        .hasPrecheck(BUSY),
                explicit(
                                HintsPreprocessingVote,
                                (spec, b) -> b.setHintsPreprocessingVote(
                                        HintsPreprocessingVoteTransactionBody.getDefaultInstance()))
                        .payingWith(CIVILIAN_PAYER)
                        .hasPrecheck(BUSY),
                explicit(
                                HintsKeyPublication,
                                (spec, b) -> b.setHintsKeyPublication(
                                        HintsKeyPublicationTransactionBody.getDefaultInstance()))
                        .payingWith(CIVILIAN_PAYER)
                        .hasPrecheck(BUSY),
                explicit(
                                HintsPartialSignature,
                                (spec, b) -> b.setHintsPartialSignature(
                                        HintsPartialSignatureTransactionBody.getDefaultInstance()))
                        .payingWith(CIVILIAN_PAYER)
                        .hasPrecheck(BUSY),
                explicit(
                                HistoryProofKeyPublication,
                                (spec, b) -> b.setHistoryProofKeyPublication(
                                        HistoryProofKeyPublicationTransactionBody.getDefaultInstance()))
                        .payingWith(CIVILIAN_PAYER)
                        .hasPrecheck(BUSY),
                explicit(
                                HistoryAssemblySignature,
                                (spec, b) -> b.setHistoryProofSignature(
                                        HistoryProofSignatureTransactionBody.getDefaultInstance()))
                        .payingWith(CIVILIAN_PAYER)
                        .hasPrecheck(BUSY),
                explicit(
                                HistoryProofVote,
                                (spec, b) ->
                                        b.setHistoryProofVote(HistoryProofVoteTransactionBody.getDefaultInstance()))
                        .payingWith(CIVILIAN_PAYER)
                        .hasPrecheck(BUSY),
                explicit(
                                CrsPublication,
                                (spec, b) -> b.setCrsPublication(CrsPublicationTransactionBody.getDefaultInstance()))
                        .payingWith(CIVILIAN_PAYER)
                        .hasPrecheck(BUSY),
                explicit(HookDispatch, (spec, b) -> b.setHookDispatch(HookDispatchTransactionBody.getDefaultInstance()))
                        .payingWith(CIVILIAN_PAYER)
                        .hasPrecheck(BUSY),
                explicit(
                                NodeStakeUpdate,
                                (spec, b) -> b.setNodeStakeUpdate(NodeStakeUpdateTransactionBody.getDefaultInstance()))
                        .payingWith(CIVILIAN_PAYER)
                        .hasPrecheck(BUSY));
    }

    @LeakyEmbeddedHapiTest(
            reason = NEEDS_STATE_ACCESS,
            requirement = FEE_SCHEDULE_OVERRIDES,
            overrides = "fees.simpleFeesEnabled")
    final Stream<DynamicTest> hollowAccountCreationChargesExpectedFees() {
        final long REDUCED_NODE_FEE = 2L;
        final long REDUCED_NETWORK_FEE = 3L;
        final long REDUCED_SERVICE_FEE = 3L;
        final long REDUCED_TOTAL_FEE = REDUCED_NODE_FEE + REDUCED_NETWORK_FEE + REDUCED_SERVICE_FEE;
        final var payer = "payer";
        final var secondKey = "secondKey";
        return hapiTest(
                overriding("fees.simpleFeesEnabled", "false"),
                newKeyNamed(SECP_256K1_SOURCE_KEY).shape(SECP_256K1_SHAPE),
                newKeyNamed(secondKey).shape(SECP_256K1_SHAPE),
                cryptoCreate(payer).balance(0L),
                reduceFeeFor(
                        List.of(CryptoTransfer, CryptoUpdate, CryptoCreate),
                        REDUCED_NODE_FEE,
                        REDUCED_NETWORK_FEE,
                        REDUCED_SERVICE_FEE),
                withOpContext((spec, opLog) -> {
                    // crypto transfer fees check
                    final HapiCryptoTransfer transferToPayerAgain =
                            cryptoTransfer(tinyBarsFromTo(GENESIS, payer, ONE_HUNDRED_HBARS + 2 * REDUCED_TOTAL_FEE));
                    final var secondEvmAddress = ByteString.copyFrom(recoverAddressFromPubKey(spec.registry()
                            .getKey(secondKey)
                            .getECDSASecp256K1()
                            .toByteArray()));
                    // try to create the hollow account without having enough
                    // balance to pay for the finalization (CryptoUpdate) fee
                    final var op5 = cryptoTransfer(tinyBarsFromTo(payer, secondEvmAddress, ONE_HUNDRED_HBARS))
                            .payingWith(payer)
                            .hasKnownStatusFrom(INSUFFICIENT_PAYER_BALANCE, INSUFFICIENT_ACCOUNT_BALANCE)
                            .via(TRANSFER_TXN);
                    final var op5FeeAssertion = getTxnRecord(TRANSFER_TXN)
                            .logged()
                            .exposingTo(record -> {
                                Assertions.assertEquals(REDUCED_TOTAL_FEE, record.getTransactionFee());
                            });
                    final var notExistingAccountInfo =
                            getAliasedAccountInfo(secondKey).hasCostAnswerPrecheck(INVALID_ACCOUNT_ID);
                    // transfer the needed balance for the finalization fee to the
                    // sponsor; we need + 2 * TOTAL_FEE, not 1, since we paid for
                    // the
                    // failed crypto transfer
                    final var op6 = cryptoTransfer(tinyBarsFromTo(GENESIS, payer, 2 * REDUCED_TOTAL_FEE));
                    // now the sponsor can successfully create the hollow account
                    final var op7 = cryptoTransfer(tinyBarsFromTo(payer, secondEvmAddress, ONE_HUNDRED_HBARS))
                            .payingWith(payer)
                            .via(TRANSFER_TXN);
                    final var op7FeeAssertion = getTxnRecord(TRANSFER_TXN)
                            .logged()
                            .andAllChildRecords()
                            .exposingTo(record -> {
                                Assertions.assertEquals(REDUCED_TOTAL_FEE, record.getTransactionFee());
                            });
                    final var op8 = getAliasedAccountInfo(secondKey)
                            .has(accountWith()
                                    .hasEmptyKey()
                                    .expectedBalanceWithChargedUsd(ONE_HUNDRED_HBARS, 0, 0)
                                    .autoRenew(THREE_MONTHS_IN_SECONDS)
                                    .receiverSigReq(false)
                                    .memo(LAZY_MEMO));
                    final var op9 = getAccountBalance(payer).hasTinyBars(0);
                    allRunFor(
                            spec,
                            transferToPayerAgain,
                            op5,
                            op5FeeAssertion,
                            notExistingAccountInfo,
                            op6,
                            op7,
                            op7FeeAssertion,
                            op8,
                            op9);
                }));
    }

    @HapiTest
    @Order(18)
    final Stream<DynamicTest> customFeesHaveExpectedAutoCreateInteractions() {
        final var nftWithRoyaltyNoFallback = "nftWithRoyaltyNoFallback";
        final var nftWithRoyaltyPlusHtsFallback = "nftWithRoyaltyPlusFallback";
        final var nftWithRoyaltyPlusHbarFallback = "nftWithRoyaltyPlusHbarFallback";
        final var ftWithNetOfTransfersFractional = "ftWithNetOfTransfersFractional";
        final var ftWithNonNetOfTransfersFractional = "ftWithNonNetOfTransfersFractional";
        final var finalReceiverKey = "finalReceiverKey";
        final var otherCollector = "otherCollector";
        final var finalTxn = "finalTxn";

        return hapiTest(
                wellKnownTokenEntities(),
                cryptoCreate(otherCollector),
                cryptoCreate(CIVILIAN).maxAutomaticTokenAssociations(42),
                inParallel(
                        createWellKnownFungibleToken(
                                ftWithNetOfTransfersFractional,
                                creation -> creation.withCustom(fractionalFeeNetOfTransfers(
                                        1L, 100L, 1L, OptionalLong.of(5L), TOKEN_TREASURY))),
                        createWellKnownFungibleToken(
                                ftWithNonNetOfTransfersFractional,
                                creation -> creation.withCustom(
                                        fractionalFee(1L, 100L, 1L, OptionalLong.of(5L), TOKEN_TREASURY))),
                        createWellKnownNonFungibleToken(
                                nftWithRoyaltyNoFallback,
                                1,
                                creation -> creation.withCustom(royaltyFeeNoFallback(1L, 100L, TOKEN_TREASURY))),
                        createWellKnownNonFungibleToken(
                                nftWithRoyaltyPlusHbarFallback,
                                1,
                                creation -> creation.withCustom(royaltyFeeWithFallback(
                                        1L, 100L, fixedHbarFeeInheritingRoyaltyCollector(ONE_HBAR), TOKEN_TREASURY)))),
                tokenAssociate(otherCollector, ftWithNonNetOfTransfersFractional),
                createWellKnownNonFungibleToken(
                        nftWithRoyaltyPlusHtsFallback,
                        1,
                        creation -> creation.withCustom(royaltyFeeWithFallback(
                                1L,
                                100L,
                                fixedHtsFeeInheritingRoyaltyCollector(666, ftWithNonNetOfTransfersFractional),
                                otherCollector))),
                autoCreateWithFungible(ftWithNetOfTransfersFractional),
                autoCreateWithFungible(ftWithNonNetOfTransfersFractional),
                autoCreateWithNonFungible(nftWithRoyaltyNoFallback, SUCCESS),
                autoCreateWithNonFungible(
                        nftWithRoyaltyPlusHbarFallback, INSUFFICIENT_SENDER_ACCOUNT_BALANCE_FOR_CUSTOM_FEE),
                newKeyNamed(finalReceiverKey),
                cryptoTransfer(
                        moving(100_000, ftWithNonNetOfTransfersFractional).between(TOKEN_TREASURY, CIVILIAN),
                        movingUnique(nftWithRoyaltyPlusHtsFallback, 1L).between(TOKEN_TREASURY, CIVILIAN)),
                cryptoTransfer(
                                moving(10_000, ftWithNonNetOfTransfersFractional)
                                        .between(CIVILIAN, finalReceiverKey),
                                movingUnique(nftWithRoyaltyPlusHtsFallback, 1L).between(CIVILIAN, finalReceiverKey))
                        .hasKnownStatus(INSUFFICIENT_SENDER_ACCOUNT_BALANCE_FOR_CUSTOM_FEE)
                        .via(finalTxn));
    }

    private long tinybarCostOfGas(final HapiSpec spec, final HederaFunctionality function, final long gasAmount) {
        final var gasThousandthsOfTinycentPrice = spec.fees()
                .getCurrentOpFeeData()
                .get(function)
                .get(DEFAULT)
                .getServicedata()
                .getGas();
        final var rates = spec.ratesProvider().rates();
        return (gasThousandthsOfTinycentPrice / 1000 * rates.getHbarEquiv()) / rates.getCentEquiv() * gasAmount;
    }

    private HapiSpecOperation autoCreateWithFungible(final String token) {
        final var keyName = VALID_ALIAS + "-" + token;
        final var txn = "autoCreationVia" + token;
        return blockingOrder(
                newKeyNamed(keyName),
                cryptoTransfer(moving(100_000, token).between(TOKEN_TREASURY, CIVILIAN)),
                cryptoTransfer(moving(10_000, token).between(CIVILIAN, keyName)).via(txn),
                getTxnRecord(txn).assertingKnownEffectivePayers());
    }

    private HapiSpecOperation autoCreateWithNonFungible(final String token, final ResponseCodeEnum expectedStatus) {
        final var keyName = VALID_ALIAS + "-" + token;
        final var txn = "autoCreationVia" + token;
        return blockingOrder(
                newKeyNamed(keyName),
                cryptoTransfer(movingUnique(token, 1L).between(TOKEN_TREASURY, CIVILIAN)),
                cryptoTransfer(movingUnique(token, 1L).between(CIVILIAN, keyName))
                        .via(txn)
                        .hasKnownStatus(expectedStatus),
                getTxnRecord(txn).assertingKnownEffectivePayers());
    }
}
