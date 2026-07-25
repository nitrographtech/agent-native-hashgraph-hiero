// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.service.contract.impl.exec.metrics;

import static com.hedera.hapi.node.base.HederaFunctionality.CONTRACT_CALL;
import static com.hedera.hapi.node.base.HederaFunctionality.CONTRACT_CREATE;
import static com.hedera.hapi.node.base.HederaFunctionality.ETHEREUM_TRANSACTION;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

import com.google.common.annotations.VisibleForTesting;
import com.hedera.hapi.node.base.HederaFunctionality;
import com.hedera.node.config.data.ContractsConfig;
import com.swirlds.metrics.api.*;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.consensus.metrics.platform.prometheus.NameConverter;

/**
 * Metrics collection management for Smart Contracts service
 *
 * Includes:
 * * Rejected transactions counters: Transactions which failed `pureCheck` for one reason or another
 */
public class ContractMetrics {

    private static final Logger log = LogManager.getLogger(ContractMetrics.class);

    private final Metrics metrics;
    private final Supplier<ContractsConfig> contractsConfigSupplier;
    private boolean p1MetricsEnabled;

    private CountAccumulateAverageMetricTriplet transactionDuration;
    private CountAccumulateAverageMetricTriplet successfulTransactionDuration;
    private CountAccumulateAverageMetricTriplet failedTransactionDuration;
    private CountAccumulateAverageMetricTriplet transactionGasUsed;
    private LongGauge gasPrice;

    private final OpsDurationMetrics opsDurationMetrics;

    // Counters that are the P1 metrics

    private final ConcurrentHashMap<HederaFunctionality, Counter> rejectedTxsCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<HederaFunctionality, Counter> rejectedTxsLackingIntrinsicGas =
            new ConcurrentHashMap<>();
    private Counter rejectedEthType3Counter;

    public record TransactionProcessingSummary(
            long durationNanos, long opsDurationUnitsConsumed, long gasUsed, OptionalLong gasPrice, boolean success) {}

    private static final Map<HederaFunctionality, String> POSSIBLE_FAILING_TX_TYPES = Map.of(
            CONTRACT_CALL, "contractCallTx", CONTRACT_CREATE, "contractCreateTx", ETHEREUM_TRANSACTION, "ethereumTx");

    // String templates and other stringish things used to create metrics' names and descriptions

    static final String METRIC_CATEGORY = "app";
    static final String METRIC_SERVICE = "SmartContractService";
    private static final String METRIC_TXN_UNIT = "txs";

    // Templates:  %1$s - HederaFunctionality name
    //             %2$s - METRIC_SERVICE
    //             %3$s - short specific metric description

    private static final String REJECTED_NAME_TEMPLATE = "%2$s:Rejected_%1$s_total";
    private static final String REJECTED_DESCR_TEMPLATE = "submitted %1$s %3$s rejected by pureChecks";

    private static final String REJECTED_TXN_SHORT_DESCR = "txns";
    private static final String REJECTED_TYPE3_SHORT_DESCR = "Ethereum Type 3 txns";
    private static final String REJECTED_FOR_GAS_SHORT_DESCR = "txns with not even intrinsic gas";
    private static final String REJECTED_TYPE3_FUNCTIONALITY = "ethType3BlobTransaction";

    @Inject
    public ContractMetrics(
            @NonNull final Metrics metrics, @NonNull final Supplier<ContractsConfig> contractsConfigSupplier) {
        this.metrics = requireNonNull(metrics, "metrics (from platform via ServicesMain/Hedera must not be null");
        this.contractsConfigSupplier =
                requireNonNull(contractsConfigSupplier, "contracts configuration supplier must not be null");
        this.opsDurationMetrics = new OpsDurationMetrics(metrics);
    }

    // --------------------
    // Creating the metrics

