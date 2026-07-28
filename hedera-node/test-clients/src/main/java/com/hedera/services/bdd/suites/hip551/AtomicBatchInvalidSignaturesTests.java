// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.suites.hip551;

import static com.google.protobuf.ByteString.copyFromUtf8;
import static com.hedera.services.bdd.junit.TestTags.ATOMIC_BATCH;
import static com.hedera.services.bdd.spec.HapiSpec.hapiTest;
import static com.hedera.services.bdd.spec.assertions.TransactionRecordAsserts.recordWith;
import static com.hedera.services.bdd.spec.keys.KeyShape.PREDEFINED_SHAPE;
import static com.hedera.services.bdd.spec.keys.KeyShape.sigs;
import static com.hedera.services.bdd.spec.queries.QueryVerbs.getAccountBalance;
import static com.hedera.services.bdd.spec.queries.QueryVerbs.getTokenInfo;
import static com.hedera.services.bdd.spec.queries.QueryVerbs.getTokenNftInfo;
import static com.hedera.services.bdd.spec.queries.QueryVerbs.getTxnRecord;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.atomicBatch;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.cryptoCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.cryptoTransfer;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.grantTokenKyc;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.mintToken;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.tokenAssociate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.tokenCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.tokenUpdate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.tokenUpdateNfts;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.wipeTokenAccount;
import static com.hedera.services.bdd.spec.transactions.token.CustomFeeSpecs.fractionalFee;
import static com.hedera.services.bdd.spec.transactions.token.TokenMovement.moving;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.newKeyNamed;
import static com.hedera.services.bdd.suites.HapiSuite.DEFAULT_PAYER;
import static com.hedera.services.bdd.suites.HapiSuite.GENESIS;
import static com.hedera.services.bdd.suites.HapiSuite.ONE_HBAR;
import static com.hedera.services.bdd.suites.HapiSuite.ONE_HUNDRED_HBARS;
import static com.hedera.services.bdd.suites.HapiSuite.ONE_MILLION_HBARS;
import static com.hedera.services.bdd.suites.HapiSuite.THREE_MONTHS_IN_SECONDS;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.CANNOT_WIPE_TOKEN_TREASURY_ACCOUNT;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.INNER_TRANSACTION_FAILED;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.INVALID_SIGNATURE;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.TOKEN_HAS_NO_KYC_KEY;
import static com.hederahashgraph.api.proto.java.TokenType.NON_FUNGIBLE_UNIQUE;

import com.hedera.services.bdd.junit.HapiTest;
import com.hedera.services.bdd.junit.HapiTestLifecycle;
import com.hedera.services.bdd.junit.support.TestLifecycle;
import com.hedera.services.bdd.spec.keys.KeyShape;
import com.hederahashgraph.api.proto.java.TokenSupplyType;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import java.util.OptionalLong;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;

@Tag(ATOMIC_BATCH)
@HapiTestLifecycle
class AtomicBatchInvalidSignaturesTests {

    private static final String TOKEN_TREASURY = "treasury";
    private static final String DEFAULT_BATCH_OPERATOR = "DEFAULT_BATCH_OPERATOR";

    @BeforeAll
    static void beforeAll(@NonNull final TestLifecycle testLifecycle) {
        testLifecycle.doAdhoc(cryptoCreate(DEFAULT_BATCH_OPERATOR).balance(ONE_MILLION_HBARS));
    }

    @Nested
    @DisplayName("Contract Association Batch Tests")
    class ContractAssociationBatch {}

    @Nested
    @DisplayName("Token Creation Batch Tests")
    class TokenCreationBatchTests {

