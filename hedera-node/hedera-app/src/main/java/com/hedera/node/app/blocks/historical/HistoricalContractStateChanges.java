// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.blocks.historical;

import static java.util.Objects.requireNonNull;

import com.hedera.hapi.streams.ContractStateChanges;
import java.util.List;

/** An immutable, non-executable snapshot of a historical contract state-change sidecar. */
public record HistoricalContractStateChanges(List<HistoricalContractStateChange> contractStateChanges) {
    public HistoricalContractStateChanges {
        contractStateChanges = List.copyOf(requireNonNull(contractStateChanges));
    }

    /** Creates a neutral snapshot while preserving contract and storage-change ordering. */
    public static HistoricalContractStateChanges fromPbj(final ContractStateChanges changes) {
        requireNonNull(changes);
        return new HistoricalContractStateChanges(changes.contractStateChanges().stream()
                .map(HistoricalContractStateChange::fromPbj)
                .toList());
    }

    /** Reconstructs the identical PBJ wire value at the stream boundary. */
    public ContractStateChanges toPbj() {
        return new ContractStateChanges(contractStateChanges.stream()
                .map(HistoricalContractStateChange::toPbj)
                .toList());
    }

    /** Returns a copy with writes removed, preserving read values for reverted transactions. */
    public HistoricalContractStateChanges withoutWrittenValues() {
        return new HistoricalContractStateChanges(contractStateChanges.stream()
                .map(change -> new HistoricalContractStateChange(
                        change.contractId(),
                        change.storageChanges().stream()
                                .map(storage -> new HistoricalStorageChange(storage.slot(), storage.valueRead(), null))
                                .toList()))
                .toList());
    }
}
