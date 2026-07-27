// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.suites.file;

import static com.hedera.services.bdd.junit.ContextRequirement.PERMISSION_OVERRIDES;
import static com.hedera.services.bdd.junit.ContextRequirement.UPGRADE_FILE_CONTENT;
import static com.hedera.services.bdd.junit.EmbeddedReason.NEEDS_STATE_ACCESS;
import static com.hedera.services.bdd.spec.HapiSpec.hapiTest;
import static com.hedera.services.bdd.spec.queries.QueryVerbs.contractCallLocal;
import static com.hedera.services.bdd.spec.queries.QueryVerbs.getAccountInfo;
import static com.hedera.services.bdd.spec.queries.QueryVerbs.getFileContents;
import static com.hedera.services.bdd.spec.queries.QueryVerbs.getFileInfo;
import static com.hedera.services.bdd.spec.queries.QueryVerbs.getTxnRecord;
import static com.hedera.services.bdd.spec.queries.crypto.ExpectedTokenRel.relationshipWith;
import static com.hedera.services.bdd.spec.transactions.TxnUtils.BYTES_4K;
import static com.hedera.services.bdd.spec.transactions.TxnUtils.randomUtf8Bytes;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.contractCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.createTopic;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.cryptoCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.cryptoTransfer;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.fileCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.fileUpdate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.scheduleCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.submitMessageTo;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.tokenAssociate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.tokenCreate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.tokenDissociate;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.uploadInitCode;
import static com.hedera.services.bdd.spec.transactions.crypto.HapiCryptoTransfer.tinyBarsFromTo;
import static com.hedera.services.bdd.spec.transactions.token.CustomFeeSpecs.fixedHbarFee;
import static com.hedera.services.bdd.spec.transactions.token.CustomFeeSpecs.fixedHtsFee;
import static com.hedera.services.bdd.spec.utilops.CustomSpecAssert.allRunFor;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.doWithStartupConfig;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.newKeyNamed;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.overriding;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.overridingAllOf;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.submitModified;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.updateSpecialFile;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.withOpContext;
import static com.hedera.services.bdd.spec.utilops.mod.ModificationUtils.withSuccessivelyVariedBodyIds;
import static com.hedera.services.bdd.suites.HapiSuite.ADDRESS_BOOK_CONTROL;
import static com.hedera.services.bdd.suites.HapiSuite.API_PERMISSIONS;
import static com.hedera.services.bdd.suites.HapiSuite.DEFAULT_PAYER;
import static com.hedera.services.bdd.suites.HapiSuite.FUNDING;
import static com.hedera.services.bdd.suites.HapiSuite.GENESIS;
import static com.hedera.services.bdd.suites.HapiSuite.ONE_HUNDRED_HBARS;
import static com.hedera.services.bdd.suites.HapiSuite.ZERO_BYTE_MEMO;
import static com.hedera.services.bdd.suites.HapiSuite.flattened;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.AUTORENEW_DURATION_NOT_IN_RANGE;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.BUSY;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.CUSTOM_FEES_LIST_TOO_LONG;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.INVALID_TOKEN_ID;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.INVALID_ZERO_BYTE_IN_STRING;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.MAX_ENTITIES_IN_PRICE_REGIME_HAVE_BEEN_CREATED;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.MAX_GAS_LIMIT_EXCEEDED;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.MESSAGE_SIZE_TOO_LARGE;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.NOT_SUPPORTED;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.OK;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.SUCCESS;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.TOKEN_ALREADY_ASSOCIATED_TO_ACCOUNT;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.TOKEN_ID_REPEATED_IN_TOKEN_LIST;
import static com.hederahashgraph.api.proto.java.ResponseCodeEnum.UNAUTHORIZED;
import static com.hederahashgraph.api.proto.java.TokenFreezeStatus.FreezeNotApplicable;
import static com.hederahashgraph.api.proto.java.TokenFreezeStatus.Frozen;
import static com.hederahashgraph.api.proto.java.TokenFreezeStatus.Unfrozen;
import static com.hederahashgraph.api.proto.java.TokenKycStatus.KycNotApplicable;
import static com.hederahashgraph.api.proto.java.TokenKycStatus.Revoked;
import static java.lang.Long.parseLong;

import com.google.protobuf.ByteString;
import com.hedera.services.bdd.junit.HapiTest;
import com.hedera.services.bdd.junit.LeakyEmbeddedHapiTest;
import com.hedera.services.bdd.spec.keys.SigControl;
import com.hedera.services.bdd.spec.transactions.TxnUtils;
import com.hedera.services.bdd.suites.token.TokenAssociationSpecs;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.base.utility.CommonUtils;
import org.junit.jupiter.api.DynamicTest;