    /**
     * Primary metrics are a fixed set and can be created when `Hedera` initializes the system.  But
     * it actually must wait until the platform calls `Hedera.onStateInitialized`, and then for
     * GENESIS only.
     */
    public void createContractPrimaryMetrics() {
        final var contractsConfig = requireNonNull(contractsConfigSupplier.get());
        this.p1MetricsEnabled = contractsConfig.metricsSmartContractPrimaryEnabled();

        if (p1MetricsEnabled) {

            // Rejected transactions counters
            for (final var txKind : POSSIBLE_FAILING_TX_TYPES.keySet()) {
                final var name = toRejectedName(txKind, REJECTED_TXN_SHORT_DESCR);
                final var descr = toRejectedDescr(txKind, REJECTED_TXN_SHORT_DESCR);
                final var config = new Counter.Config(METRIC_CATEGORY, name)
                        .withDescription(descr)
                        .withUnit(METRIC_TXN_UNIT);
                final var metric = newCounter(metrics, config);
                rejectedTxsCounters.put(txKind, metric);
            }

            // Rejected transactions because they don't even have intrinsic gas
            for (final var txKind : POSSIBLE_FAILING_TX_TYPES.keySet()) {
                final var functionalityName = POSSIBLE_FAILING_TX_TYPES.get(txKind) + "DueToIntrinsicGas";
                final var name = toRejectedName(functionalityName, REJECTED_FOR_GAS_SHORT_DESCR);
                final var descr = toRejectedDescr(functionalityName, REJECTED_FOR_GAS_SHORT_DESCR);
                final var config = new Counter.Config(METRIC_CATEGORY, name)
                        .withDescription(descr)
                        .withUnit(METRIC_TXN_UNIT);
                final var metric = newCounter(metrics, config);
                rejectedTxsLackingIntrinsicGas.put(txKind, metric);
            }

            // Rejected transactions for ethereum calls that are in type 3 blob transaction format
            {
                final var name = toRejectedName(REJECTED_TYPE3_FUNCTIONALITY, REJECTED_TYPE3_SHORT_DESCR);
                final var descr = toRejectedDescr(REJECTED_TYPE3_FUNCTIONALITY, REJECTED_TYPE3_SHORT_DESCR);
                final var config = new Counter.Config(METRIC_CATEGORY, name)
                        .withDescription(descr)
                        .withUnit(METRIC_TXN_UNIT);
                final var metric = newCounter(metrics, config);
                rejectedEthType3Counter = metric;
            }

            transactionDuration = CountAccumulateAverageMetricTriplet.create(
                    metrics,
                    METRIC_CATEGORY,
                    METRIC_SERVICE + ":TransactionDuration",
                    "Actual duration of processed smart contract transactions in nanoseconds");
            successfulTransactionDuration = CountAccumulateAverageMetricTriplet.create(
                    metrics,
                    METRIC_CATEGORY,
                    METRIC_SERVICE + ":SuccessfulTransactionDuration",
                    "Actual duration of successful smart contract transactions in nanoseconds");
            failedTransactionDuration = CountAccumulateAverageMetricTriplet.create(
                    metrics,
                    METRIC_CATEGORY,
                    METRIC_SERVICE + ":FailedTransactionDuration",
                    "Actual duration of failed smart contract transactions in nanoseconds");
            transactionGasUsed = CountAccumulateAverageMetricTriplet.create(
                    metrics,
                    METRIC_CATEGORY,
                    METRIC_SERVICE + ":TransactionGasUsed",
                    "Actual gas used by smart contract transactions");
            gasPrice = metrics.getOrCreate(new LongGauge.Config(METRIC_CATEGORY, METRIC_SERVICE + ":LatestGasPrice")
                    .withDescription("Gas price of the latest processed smart contract transaction"));
        }
    }

    // ---------------------------------
    // P1 metrics:  `pureCheck` failures

    public void incrementRejectedTx(@NonNull final HederaFunctionality txKind) {
        bumpRejectedTx(txKind, 1);
    }

    public void bumpRejectedTx(@NonNull final HederaFunctionality txKind, final long bumpBy) {
        if (p1MetricsEnabled) {
            requireNonNull(rejectedTxsCounters.get(txKind)).add(bumpBy);
        }
    }

    public void incrementRejectedForGasTx(@NonNull final HederaFunctionality txKind) {
        bumpRejectedForGasTx(txKind, 1);
    }

    public void bumpRejectedForGasTx(@NonNull final HederaFunctionality txKind, final long bumpBy) {
        if (p1MetricsEnabled)
            requireNonNull(rejectedTxsLackingIntrinsicGas.get(txKind)).add(bumpBy);
    }

    public void incrementRejectedType3EthTx() {
        bumpRejectedType3EthTx(1);
    }

    public void bumpRejectedType3EthTx(final long bumpBy) {
        if (p1MetricsEnabled) {
            rejectedEthType3Counter.add(bumpBy);
        }
    }

