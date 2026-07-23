// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.blocks.historical;

import static java.util.Objects.requireNonNull;

import com.hedera.hapi.node.base.AccountID;
import com.hedera.hapi.node.base.ContractID;
import com.hedera.hapi.node.contract.ContractNonceInfo;
import com.hedera.pbj.runtime.io.buffer.Bytes;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.List;

/**
 * An immutable, non-executable snapshot of the data needed to translate a
 * historical contract result.
 *
 * <p>Nullable contextual fields deliberately remain nullable so translation
 * preserves absence versus an explicitly present empty value. Byte values are
 * retained verbatim. Gas and value fields are historical unsigned protobuf
 * values represented with their original {@code long} bit patterns.
 */
public record HistoricalContractResult(
        @Nullable AccountID senderId,
        @Nullable ContractID contractId,
        Bytes returnData,
        String errorMessage,
        long gasUsed,
        @Nullable HistoricalCallContext callContext,
        @Nullable Long signerNonce,
        @Nullable List<ContractID> createdContractIds,
        @Nullable Bytes evmAddress,
        List<ContractNonceInfo> contractNonces,
        List<HistoricalLog> logs) {
    public HistoricalContractResult {
        requireNonNull(returnData);
        requireNonNull(errorMessage);
        contractNonces = List.copyOf(requireNonNull(contractNonces));
        logs = List.copyOf(requireNonNull(logs));
        if (createdContractIds != null) {
            createdContractIds = List.copyOf(createdContractIds);
        }
    }
}
