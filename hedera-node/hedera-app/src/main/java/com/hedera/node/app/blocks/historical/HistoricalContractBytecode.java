// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.blocks.historical;

import static java.util.Objects.requireNonNull;

import com.hedera.hapi.node.base.ContractID;
import com.hedera.hapi.streams.ContractBytecode;
import com.hedera.pbj.runtime.io.buffer.Bytes;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * An immutable, non-executable snapshot of a historical contract bytecode sidecar.
 *
 * <p>Initcode and runtime bytecode are retained verbatim. The protobuf byte
 * fields have no presence distinction, so an absent wire value is represented
 * by {@link Bytes#EMPTY}; contract identity presence remains nullable.
 */
public record HistoricalContractBytecode(@Nullable ContractID contractId, Bytes initcode, Bytes runtimeBytecode) {
    public HistoricalContractBytecode {
        requireNonNull(initcode);
        requireNonNull(runtimeBytecode);
    }

    /** Creates a neutral snapshot without interpreting or validating executable code. */
    public static HistoricalContractBytecode fromPbj(final ContractBytecode bytecode) {
        requireNonNull(bytecode);
        return new HistoricalContractBytecode(bytecode.contractId(), bytecode.initcode(), bytecode.runtimeBytecode());
    }

    /** Reconstructs the identical PBJ wire value at the stream boundary. */
    public ContractBytecode toPbj() {
        final var builder = ContractBytecode.newBuilder().initcode(initcode).runtimeBytecode(runtimeBytecode);
        if (contractId != null) {
            builder.contractId(contractId);
        }
        return builder.build();
    }
}
