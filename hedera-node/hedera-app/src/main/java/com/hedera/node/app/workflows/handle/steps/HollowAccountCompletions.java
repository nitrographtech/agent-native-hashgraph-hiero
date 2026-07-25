// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.workflows.handle.steps;

import static com.hedera.hapi.node.base.ResponseCodeEnum.SUCCESS;
import static com.hedera.node.app.hapi.utils.keys.KeyUtils.IMMUTABILITY_SENTINEL_KEY;
import static com.hedera.node.app.spi.fees.NoopFeeCharging.UNIVERSAL_NOOP_FEE_CHARGING;
import static com.hedera.node.app.spi.workflows.DispatchOptions.setupDispatch;
import static java.util.Objects.requireNonNull;

import com.hedera.hapi.node.base.AccountID;
import com.hedera.hapi.node.state.token.Account;
import com.hedera.hapi.node.token.CryptoUpdateTransactionBody;
import com.hedera.hapi.node.transaction.TransactionBody;
import com.hedera.node.app.service.token.records.CryptoUpdateStreamBuilder;
import com.hedera.node.app.signature.AppKeyVerifier;
import com.hedera.node.app.spi.signatures.SignatureVerification;
import com.hedera.node.app.spi.workflows.HandleContext;
import com.hedera.node.app.spi.workflows.HandleContext.ConsensusThrottling;
import com.hedera.node.app.spi.workflows.HandleException;
import com.hedera.node.app.workflows.handle.Dispatch;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Completes the hollow accounts by finalizing them.
 */
@Singleton
public class HollowAccountCompletions {
    private static final Logger logger = LogManager.getLogger(HollowAccountCompletions.class);

    @Inject
    public HollowAccountCompletions() {}

    /**
     * Finalizes the hollow accounts by updating the key on the hollow accounts that need to be finalized.
     * This is done by dispatching a preceding synthetic update transaction. The key is derived from the signature
     * expansion, by looking up the ECDSA key for the alias.
     * The hollow accounts that need to be finalized are determined by the set of hollow accounts that are returned
     * by the pre-handle result.
     * @param parentTxn the user transaction component
     * @param dispatch the dispatch
     * @return a replay descriptor for any completion dispatches that were attempted
     */
    public @Nullable Details completeHollowAccounts(
            @NonNull final ParentTxn parentTxn, @NonNull final Dispatch dispatch) {
        requireNonNull(parentTxn);
        requireNonNull(dispatch);
        // Any hollow accounts that must sign to have all needed signatures, need to be finalized
        // as a result of transaction being handled.
        final Set<Account> hollowAccounts = parentTxn.preHandleResult().getHollowAccounts();
        return finalizeHollowAccounts(
                dispatch.handleContext(), hollowAccounts, dispatch.keyVerifier(), null, parentTxn);
    }