        @HapiTest
        @DisplayName("Batch with invalid token create transactions")
        Stream<DynamicTest> batchWithInvalidTokenCreateTransactions() {
            final var batchOperator = "batchOperator";
            final var alice = "ALICE";
            final var aliceKey1 = "aliceKey1";
            final var aliceKey2 = "aliceKey2";
            final var aliceThresholdKey = "aliceThresholdKey";
            final var invalidTokenTxnId = "invalidTokenTxnId";
            final var invalidSigTokenTxnId = "invalidSigTokenTxnId";

            return hapiTest(
                    cryptoCreate(batchOperator).balance(ONE_HUNDRED_HBARS),
                    newKeyNamed(aliceKey1),
                    newKeyNamed(aliceKey2),
                    newKeyNamed(aliceThresholdKey)
                            .shape(KeyShape.threshOf(2, PREDEFINED_SHAPE, PREDEFINED_SHAPE)
                                    .signedWith(sigs(aliceKey1, aliceKey2))),
                    cryptoCreate(alice).balance(ONE_HUNDRED_HBARS).key(aliceThresholdKey),

                    // Batch with valid token but missing admin key signature
                    atomicBatch(tokenCreate("validToken")
                                    .adminKey(aliceThresholdKey)
                                    .via(invalidTokenTxnId)
                                    .batchKey(batchOperator)
                                    .hasKnownStatus(INVALID_SIGNATURE))
                            .signedByPayerAnd(batchOperator, aliceKey1) // Missing aliceKey2 for 2/2 threshold
                            .via("invalidNameBatch")
                            .hasKnownStatus(INNER_TRANSACTION_FAILED),

                    // Verify inner transaction failed signature check
                    getTxnRecord(invalidTokenTxnId)
                            .logged()
                            .hasPriority(recordWith().status(INVALID_SIGNATURE)),

                    // Batch with missing treasury signature (threshold key)
                    atomicBatch(tokenCreate("invalidSigToken")
                                    .treasury(alice)
                                    .via(invalidSigTokenTxnId)
                                    .batchKey(batchOperator)
                                    .hasKnownStatus(INVALID_SIGNATURE))
                            .signedByPayerAnd(batchOperator, aliceKey1) // Missing aliceKey2 for 2/2 threshold treasury
                            .via("invalidSigBatch")
                            .hasKnownStatus(INNER_TRANSACTION_FAILED),

                    // Verify inner transaction failed signature check
                    getTxnRecord(invalidTokenTxnId)
                            .logged()
                            .hasPriority(recordWith().status(INVALID_SIGNATURE)));
        }

        @HapiTest
        @DisplayName("Batch with missing treasury signature failure")
        Stream<DynamicTest> batchWithMissingTreasurySignature() {
            final var batchOperator = "batchOperator";
            final var tokenName = "PRIMARY";
            final var tokenCreateTxnId = "tokenCreateTxnId";
            final var treasuryKey1 = "treasuryKey1";
            final var treasuryKey2 = "treasuryKey2";
            final var treasuryThresholdKey = "treasuryThresholdKey";

            return hapiTest(
                    cryptoCreate(batchOperator).balance(ONE_HUNDRED_HBARS),
                    newKeyNamed(treasuryKey1),
                    newKeyNamed(treasuryKey2),
                    newKeyNamed(treasuryThresholdKey)
                            .shape(KeyShape.threshOf(2, PREDEFINED_SHAPE, PREDEFINED_SHAPE)
                                    .signedWith(sigs(treasuryKey1, treasuryKey2))),
                    cryptoCreate(TOKEN_TREASURY).balance(ONE_HUNDRED_HBARS).key(treasuryThresholdKey),

                    // Batch that should fail due to missing treasury threshold signature
                    atomicBatch(tokenCreate(tokenName)
                                    .treasury(TOKEN_TREASURY)
                                    .via(tokenCreateTxnId)
                                    .batchKey(batchOperator)
                                    .hasKnownStatus(INVALID_SIGNATURE))
                            .signedByPayerAnd(batchOperator, treasuryKey1) // Missing treasuryKey2 for 2/2 threshold
                            .via("treasuryBatch")
                            .hasKnownStatus(INNER_TRANSACTION_FAILED),

                    // Verify inner transaction failed signature check
                    getTxnRecord(tokenCreateTxnId)
                            .logged()
                            .hasPriority(recordWith().status(INVALID_SIGNATURE)));
        }

