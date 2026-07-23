// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.blocks.historical;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hedera.hapi.node.base.ContractID;
import com.hedera.hapi.streams.ContractStateChange;
import com.hedera.hapi.streams.ContractStateChanges;
import com.hedera.hapi.streams.StorageChange;
import com.hedera.pbj.runtime.io.buffer.Bytes;
import java.util.List;
import org.junit.jupiter.api.Test;

class HistoricalContractStateChangesTest {
    private static final ContractID CONTRACT_1 =
            ContractID.newBuilder().contractNum(1234).build();
    private static final ContractID CONTRACT_2 =
            ContractID.newBuilder().contractNum(5678).build();

    @Test
    void preservesOrderingLeadingZerosAndWrittenValuePresence() {
        final var readOnly = new StorageChange(Bytes.wrap(new byte[] {0, 1}), Bytes.EMPTY, null);
        final var explicitZeroWrite =
                new StorageChange(Bytes.wrap(new byte[] {2}), Bytes.wrap(new byte[] {0, 3}), Bytes.EMPTY);
        final var maximumWords =
                new StorageChange(Bytes.wrap(new byte[32]), Bytes.wrap(new byte[32]), Bytes.wrap(new byte[32]));
        final var source = new ContractStateChanges(List.of(
                new ContractStateChange(CONTRACT_1, List.of(readOnly, explicitZeroWrite)),
                new ContractStateChange(CONTRACT_2, List.of(maximumWords))));

        final var subject = HistoricalContractStateChanges.fromPbj(source);

        assertThat(subject.toPbj()).isEqualTo(source);
        assertThat(subject.contractStateChanges())
                .extracting(HistoricalContractStateChange::contractId)
                .containsExactly(CONTRACT_1, CONTRACT_2);
        assertThat(subject.contractStateChanges().getFirst().storageChanges())
                .extracting(HistoricalStorageChange::slot)
                .containsExactly(readOnly.slot(), explicitZeroWrite.slot());
        assertThat(subject.contractStateChanges()
                        .getFirst()
                        .storageChanges()
                        .getFirst()
                        .valueWritten())
                .isNull();
        assertThat(subject.contractStateChanges()
                        .getFirst()
                        .storageChanges()
                        .get(1)
                        .valueWritten())
                .isEqualTo(Bytes.EMPTY);
    }

    @Test
    void removesOnlyWrittenPresenceForRevertedTransactions() {
        final var source = new ContractStateChanges(List.of(new ContractStateChange(
                CONTRACT_1, List.of(new StorageChange(Bytes.fromHex("01"), Bytes.fromHex("02"), Bytes.EMPTY)))));

        final var reverted = HistoricalContractStateChanges.fromPbj(source).withoutWrittenValues();

        assertThat(reverted.contractStateChanges()
                        .getFirst()
                        .storageChanges()
                        .getFirst()
                        .slot())
                .isEqualTo(Bytes.fromHex("01"));
        assertThat(reverted.contractStateChanges()
                        .getFirst()
                        .storageChanges()
                        .getFirst()
                        .valueRead())
                .isEqualTo(Bytes.fromHex("02"));
        assertThat(reverted.contractStateChanges()
                        .getFirst()
                        .storageChanges()
                        .getFirst()
                        .valueWritten())
                .isNull();
    }

    @Test
    void rejectsOversizedSlotOrValues() {
        final var oversized = Bytes.wrap(new byte[33]);
        assertThatThrownBy(() -> new HistoricalStorageChange(oversized, Bytes.EMPTY, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("slot");
        assertThatThrownBy(() -> new HistoricalStorageChange(Bytes.EMPTY, oversized, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valueRead");
        assertThatThrownBy(() -> new HistoricalStorageChange(Bytes.EMPTY, Bytes.EMPTY, oversized))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valueWritten");
    }

    @Test
    void snapshotsInputLists() {
        final var mutable = new java.util.ArrayList<HistoricalStorageChange>();
        final var stateChange = new HistoricalContractStateChange(CONTRACT_1, mutable);
        mutable.add(new HistoricalStorageChange(Bytes.EMPTY, Bytes.EMPTY, null));

        assertThat(stateChange.storageChanges()).isEmpty();
        assertThatThrownBy(() ->
                        stateChange.storageChanges().add(new HistoricalStorageChange(Bytes.EMPTY, Bytes.EMPTY, null)))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
