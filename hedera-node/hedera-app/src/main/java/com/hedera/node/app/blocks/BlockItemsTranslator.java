// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.blocks;

import static com.hedera.hapi.node.base.HederaFunctionality.CONTRACT_CALL;
import static com.hedera.hapi.node.base.HederaFunctionality.CONTRACT_CREATE;
import static com.hedera.hapi.node.base.HederaFunctionality.ETHEREUM_TRANSACTION;
import static com.hedera.hapi.node.base.HederaFunctionality.HOOK_DISPATCH;
import static com.hedera.node.app.blocks.historical.HistoricalLogBloom.forAll;
import static com.hedera.node.app.blocks.historical.HistoricalLogBloom.forLog;
import static com.hedera.node.app.service.token.api.ContractChangeSummary.NONCE_INFO_CONTRACT_ID_COMPARATOR;
import static java.util.Objects.requireNonNull;

import com.hedera.hapi.block.stream.output.TransactionOutput;
import com.hedera.hapi.block.stream.output.TransactionResult;
import com.hedera.hapi.block.stream.trace.EvmTransactionLog;
import com.hedera.hapi.node.base.ContractID;
import com.hedera.hapi.node.contract.ContractFunctionResult;
import com.hedera.hapi.node.contract.ContractLoginfo;
import com.hedera.hapi.node.contract.ContractNonceInfo;
import com.hedera.hapi.node.contract.EvmTransactionResult;
import com.hedera.hapi.node.transaction.TransactionReceipt;
import com.hedera.hapi.node.transaction.TransactionRecord;
import com.hedera.node.app.blocks.historical.HistoricalCallContext;
import com.hedera.node.app.blocks.historical.HistoricalContractResult;
import com.hedera.node.app.blocks.historical.HistoricalEthereumAddress;
import com.hedera.node.app.blocks.historical.HistoricalLog;
import com.hedera.node.app.blocks.historical.HistoricalLogData;
import com.hedera.node.app.blocks.historical.HistoricalLogTopic;
import com.hedera.node.app.blocks.impl.TranslationContext;
import com.hedera.node.app.blocks.impl.contexts.AirdropOpContext;
import com.hedera.node.app.blocks.impl.contexts.ContractOpContext;
import com.hedera.node.app.blocks.impl.contexts.CryptoOpContext;
import com.hedera.node.app.blocks.impl.contexts.FileOpContext;
import com.hedera.node.app.blocks.impl.contexts.MintOpContext;
import com.hedera.node.app.blocks.impl.contexts.NodeOpContext;
import com.hedera.node.app.blocks.impl.contexts.ScheduleOpContext;
import com.hedera.node.app.blocks.impl.contexts.SubmitOpContext;
import com.hedera.node.app.blocks.impl.contexts.SupplyChangeOpContext;
import com.hedera.node.app.blocks.impl.contexts.TokenOpContext;
import com.hedera.node.app.blocks.impl.contexts.TopicOpContext;
import com.hedera.node.app.hapi.utils.contracts.HookUtils;
import com.hedera.pbj.runtime.io.buffer.Bytes;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Translates a {@link TransactionResult} and, optionally, one or more {@link TransactionOutput}s within a given
 * {@link TranslationContext} into a {@link TransactionRecord} or {@link TransactionReceipt} appropriate for returning
 * from a query.
 */
public class BlockItemsTranslator {
    private static final Function<TransactionOutput, EvmTransactionResult> CONTRACT_CALL_EXTRACTOR =
            output -> output.contractCallOrThrow().evmTransactionResultOrThrow();
    private static final Function<TransactionOutput, EvmTransactionResult> CONTRACT_CREATE_EXTRACTOR =
            output -> output.contractCreateOrThrow().evmTransactionResultOrThrow();

    public static final BlockItemsTranslator BLOCK_ITEMS_TRANSLATOR = new BlockItemsTranslator();