        @HapiTest
        @DisplayName("Batch with fee collector signing requirements")
        Stream<DynamicTest> batchFeeCollectorSigningRequirements() {
            final var batchOperator = "batchOperator";
            final var customFeesKey = "customFeesKey";
            final var htsCollector = "htsCollector";
            final var hbarCollector = "hbarCollector";
            final var tokenCollector = "tokenCollector";
            final var feeDenom = "feeDenom";
            final var token = "token";
            final var numerator = 1L;
            final var minimumToCollect = 1L;
            final var maximumToCollect = 10L;
            final var token1TxnId = "token1TxnId";

            return hapiTest(
                    cryptoCreate(batchOperator).balance(ONE_HUNDRED_HBARS),
                    newKeyNamed(customFeesKey),
                    cryptoCreate(htsCollector).receiverSigRequired(true),
                    cryptoCreate(hbarCollector),
                    cryptoCreate(tokenCollector),
                    cryptoCreate(TOKEN_TREASURY),
                    tokenCreate(feeDenom).treasury(htsCollector),

                    // Batch with invalid fee collector signature - should fail
                    atomicBatch(tokenCreate(token)
                                    .treasury(TOKEN_TREASURY)
                                    .withCustom(fractionalFee(
                                            numerator,
                                            0,
                                            minimumToCollect,
                                            OptionalLong.of(maximumToCollect),
                                            tokenCollector))
                                    .via(token1TxnId)
                                    .batchKey(batchOperator)
                                    .hasKnownStatus(INVALID_SIGNATURE))
                            .signedByPayerAnd(
                                    batchOperator, DEFAULT_PAYER, TOKEN_TREASURY) // Missing tokenCollector signature
                            .via("feeCollectorBatch")
                            .hasKnownStatus(INNER_TRANSACTION_FAILED),

                    // Verify inner transaction failed signature check
                    getTxnRecord(token1TxnId).hasPriority(recordWith().status(INVALID_SIGNATURE)));
        }
    }

    @Nested
    @DisplayName("Token Management Batch Tests")
    class TokenManagementBatchTests {

        @HapiTest
        @DisplayName("Batch with token wipe failure cases")
        Stream<DynamicTest> batchWithTokenWipeFailures() {
            final var batchOperator = "batchOperator";
            final var unwipeableToken = "without";
            final var wipeableToken = "with";
            final var wipeableUniqueToken = "uniqueWith";
            final var anotherWipeableToken = "anotherWith";
            final var multiKey = "wipeAndSupplyKey";
            final var someMeta = copyFromUtf8("HEY");
            final var wipe1TxnId = "wipe1TxnId";

            return hapiTest(
                    cryptoCreate(batchOperator).balance(ONE_HUNDRED_HBARS),
                    newKeyNamed(multiKey),
                    cryptoCreate("misc").balance(ONE_HUNDRED_HBARS),
                    cryptoCreate(TOKEN_TREASURY).balance(ONE_HUNDRED_HBARS),
                    tokenCreate(unwipeableToken).treasury(TOKEN_TREASURY),
                    tokenCreate(wipeableToken).treasury(TOKEN_TREASURY).wipeKey(multiKey),
                    tokenCreate(wipeableUniqueToken)
                            .tokenType(NON_FUNGIBLE_UNIQUE)
                            .supplyKey(multiKey)
                            .initialSupply(0L)
                            .treasury(TOKEN_TREASURY)
                            .wipeKey(multiKey),
                    mintToken(wipeableUniqueToken, List.of(someMeta)),
                    tokenCreate(anotherWipeableToken)
                            .treasury(TOKEN_TREASURY)
                            .initialSupply(1_000)
                            .wipeKey(multiKey),
                    tokenAssociate("misc", anotherWipeableToken),
                    cryptoTransfer(moving(500, anotherWipeableToken).between(TOKEN_TREASURY, "misc")),
                    // Verify initial setup
                    getAccountBalance("misc").hasTokenBalance(anotherWipeableToken, 500),

                    // Batch with treasury wipe attempt - should fail entire batch
                    atomicBatch(wipeTokenAccount(wipeableUniqueToken, TOKEN_TREASURY, List.of(1L))
                                    .via(wipe1TxnId)
                                    .batchKey(batchOperator)
                                    .hasKnownStatus(CANNOT_WIPE_TOKEN_TREASURY_ACCOUNT))
                            .signedByPayerAnd(batchOperator)
                            .via("wipeBatch")
                            .hasKnownStatus(INNER_TRANSACTION_FAILED),

                    // Verify inner transaction failed
                    getTxnRecord(wipe1TxnId).hasPriority(recordWith().status(CANNOT_WIPE_TOKEN_TREASURY_ACCOUNT)),

                    // Verify token balances unchanged (atomicity check)
                    getAccountBalance("misc").hasTokenBalance(anotherWipeableToken, 500));
        }

