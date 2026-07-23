// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.blocks.historical;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.hedera.hapi.node.base.ContractID;
import com.hedera.hapi.streams.ContractBytecode;
import com.hedera.pbj.runtime.io.buffer.Bytes;
import org.junit.jupiter.api.Test;

class HistoricalContractBytecodeTest {
    private static final ContractID CONTRACT_ID =
            ContractID.newBuilder().contractNum(1234).build();

    @Test
    void preservesInitcodeRuntimeCodeAndContractAssociation() {
        final var source = ContractBytecode.newBuilder()
                .contractId(CONTRACT_ID)
                .initcode(Bytes.wrap(new byte[] {0, 1, 2}))
                .runtimeBytecode(Bytes.wrap(new byte[] {0, 3, 4}))
                .build();

        final var subject = HistoricalContractBytecode.fromPbj(source);

        assertThat(subject.contractId()).isEqualTo(CONTRACT_ID);
        assertThat(subject.initcode()).isEqualTo(source.initcode());
        assertThat(subject.runtimeBytecode()).isEqualTo(source.runtimeBytecode());
        assertThat(subject.toPbj()).isEqualTo(source);
    }

    @Test
    void preservesMissingContractAndEmptyByteFields() {
        final var source = ContractBytecode.DEFAULT;

        final var subject = HistoricalContractBytecode.fromPbj(source);

        assertThat(subject.contractId()).isNull();
        assertThat(subject.initcode()).isEqualTo(Bytes.EMPTY);
        assertThat(subject.runtimeBytecode()).isEqualTo(Bytes.EMPTY);
        assertThat(subject.toPbj()).isEqualTo(source);
    }

    @Test
    void preservesInitcodeOnlyAndRuntimeOnly() {
        final var initcodeOnly =
                ContractBytecode.newBuilder().initcode(Bytes.fromHex("000102")).build();
        final var runtimeOnly = ContractBytecode.newBuilder()
                .contractId(CONTRACT_ID)
                .runtimeBytecode(Bytes.fromHex("000304"))
                .build();

        assertThat(HistoricalContractBytecode.fromPbj(initcodeOnly).toPbj()).isEqualTo(initcodeOnly);
        assertThat(HistoricalContractBytecode.fromPbj(runtimeOnly).toPbj()).isEqualTo(runtimeOnly);
    }

    @Test
    void preservesLargeBytesWithoutExecutionOrValidation() {
        final var large = Bytes.wrap(new byte[1024 * 1024]);
        final var source = ContractBytecode.newBuilder()
                .contractId(CONTRACT_ID)
                .initcode(large)
                .runtimeBytecode(large)
                .build();

        final var subject = HistoricalContractBytecode.fromPbj(source);

        assertThat(subject.initcode()).isEqualTo(large);
        assertThat(subject.runtimeBytecode()).isEqualTo(large);
    }

    @Test
    void rejectsNullByteFields() {
        assertThatNullPointerException()
                .isThrownBy(() -> new HistoricalContractBytecode(CONTRACT_ID, null, Bytes.EMPTY));
        assertThatNullPointerException()
                .isThrownBy(() -> new HistoricalContractBytecode(CONTRACT_ID, Bytes.EMPTY, null));
    }
}