    /**
     * Translate the given {@link TransactionResult} and optional {@link TransactionOutput}s into a
     * {@link TransactionReceipt} appropriate for returning from a query.
     *
     * @param context the context of the transaction
     * @param result the result of the transaction
     * @param blockNumber the block number, if known
     * @param outputs the outputs of the transaction
     * @return the translated receipt
     */
    public TransactionReceipt translateReceipt(
            @NonNull final TranslationContext context,
            @NonNull final TransactionResult result,
            @Nullable final Long blockNumber,
            @NonNull final TransactionOutput... outputs) {
        requireNonNull(context);
        requireNonNull(result);
        requireNonNull(outputs);
        final var receiptBuilder = TransactionReceipt.newBuilder()
                .status(result.status())
                .exchangeRate(context.transactionExchangeRates());
        final var function = context.functionality();
        switch (function) {
            case CONTRACT_CALL, CONTRACT_CREATE, CONTRACT_DELETE, CONTRACT_UPDATE, ETHEREUM_TRANSACTION ->
                receiptBuilder.contractID(((ContractOpContext) context).contractId());
            case CRYPTO_CREATE, CRYPTO_UPDATE -> receiptBuilder.accountID(((CryptoOpContext) context).accountId());
            case FILE_CREATE -> receiptBuilder.fileID(((FileOpContext) context).fileId());
            case NODE_CREATE -> receiptBuilder.nodeId(((NodeOpContext) context).nodeId());
            case REGISTERED_NODE_CREATE -> receiptBuilder.registeredNodeId(((NodeOpContext) context).nodeId());
            case SCHEDULE_CREATE -> {
                final var scheduleOutput = outputValueIfPresent(
                        TransactionOutput::hasCreateSchedule, TransactionOutput::createScheduleOrThrow, outputs);
                if (scheduleOutput != null) {
                    receiptBuilder
                            .scheduleID(scheduleOutput.scheduleId())
                            .scheduledTransactionID(scheduleOutput.scheduledTransactionId());
                }
            }
            case SCHEDULE_DELETE -> receiptBuilder.scheduleID(((ScheduleOpContext) context).scheduleId());
            case SCHEDULE_SIGN -> {
                final var signOutput = outputValueIfPresent(
                        TransactionOutput::hasSignSchedule, TransactionOutput::signScheduleOrThrow, outputs);
                if (signOutput != null) {
                    receiptBuilder.scheduledTransactionID(signOutput.scheduledTransactionId());
                }
            }
            case CONSENSUS_SUBMIT_MESSAGE ->
                receiptBuilder
                        .topicRunningHashVersion(((SubmitOpContext) context).runningHashVersion())
                        .topicSequenceNumber(((SubmitOpContext) context).sequenceNumber())
                        .topicRunningHash(((SubmitOpContext) context).runningHash());
            case TOKEN_MINT ->
                receiptBuilder
                        .newTotalSupply(((MintOpContext) context).newTotalSupply())
                        .serialNumbers(((MintOpContext) context).serialNumbers());
            case TOKEN_BURN, TOKEN_ACCOUNT_WIPE ->
                receiptBuilder.newTotalSupply(((SupplyChangeOpContext) context).newTotalSupply());
            case TOKEN_CREATE -> receiptBuilder.tokenID(((TokenOpContext) context).tokenId());
            case CONSENSUS_CREATE_TOPIC -> receiptBuilder.topicID(((TopicOpContext) context).topicId());
        }
        if (blockNumber != null) {
            receiptBuilder.blockNumber(blockNumber);
        }
        return receiptBuilder.build();
    }