        @HapiTest
        @DisplayName("Batch with KYC management failure cases")
        Stream<DynamicTest> batchWithKycManagementFailures() {
            final var batchOperator = "batchOperator";
            final var withoutKycKey = "withoutKycKey";
            final var withKycKey = "withKycKey";
            final String ONE_KYC = "oneKyc";
            final var kyc1TxnId = "kyc1TxnId";

            return hapiTest(
                    cryptoCreate(batchOperator).balance(ONE_HUNDRED_HBARS),
                    newKeyNamed(ONE_KYC),
                    cryptoCreate(TOKEN_TREASURY).balance(ONE_HUNDRED_HBARS),
                    tokenCreate(withoutKycKey).treasury(TOKEN_TREASURY),
                    tokenCreate(withKycKey).kycKey(ONE_KYC).treasury(TOKEN_TREASURY),
                    // Verify initial setup
                    getTokenInfo(withoutKycKey).logged(),
                    getTokenInfo(withKycKey).logged(),

                    // Batch with KYC grant on token without KYC key - should fail entire batch
                    atomicBatch(grantTokenKyc(withoutKycKey, TOKEN_TREASURY)
                                    .via(kyc1TxnId)
                                    .batchKey(batchOperator)
                                    .hasKnownStatus(TOKEN_HAS_NO_KYC_KEY))
                            .signedByPayerAnd(batchOperator, GENESIS)
                            .via("kycBatch")
                            .hasKnownStatus(INNER_TRANSACTION_FAILED),

                    // Verify inner transaction failed
                    getTxnRecord(kyc1TxnId).hasPriority(recordWith().status(TOKEN_HAS_NO_KYC_KEY)));
        }
    }

    @Nested
    @DisplayName("Token Update Batch Tests")
    class TokenUpdateBatchTests {
        private static final String SUPPLY_KEY = "supplyKey";
        private static final String METADATA_KEY = "metadataKey";
        private static final String NFT_TEST_METADATA = " test metadata";
        private static final String RECEIVER = "receiver";