    public void recordProcessedTransaction(final TransactionProcessingSummary summary) {
        if (p1MetricsEnabled) {
            this.transactionDuration.recordObservation(summary.durationNanos());
            if (summary.success()) {
                this.successfulTransactionDuration.recordObservation(summary.durationNanos());
            } else {
                this.failedTransactionDuration.recordObservation(summary.durationNanos());
            }
            this.transactionGasUsed.recordObservation(summary.gasUsed());
            summary.gasPrice().ifPresent(newGasPrice -> {
                this.gasPrice.set(newGasPrice);
            });
        }

        this.opsDurationMetrics.recordTxnTotalOpsDuration(summary.opsDurationUnitsConsumed());
    }

    public OpsDurationMetrics opsDurationMetrics() {
        return opsDurationMetrics;
    }

    // -----------------
    // Unit test helpers

    @VisibleForTesting
    public @NonNull Set<Counter> getAllP1Counters() {
        final var allCounters = new HashSet<Counter>(200);
        allCounters.addAll(rejectedTxsCounters.values());
        allCounters.addAll(rejectedTxsLackingIntrinsicGas.values());
        if (rejectedEthType3Counter != null) allCounters.add(rejectedEthType3Counter);
        return allCounters;
    }

    @VisibleForTesting
    public @NonNull Set<Counter> getAllCounters() {
        return getAllP1Counters();
    }

    @VisibleForTesting
    public @NonNull Map<String, Long> getAllP1CounterValues() {
        return getAllP1Counters().stream().collect(toMap(Counter::getName, Counter::get));
    }

    @VisibleForTesting
    public @NonNull Map<String, Long> getAllCounterValues() {
        return getAllCounters().stream().collect(toMap(Counter::getName, Counter::get));
    }

    @VisibleForTesting
    public @NonNull List<String> getAllP1CounterNames() {
        return getAllP1Counters().stream().map(Metric::getName).sorted().toList();
    }

    @VisibleForTesting
    public @NonNull List<String> getAllCounterNames() {
        final var n = getAllCounters().stream().map(Metric::getDescription).count();
        return getAllCounters().stream().map(Metric::getName).sorted().toList();
    }

    @VisibleForTesting
    public @NonNull List<String> getAllCounterDescriptions() {
        return getAllCounters().stream().map(Metric::getDescription).sorted().toList();
    }

    @VisibleForTesting
    public @NonNull String allCountersToString() {
        return '{' + allCountersAsTable().replace("\n", ", ") + '}';
    }

    @VisibleForTesting
    public @NonNull String allCountersAsTable() {
        return getAllCounterValues().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("\n"));
    }

    @VisibleForTesting
    public long getProcessedTransactionCount() {
        return this.transactionDuration.counter().get();
    }

    // ---------------------------------
    // Helpers for making metrics' names

    private @NonNull Counter newCounter(@NonNull final Metrics metrics, @NonNull final Counter.Config config) {
        return metrics.getOrCreate(config);
    }

    private static @NonNull String toRejectedName(
            @NonNull final HederaFunctionality functionality, @NonNull final String shortDescription) {
        return toRejectedName(POSSIBLE_FAILING_TX_TYPES.get(functionality), shortDescription);
    }

    private static @NonNull String toRejectedName(
            @NonNull final String functionality, @NonNull final String shortDescription) {
        return toString(REJECTED_NAME_TEMPLATE, functionality, shortDescription);
    }

    private static @NonNull String toRejectedDescr(
            @NonNull final HederaFunctionality functionality, @NonNull final String shortDescription) {
        return toString(REJECTED_DESCR_TEMPLATE, POSSIBLE_FAILING_TX_TYPES.get(functionality), shortDescription);
    }

    private static @NonNull String toRejectedDescr(
            @NonNull final String functionality, @NonNull final String shortDescription) {
        return toString(REJECTED_DESCR_TEMPLATE, functionality, shortDescription);
    }

    private static @NonNull String toString(
            @NonNull final String template,
            @NonNull final String functionality,
            @NonNull final String shortDescription) {
        final var possiblyUnacceptableName = template.formatted(functionality, METRIC_SERVICE, shortDescription);
        final var definitelyAcceptableName = NameConverter.fix(possiblyUnacceptableName);
        return definitelyAcceptableName;
    }

    private <E extends Enum<E>> @NonNull EnumSet<E> intersect(
            @NonNull final EnumSet<E> set1, @NonNull final EnumSet<E> set2) {
        final var r = set1.clone();
        r.retainAll(set2);
        return r;
    }
}