@SuppressWarnings("java:S1192")
public class FileUpdateSuite {
    private static final Logger log = LogManager.getLogger(FileUpdateSuite.class);
    private static final String CONTRACT = "CreateTrivial";
    private static final String CREATE_TXN = "create";
    public static final String INSERT_ABI = "insert";
    private static final String INDIRECT_GET_ABI = "getIndirect";
    private static final String CHAIN_ID_GET_ABI = "getChainID";
    private static final String INVALID_ENTITY_ID = "1.2.3";
    public static final String CIVILIAN = "civilian";
    public static final String TEST_TOPIC = "testTopic";

    @HapiTest
    final Stream<DynamicTest> idVariantsTreatedAsExpected() {
        return hapiTest(
                fileCreate("file").contents("ABC"),
                submitModified(withSuccessivelyVariedBodyIds(), () -> fileUpdate("file")
                        .contents("DEF")));
    }

    @HapiTest
    final Stream<DynamicTest> associateHasExpectedSemantics() {
        return hapiTest(flattened(
                TokenAssociationSpecs.basicKeysAndTokens(),
                cryptoCreate("misc").balance(0L),
                tokenAssociate("misc", TokenAssociationSpecs.FREEZABLE_TOKEN_ON_BY_DEFAULT),
                tokenAssociate("misc", TokenAssociationSpecs.FREEZABLE_TOKEN_ON_BY_DEFAULT)
                        .hasKnownStatus(TOKEN_ALREADY_ASSOCIATED_TO_ACCOUNT),
                tokenAssociate("misc", INVALID_ENTITY_ID).hasKnownStatus(INVALID_TOKEN_ID),
                tokenAssociate("misc", INVALID_ENTITY_ID, INVALID_ENTITY_ID)
                        .hasPrecheck(TOKEN_ID_REPEATED_IN_TOKEN_LIST),
                tokenDissociate("misc", INVALID_ENTITY_ID, INVALID_ENTITY_ID)
                        .hasPrecheck(TOKEN_ID_REPEATED_IN_TOKEN_LIST),
                tokenAssociate("misc", TokenAssociationSpecs.FREEZABLE_TOKEN_OFF_BY_DEFAULT),
                tokenAssociate("misc", TokenAssociationSpecs.KNOWABLE_TOKEN, TokenAssociationSpecs.VANILLA_TOKEN),
                getAccountInfo("misc")
                        .hasToken(relationshipWith(TokenAssociationSpecs.FREEZABLE_TOKEN_ON_BY_DEFAULT)
                                .kyc(KycNotApplicable)
                                .freeze(Frozen))
                        .hasToken(relationshipWith(TokenAssociationSpecs.FREEZABLE_TOKEN_OFF_BY_DEFAULT)
                                .kyc(KycNotApplicable)
                                .freeze(Unfrozen))
                        .hasToken(relationshipWith(TokenAssociationSpecs.KNOWABLE_TOKEN)
                                .kyc(Revoked)
                                .freeze(FreezeNotApplicable))
                        .hasToken(relationshipWith(TokenAssociationSpecs.VANILLA_TOKEN)
                                .kyc(KycNotApplicable)
                                .freeze(FreezeNotApplicable))
                        .logged()));
    }

    @LeakyEmbeddedHapiTest(
            reason = NEEDS_STATE_ACCESS,
            overrides = {"tokens.maxCustomFeesAllowed"})
    final Stream<DynamicTest> notTooManyFeeScheduleCanBeCreated() {
        final var denom = "fungible";
        final var token = "token";
        return hapiTest(
                overriding("tokens.maxCustomFeesAllowed", "1"),
                tokenCreate(denom),
                tokenCreate(token)
                        .treasury(DEFAULT_PAYER)
                        .withCustom(fixedHbarFee(1, DEFAULT_PAYER))
                        .withCustom(fixedHtsFee(1, denom, DEFAULT_PAYER))
                        .hasKnownStatus(CUSTOM_FEES_LIST_TOO_LONG));
    }