        @HapiTest
        @DisplayName("Batch fails when NFT metadata update lacks required signatures")
        Stream<DynamicTest> batchFailsWithoutMetadataKeySignature() {
            final var batchOperator = "batchOperator";
            final var nftToken = "nftToken";
            final var updateTxnId = "updateTxnId";

            return hapiTest(
                    cryptoCreate(batchOperator).balance(ONE_HUNDRED_HBARS),
                    newKeyNamed(SUPPLY_KEY),
                    newKeyNamed(METADATA_KEY),
                    cryptoCreate(TOKEN_TREASURY).balance(ONE_HUNDRED_HBARS).maxAutomaticTokenAssociations(4),
                    cryptoCreate(RECEIVER).balance(ONE_HUNDRED_HBARS).maxAutomaticTokenAssociations(4),
                    tokenCreate(nftToken)
                            .supplyType(TokenSupplyType.FINITE)
                            .tokenType(NON_FUNGIBLE_UNIQUE)
                            .treasury(TOKEN_TREASURY)
                            .maxSupply(12L)
                            .supplyKey(SUPPLY_KEY)
                            .metadataKey(METADATA_KEY)
                            .initialSupply(0L),
                    tokenAssociate(RECEIVER, nftToken),
                    mintToken(nftToken, List.of(copyFromUtf8("a"), copyFromUtf8("b"))),
                    // Verify initial setup
                    getTokenNftInfo(nftToken, 1L).hasMetadata(copyFromUtf8("a")),

                    // Batch with invalid NFT metadata update - should fail entire batch
                    atomicBatch(tokenUpdateNfts(nftToken, NFT_TEST_METADATA, List.of(1L))
                                    .via(updateTxnId)
                                    .batchKey(batchOperator)
                                    .payingWith(TOKEN_TREASURY)
                                    .fee(10 * ONE_HBAR) // Missing METADATA_KEY signature - INVALID_SIGNATURE
                                    .hasKnownStatus(INVALID_SIGNATURE))
                            .signedByPayerAnd(batchOperator)
                            .via("metadataBatch")
                            .hasKnownStatus(INNER_TRANSACTION_FAILED),

                    // Verify batch failed and check transaction records
                    getTxnRecord(updateTxnId).hasPriority(recordWith().status(INVALID_SIGNATURE)),

                    // Verify NFT metadata unchanged
                    getTokenNftInfo(nftToken, 1L).hasMetadata(copyFromUtf8("a")));
        }

        @HapiTest
        @DisplayName("Batch with auto renew account update signature requirements")
        Stream<DynamicTest> batchWithAutoRenewAccountSignatureRequirement() {
            final var batchOperator = "batchOperator";
            final var tokenName = "autoRenewToken";
            final var secondPeriod = THREE_MONTHS_IN_SECONDS + 1234;
            final var updateTxnId = "updateTxnId";
            final var adminKey1 = "adminKey1";
            final var adminKey2 = "adminKey2";
            final var thresholdAdminKey = "thresholdAdminKey";

            return hapiTest(
                    cryptoCreate(batchOperator).balance(ONE_HUNDRED_HBARS),
                    cryptoCreate("autoRenew").balance(ONE_HUNDRED_HBARS),
                    cryptoCreate("newAutoRenew").balance(ONE_HUNDRED_HBARS),
                    newKeyNamed(adminKey1),
                    newKeyNamed(adminKey2),
                    newKeyNamed(thresholdAdminKey)
                            .shape(KeyShape.threshOf(2, PREDEFINED_SHAPE, PREDEFINED_SHAPE)
                                    .signedWith(sigs(adminKey1, adminKey2))),
                    tokenCreate(tokenName)
                            .adminKey(thresholdAdminKey)
                            .autoRenewAccount("autoRenew")
                            .autoRenewPeriod(THREE_MONTHS_IN_SECONDS)
                            .signedBy(DEFAULT_PAYER, "autoRenew", adminKey1, adminKey2),
                    // Verify initial setup
                    getTokenInfo(tokenName).hasAutoRenewAccount("autoRenew"),

                    // Batch with invalid token update (missing one required signature from threshold key)
                    atomicBatch(tokenUpdate(tokenName)
                                    .autoRenewAccount("newAutoRenew")
                                    .autoRenewPeriod(secondPeriod)
                                    .via(updateTxnId)
                                    .batchKey(batchOperator)
                                    .hasKnownStatus(INVALID_SIGNATURE))
                            .signedByPayerAnd(batchOperator, adminKey1) // Missing adminKey2 - need 2/2 threshold
                            .via("autoRenewBatch")
                            .hasKnownStatus(INNER_TRANSACTION_FAILED),

                    // Verify inner transaction failed signature check
                    getTxnRecord(updateTxnId).hasPriority(recordWith().status(INVALID_SIGNATURE)),

                    // Verify auto renew account unchanged
                    getTokenInfo(tokenName).hasAutoRenewAccount("autoRenew"));
        }
    }
}
