// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.blocks.historical;

import static java.util.Objects.requireNonNull;

import com.hedera.hapi.node.base.AccountID;
import com.hedera.hapi.node.base.ContractID;
import com.hedera.hapi.streams.CallOperationType;
import com.hedera.hapi.streams.ContractAction;
import com.hedera.hapi.streams.ContractActionType;
import com.hedera.pbj.runtime.io.buffer.Bytes;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * An immutable, non-executable snapshot of a historical contract action.
 *
 * <p>Nullable fields preserve PBJ one-of presence exactly. Numeric fields
 * retain their protobuf bit patterns without execution or economic
 * interpretation. List position is the action index and {@code callDepth}
 * preserves the historical nesting relationship.
 */
public record HistoricalContractAction(
        ContractActionType callType,
        @Nullable AccountID callingAccount,
        @Nullable ContractID callingContract,
        long gas,
        Bytes input,
        @Nullable AccountID recipientAccount,
        @Nullable ContractID recipientContract,
        @Nullable Bytes targetedAddress,
        long value,
        long gasUsed,
        @Nullable Bytes output,
        @Nullable Bytes revertReason,
        @Nullable Bytes error,
        int callDepth,
        CallOperationType callOperationType) {
    private static final int EVM_ADDRESS_BYTES = 20;

    public HistoricalContractAction {
        requireNonNull(callType);
        requireNonNull(input);
        requireNonNull(callOperationType);
        requireAtMostOne("caller", callingAccount, callingContract);
        requireAtMostOne("recipient", recipientAccount, recipientContract, targetedAddress);
        requireAtMostOne("result", output, revertReason, error);
        if (targetedAddress != null && targetedAddress.length() != EVM_ADDRESS_BYTES) {
            throw new IllegalArgumentException("targetedAddress must contain exactly 20 bytes");
        }
        if (callDepth < 0) {
            throw new IllegalArgumentException("callDepth must not be negative");
        }
    }

    /** Creates a neutral snapshot without changing PBJ field presence. */
    public static HistoricalContractAction fromPbj(final ContractAction action) {
        requireNonNull(action);
        return new HistoricalContractAction(
                action.callType(),
                action.callingAccount(),
                action.callingContract(),
                action.gas(),
                action.input(),
                action.recipientAccount(),
                action.recipientContract(),
                action.targetedAddress(),
                action.value(),
                action.gasUsed(),
                action.output(),
                action.revertReason(),
                action.error(),
                action.callDepth(),
                action.callOperationType());
    }

    /** Reconstructs the identical PBJ wire value at the stream boundary. */
    public ContractAction toPbj() {
        final var builder = ContractAction.newBuilder()
                .callType(callType)
                .gas(gas)
                .input(input)
                .value(value)
                .gasUsed(gasUsed)
                .callDepth(callDepth)
                .callOperationType(callOperationType);
        if (callingAccount != null) {
            builder.callingAccount(callingAccount);
        } else if (callingContract != null) {
            builder.callingContract(callingContract);
        }
        if (recipientAccount != null) {
            builder.recipientAccount(recipientAccount);
        } else if (recipientContract != null) {
            builder.recipientContract(recipientContract);
        } else if (targetedAddress != null) {
            builder.targetedAddress(targetedAddress);
        }
        if (output != null) {
            builder.output(output);
        } else if (revertReason != null) {
            builder.revertReason(revertReason);
        } else if (error != null) {
            builder.error(error);
        }
        return builder.build();
    }

    private static void requireAtMostOne(final String group, final Object... values) {
        var present = 0;
        for (final var value : values) {
            if (value != null) {
                present++;
            }
        }
        if (present > 1) {
            throw new IllegalArgumentException(group + " must preserve one-of presence");
        }
    }
}