    @LeakyEmbeddedHapiTest(reason = NEEDS_STATE_ACCESS, requirement = UPGRADE_FILE_CONTENT)
    final Stream<DynamicTest> optimisticSpecialFileUpdate() {
        final var appendsPerBurst = 128;
        final var specialFile = "159";
        final var contents = randomUtf8Bytes(64 * BYTES_4K);
        final var specialFileContents = ByteString.copyFrom(contents);
        final byte[] expectedHash;
        try {
            expectedHash = MessageDigest.getInstance("SHA-384").digest(contents);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        return hapiTest(
                updateSpecialFile(GENESIS, specialFile, specialFileContents, BYTES_4K, appendsPerBurst),
                getFileInfo(specialFile).hasMemo(CommonUtils.hex(expectedHash)));
    }

    @LeakyEmbeddedHapiTest(reason = NEEDS_STATE_ACCESS, requirement = PERMISSION_OVERRIDES)
    final Stream<DynamicTest> apiPermissionsChangeDynamically() {
        final var civilian = CIVILIAN;
        return hapiTest(
                cryptoTransfer(tinyBarsFromTo(GENESIS, ADDRESS_BOOK_CONTROL, 1_000_000_000L)),
                cryptoCreate(civilian).balance(ONE_HUNDRED_HBARS),
                getFileContents(API_PERMISSIONS).logged(),
                tokenCreate("poc").payingWith(civilian),
                fileUpdate(API_PERMISSIONS)
                        .payingWith(ADDRESS_BOOK_CONTROL)
                        .overridingProps(Map.of("tokenCreate", "0-1")),
                getFileContents(API_PERMISSIONS).logged(),
                tokenCreate("poc")
                        .payingWith(civilian)
                        .hasPrecheckFrom(NOT_SUPPORTED, OK)
                        .hasKnownStatus(UNAUTHORIZED),
                fileUpdate(API_PERMISSIONS)
                        .payingWith(ADDRESS_BOOK_CONTROL)
                        .overridingProps(Map.of("tokenCreate", "0-*")),
                tokenCreate("secondPoc").payingWith(civilian));
    }

    @HapiTest
    final Stream<DynamicTest> updateFeesCompatibleWithCreates() {
        final long origLifetime = 7_200_000L;
        final long extension = 700_000L;
        final byte[] old2k = randomUtf8Bytes(BYTES_4K / 2);
        final byte[] new4k = randomUtf8Bytes(BYTES_4K);
        final byte[] new2k = randomUtf8Bytes(BYTES_4K / 2);

        return hapiTest(
                fileCreate("test").contents(old2k).lifetime(origLifetime).via(CREATE_TXN),
                fileUpdate("test").contents(new4k).extendingExpiryBy(0).via("updateTo4"),
                fileUpdate("test").contents(new2k).extendingExpiryBy(0).via("updateTo2"),
                fileUpdate("test").extendingExpiryBy(extension).via("extend"),
                withOpContext((spec, opLog) -> {
                    final var createOp = getTxnRecord(CREATE_TXN);
                    final var to4kOp = getTxnRecord("updateTo4");
                    final var to2kOp = getTxnRecord("updateTo2");
                    final var extensionOp = getTxnRecord("extend");
                    allRunFor(spec, createOp, to4kOp, to2kOp, extensionOp);
                    final var createFee = createOp.getResponseRecord().getTransactionFee();
                    opLog.info("Creation : {} ", createFee);
                    opLog.info(
                            "New 4k   : {} ({})",
                            to4kOp.getResponseRecord().getTransactionFee(),
                            (to4kOp.getResponseRecord().getTransactionFee() - createFee));
                    opLog.info(
                            "New 2k   : {} ({})",
                            to2kOp.getResponseRecord().getTransactionFee(),
                            +(to2kOp.getResponseRecord().getTransactionFee() - createFee));
                    opLog.info(
                            "Extension: {} ({})",
                            extensionOp.getResponseRecord().getTransactionFee(),
                            (extensionOp.getResponseRecord().getTransactionFee() - createFee));
                }));
    }

    @HapiTest
    final Stream<DynamicTest> vanillaUpdateSucceeds() {
        final byte[] old4K = randomUtf8Bytes(BYTES_4K);
        final byte[] new4k = randomUtf8Bytes(BYTES_4K);
        final String firstMemo = "Originally";
        final String secondMemo = "Subsequently";

        return hapiTest(
                fileCreate("test").entityMemo(firstMemo).contents(old4K),
                fileUpdate("test").entityMemo(ZERO_BYTE_MEMO).contents(new4k).hasPrecheck(INVALID_ZERO_BYTE_IN_STRING),
                fileUpdate("test").entityMemo(secondMemo).contents(new4k),
                getFileContents("test").hasContents(ignore -> new4k),
                getFileInfo("test").hasMemo(secondMemo));
    }

    @HapiTest
    final Stream<DynamicTest> cannotUpdateImmutableFile() {
        final String file1 = "FILE_1";
        final String file2 = "FILE_2";
        return hapiTest(
                fileCreate(file1).contents("Hello World").unmodifiable(),
                fileCreate(file2).contents("Hello World").waclShape(SigControl.emptyList()),
                fileUpdate(file1)
                        .contents("Goodbye World")
                        .signedBy(DEFAULT_PAYER)
                        .hasKnownStatus(UNAUTHORIZED),
                fileUpdate(file2)
                        .contents("Goodbye World")
                        .signedBy(DEFAULT_PAYER)
                        .hasKnownStatus(UNAUTHORIZED));
    }

    @HapiTest
    final Stream<DynamicTest> cannotUpdateExpirationPastMaxLifetime() {
        return hapiTest(
                fileCreate("test"), doWithStartupConfig("entities.maxLifetime", maxLifetime -> fileUpdate("test")
                        .lifetime(parseLong(maxLifetime) + 12_345L)
                        .hasPrecheck(AUTORENEW_DURATION_NOT_IN_RANGE)));
    }

    // C.f. https://github.com/hashgraph/hedera-services/pull/8908

    @LeakyEmbeddedHapiTest(
            reason = NEEDS_STATE_ACCESS,
            overrides = {"contracts.maxGasPerSec"})
    final Stream<DynamicTest> gasLimitOverMaxGasLimitFailsPrecheck() {
        return hapiTest(
                uploadInitCode(CONTRACT),
                contractCreate(CONTRACT).gas(1_000_000L),
                overriding("contracts.maxGasPerSec", "100"),
                contractCallLocal(CONTRACT, INDIRECT_GET_ABI)
                        .gas(101L)
                        // for some reason BUSY is returned in CI
                        .hasCostAnswerPrecheckFrom(MAX_GAS_LIMIT_EXCEEDED, BUSY));
    }

    @LeakyEmbeddedHapiTest(
            reason = NEEDS_STATE_ACCESS,
            overrides = {
                "accounts.maxNumber",
                "contracts.maxNumber",
                "files.maxNumber",
                "scheduling.maxNumber",
                "tokens.maxNumber",
                "topics.maxNumber"
            })
    final Stream<DynamicTest> entitiesNotCreatableAfterUsageLimitsReached() {
        final var notToBe = "ne'erToBe";
        return hapiTest(
                overridingAllOf(Map.of(
                        "accounts.maxNumber", "0",
                        "contracts.maxNumber", "0",
                        "files.maxNumber", "0",
                        "scheduling.maxNumber", "0",
                        "tokens.maxNumber", "0",
                        "topics.maxNumber", "0")),
                cryptoCreate(notToBe).hasKnownStatus(MAX_ENTITIES_IN_PRICE_REGIME_HAVE_BEEN_CREATED),
                fileCreate(notToBe).contents("NOPE").hasKnownStatus(MAX_ENTITIES_IN_PRICE_REGIME_HAVE_BEEN_CREATED),
                scheduleCreate(notToBe, cryptoTransfer(tinyBarsFromTo(DEFAULT_PAYER, FUNDING, 1)))
                        .hasKnownStatus(MAX_ENTITIES_IN_PRICE_REGIME_HAVE_BEEN_CREATED),
                tokenCreate(notToBe).hasKnownStatus(MAX_ENTITIES_IN_PRICE_REGIME_HAVE_BEEN_CREATED),
                createTopic(notToBe).hasKnownStatus(MAX_ENTITIES_IN_PRICE_REGIME_HAVE_BEEN_CREATED));
    }

    @LeakyEmbeddedHapiTest(
            reason = NEEDS_STATE_ACCESS,
            overrides = {"consensus.message.maxBytesAllowed"})
    final Stream<DynamicTest> messageSubmissionSizeChange() {
        final var defaultMaxBytesAllowed = 1024;
        final var longMessage = TxnUtils.randomUtf8Bytes(defaultMaxBytesAllowed);

        return hapiTest(
                newKeyNamed("submitKey"),
                createTopic(TEST_TOPIC).submitKeyName("submitKey"),
                cryptoCreate(CIVILIAN),
                submitMessageTo(TEST_TOPIC)
                        .message("testmessage")
                        .payingWith(CIVILIAN)
                        .hasRetryPrecheckFrom(BUSY)
                        .hasKnownStatus(SUCCESS),
                overriding("consensus.message.maxBytesAllowed", String.valueOf(defaultMaxBytesAllowed - 1)),
                submitMessageTo(TEST_TOPIC)
                        .message(longMessage)
                        .payingWith(CIVILIAN)
                        .hasRetryPrecheckFrom(BUSY)
                        .hasKnownStatus(MESSAGE_SIZE_TOO_LARGE));
    }
}
