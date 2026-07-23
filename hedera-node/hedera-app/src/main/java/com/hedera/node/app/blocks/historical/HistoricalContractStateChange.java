// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.blocks.historical;

import static java.util.Objects.requireNonNull;

import com.hedera.hapi.node.base.ContractID;
import com.hedera.hapi.streams.ContractStateChange;
import java.util.List;

/** An immutable, non-executable grouping of historical storage changes by contract. */
public record HistoricalContractStateChange(ContractID contractId, List<HistoricalStorageChange> storageChanges) {
    public HistoricalContractStateChange {
        requireNonNull(contractId);
        storageChanges = List.copyOf(requireNonNull(storageChanges));
    }

    /** Creates a neutral snapshot while preserving contract and slot ordering. */
    public static HistoricalContractStateChange fromPbj(final ContractStateChange change) {
        requireNonNull(change);
        return new HistoricalContractStateChange(
                change.contractIdOrThrow(),
                change.storageChanges().stream()
                        .map(HistoricalStorageChange::fromPbj)
                        .toList());
    }

    /** Reconstructs the identical PBJ wire value at the stream boundary. */
    public ContractStateChange toPbj() {
        return new ContractStateChange(
                contractId,
                storageChanges.stream().map(HistoricalStorageChange::toPbj).toList());
    }
}