    /**
     * Translate the given {@link TransactionResult} and optional {@link TransactionOutput}s into a
     * {@link TransactionRecord} appropriate for returning from a query.
     *
     * @param context the context of the transaction
     * @param result the result of the transaction
     * @param logs the EVM logs of the transaction, if any
     * @param blockNumber the block number, if known
     * @param outputs the outputs of the transaction
     * @return the translated record
     */
    public TransactionRecord translateRecord(
            @NonNull final TranslationContext context,
            @NonNull final TransactionResult result,
            @Nullable final List<EvmTransactionLog> logs,
            @Nullable final Long blockNumber,
            @NonNull final TransactionOutput... outputs) {
        requireNonNull(context);
        requireNonNull(result);
        requireNonNull(outputs);
        final var recordBuilder = TransactionRecord.newBuilder()
                .transactionID(context.txnId())
                .memo(context.memo())
                .transactionHash(context.transactionHash())
                .consensusTimestamp(result.consensusTimestamp())
                .parentConsensusTimestamp(result.parentConsensusTimestamp())
                .scheduleRef(result.scheduleRef())
                .transactionFee(result.transactionFeeCharged())
                .transferList(result.transferList())
                .tokenTransferLists(result.tokenTransferLists())
                .automaticTokenAssociations(result.automaticTokenAssociations())
                .assessedCustomFees(result.assessedCustomFees())
                .paidStakingRewards(result.paidStakingRewards());
        if (result.highVolumePricingMultiplier() != 0) {
            recordBuilder.highVolumePricingMultiplier(result.highVolumePricingMultiplier());
        }
        final var function = context.functionality();
        switch (function) {
            case HOOK_DISPATCH,
                    CONTRACT_CALL,
                    CONTRACT_CREATE,
                    CONTRACT_DELETE,
                    CONTRACT_UPDATE,
                    ETHEREUM_TRANSACTION -> {
                if (function == CONTRACT_CALL || function == HOOK_DISPATCH) {
                    recordBuilder.contractCallResult(outputValueIfPresent(
                            TransactionOutput::hasContractCall,
                            translatingExtractor(CONTRACT_CALL_EXTRACTOR, context, logs),
                            outputs));
                } else if (function == CONTRACT_CREATE) {
                    recordBuilder.contractCreateResult(outputValueIfPresent(
                            TransactionOutput::hasContractCreate,
                            translatingExtractor(CONTRACT_CREATE_EXTRACTOR, context, logs),
                            outputs));
                } else if (function == ETHEREUM_TRANSACTION) {
                    recordBuilder.ethereumHash(((ContractOpContext) context).ethHash());
                    final var ethOutput = outputValueIfPresent(
                            TransactionOutput::hasEthereumCall, TransactionOutput::ethereumCallOrThrow, outputs);
                    if (ethOutput != null) {
                        switch (ethOutput.transactionResult().kind()) {
                            case EVM_CALL_TRANSACTION_RESULT ->
                                recordBuilder.contractCallResult(legacyResultFrom(historicalResultFrom(
                                        ethOutput.evmCallTransactionResultOrThrow(), context, logs)));
                            case EVM_CREATE_TRANSACTION_RESULT ->
                                recordBuilder.contractCreateResult(legacyResultFrom(historicalResultFrom(
                                        ethOutput.evmCreateTransactionResultOrThrow(), context, logs)));
                        }
                    }
                }
            }
            default -> {
                final var synthResult = outputValueIfPresent(
                        TransactionOutput::hasContractCall,
                        translatingExtractor(CONTRACT_CALL_EXTRACTOR, context, null),
                        outputs);
                if (synthResult != null) {
                    recordBuilder.contractCallResult(synthResult);
                }
                switch (function) {
                    case CRYPTO_CREATE, CRYPTO_UPDATE ->
                        recordBuilder.evmAddress(((CryptoOpContext) context).evmAddress());
                    case TOKEN_AIRDROP ->
                        recordBuilder.newPendingAirdrops(((AirdropOpContext) context).pendingAirdropRecords());
                    case UTIL_PRNG -> {
                        final var prngOutput = outputValueIfPresent(
                                TransactionOutput::hasUtilPrng, TransactionOutput::utilPrngOrThrow, outputs);
                        if (prngOutput != null) {
                            switch (prngOutput.entropy().kind()) {
                                case PRNG_BYTES -> recordBuilder.prngBytes(prngOutput.prngBytesOrThrow());
                                case PRNG_NUMBER -> recordBuilder.prngNumber(prngOutput.prngNumberOrThrow());
                            }
                        }
                    }
                }
            }
        }
        return recordBuilder
                .receipt(translateReceipt(context, result, blockNumber, outputs))
                .build();
    }

    private Function<TransactionOutput, ContractFunctionResult> translatingExtractor(
            @NonNull final Function<TransactionOutput, EvmTransactionResult> extractor,
            @NonNull final TranslationContext context,
            @Nullable final List<EvmTransactionLog> logs) {
        return output -> legacyResultFrom(historicalResultFrom(extractor.apply(output), context, logs));
    }