    /**
     * Updates key on the hollow accounts that need to be finalized. This is done by dispatching a preceding
     * synthetic update transaction. The ksy is derived from the signature expansion, by looking up the ECDSA key
     * for the alias.
     *
     * @param context the handle context
     * @param accounts the set of hollow accounts that need to be finalized
     * @param verifier the key verifier
     * @param ethTxVerification the Ethereum transaction verification
     * @return a replay descriptor for any completion dispatches that were attempted
     */
    private @Nullable Details finalizeHollowAccounts(
            @NonNull final HandleContext context,
            @NonNull final Set<Account> accounts,
            @NonNull final AppKeyVerifier verifier,
            @Nullable SignatureVerification ethTxVerification,
            @NonNull final ParentTxn parentTxn) {
        final List<Detail> replayableFinalizations = new ArrayList<>();
        for (final var hollowAccount : accounts) {
            if (!parentTxn.stack().rootHasPrecedingCapacity()) {
                break;
            }
            final var hollowAccountId = hollowAccount.accountIdOrElse(AccountID.DEFAULT);
            if (hollowAccountId.equals(AccountID.DEFAULT)) {
                // The CryptoCreateHandler uses a "hack" to validate that a CryptoCreate with
                // an EVM address has signed with that alias's ECDSA key; that is, it adds a
                // dummy "hollow account" with the EVM address as an alias. But we don't want
                // to try to finalize such a dummy account, so skip it here.
                continue;
            }
            // get the verified key for this hollow account
            final var verification =
                    ethTxVerification != null && hollowAccount.alias().equals(ethTxVerification.evmAlias())
                            ? ethTxVerification
                            : requireNonNull(
                                    verifier.verificationFor(hollowAccount.alias()),
                                    "Required hollow account verified signature did not exist");
            if (verification.key() != null) {
                if (!IMMUTABILITY_SENTINEL_KEY.equals(hollowAccount.keyOrThrow())) {
                    logger.error("Hollow account {} has a key other than the sentinel key", hollowAccount);
                    break;
                }
                // dispatch synthetic update transaction for updating key on this hollow account
                final var syntheticUpdateTxn = TransactionBody.newBuilder()
                        .cryptoUpdateAccount(CryptoUpdateTransactionBody.newBuilder()
                                .accountIDToUpdate(hollowAccount.accountId())
                                .key(verification.key())
                                .build())
                        .build();
                final var streamBuilder = context.dispatch(setupDispatch(
                        context.payer(),
                        syntheticUpdateTxn,
                        CryptoUpdateStreamBuilder.class,
                        UNIVERSAL_NOOP_FEE_CHARGING,
                        ConsensusThrottling.OFF));
                streamBuilder.accountID(hollowAccount.accountIdOrThrow());
                if (streamBuilder.status() != SUCCESS) {
                    logger.warn(
                            "{} - failed to finalize hollow account {} via {}",
                            streamBuilder.status(),
                            hollowAccountId,
                            syntheticUpdateTxn);
                } else {
                    replayableFinalizations.add(new Detail(hollowAccount.accountIdOrThrow(), syntheticUpdateTxn));
                }
            }
        }
        return replayableFinalizations.isEmpty() ? null : new Details(context.payer(), replayableFinalizations);
    }

    /**
     * The information needed to replay hollow account completion dispatches after rollback.
     * @param payerId the payer id to use for replayed setup dispatches
     * @param details the synthetic update transactions and target account ids
     */
    public record Details(
            @NonNull AccountID payerId, @NonNull List<Detail> details) {
        public Details {
            requireNonNull(payerId);
            requireNonNull(details);
        }

        /**
         * Replays each hollow-account finalization as a setup dispatch.
         * @param childDispatch the dispatch callback to use
         */
        public void replay(@NonNull final HandleException.ChildDispatch childDispatch) {
            requireNonNull(childDispatch);
            for (final var finalization : details) {
                final var streamBuilder = childDispatch.dispatch(setupDispatch(
                        payerId,
                        finalization.syntheticUpdateTxn(),
                        CryptoUpdateStreamBuilder.class,
                        UNIVERSAL_NOOP_FEE_CHARGING,
                        ConsensusThrottling.OFF));
                streamBuilder.accountID(finalization.accountId());
                if (streamBuilder.status() != SUCCESS) {
                    logger.warn(
                            "{} - failed to replay hollow account finalization for {} via {}",
                            streamBuilder.status(),
                            finalization.accountId(),
                            finalization.syntheticUpdateTxn());
                }
            }
        }
    }

    /**
     * A single replayable synthetic crypto-update used to finalize a hollow account.
     * @param accountId the account being finalized
     * @param syntheticUpdateTxn the synthetic update transaction
     */
    public record Detail(
            @NonNull AccountID accountId, @NonNull TransactionBody syntheticUpdateTxn) {
        public Detail {
            requireNonNull(accountId);
            requireNonNull(syntheticUpdateTxn);
        }
    }
}