    private ContractFunctionResult legacyResultFrom(@NonNull final HistoricalContractResult result) {
        final var builder = ContractFunctionResult.newBuilder()
                .senderId(result.senderId())
                .contractID(result.contractId())
                .contractCallResult(result.returnData())
                .errorMessage(result.errorMessage())
                .gasUsed(result.gasUsed());
        if (result.callContext() != null) {
            builder.gas(result.callContext().gas())
                    .amount(result.callContext().value())
                    .functionParameters(result.callContext().callData());
        }
        if (result.signerNonce() != null) {
            builder.signerNonce(result.signerNonce());
        }
        if (result.createdContractIds() != null) {
            builder.createdContractIDs(result.createdContractIds());
        }
        if (result.evmAddress() != null) {
            builder.evmAddress(result.evmAddress());
        }
        if (!result.contractNonces().isEmpty()) {
            builder.contractNonces(result.contractNonces());
        }
        attachLogsTo(builder, result.logs());
        return builder.build();
    }

    private HistoricalContractResult historicalResultFrom(
            @NonNull final EvmTransactionResult result,
            @NonNull final TranslationContext context,
            @Nullable final List<EvmTransactionLog> logs) {
        final var callContext = result.hasInternalCallContext()
                ? result.internalCallContextOrThrow()
                : context instanceof ContractOpContext contractOpContext ? contractOpContext.ethCallContext() : null;
        Long signerNonce = null;
        List<ContractID> createdContractIds = null;
        Bytes evmAddress = null;
        List<ContractNonceInfo> contractNonces = List.of();
        if (context instanceof ContractOpContext contractContext) {
            signerNonce = contractContext.senderNonce();
            createdContractIds = contractContext.createdContractIds();
            evmAddress = contractContext.evmAddress();
            final var changedNonceInfos = contractContext.changedNonceInfos();
            if (changedNonceInfos != null && !changedNonceInfos.isEmpty()) {
                final var infos = new ArrayList<>(changedNonceInfos);
                infos.sort(NONCE_INFO_CONTRACT_ID_COMPARATOR);
                contractNonces = infos;
            }
        }
        return new HistoricalContractResult(
                result.senderId(),
                result.contractId(),
                result.resultData(),
                result.errorMessage(),
                result.gasUsed(),
                callContext == null
                        ? null
                        : new HistoricalCallContext(callContext.gas(), callContext.value(), callContext.callData()),
                signerNonce,
                createdContractIds,
                evmAddress,
                contractNonces,
                historicalLogsFrom(context instanceof ContractOpContext ? logs : null));
    }

    private List<HistoricalLog> historicalLogsFrom(@Nullable final List<EvmTransactionLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return List.of();
        }
        final List<HistoricalLog> historicalLogs = new ArrayList<>(logs.size());
        for (final var log : logs) {
            final var paddedTopics =
                    log.topics().stream().map(HookUtils::leftPad32).toList();
            final var historicalLog = new HistoricalLog(
                    log.contractIdOrThrow(),
                    HistoricalEthereumAddress.fromEntityNumber(
                            log.contractIdOrThrow().contractNumOrThrow()),
                    paddedTopics.stream().map(HistoricalLogTopic::new).toList(),
                    new HistoricalLogData(log.data()));
            historicalLogs.add(historicalLog);
        }
        return historicalLogs;
    }

    private void attachLogsTo(
            @NonNull final ContractFunctionResult.Builder builder, @NonNull final List<HistoricalLog> logs) {
        if (logs.isEmpty()) {
            return;
        }
        final List<ContractLoginfo> verboseLogs = new ArrayList<>(logs.size());
        for (final var log : logs) {
            verboseLogs.add(ContractLoginfo.newBuilder()
                    .contractID(log.contractId())
                    .topic(log.topics().stream().map(HistoricalLogTopic::bytes).toList())
                    .bloom(forLog(log))
                    .data(log.data().bytes())
                    .build());
        }
        builder.bloom(forAll(logs)).logInfo(verboseLogs);
    }

    private static <T> T outputValueIfPresent(
            @NonNull final Predicate<TransactionOutput> filter,
            @NonNull final Function<TransactionOutput, T> extractor,
            @NonNull final TransactionOutput... outputs) {
        for (final var output : outputs) {
            if (filter.test(output)) {
                return extractor.apply(output);
            }
        }
        return null;
    }
}
