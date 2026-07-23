// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.blocks.historical;

import static com.hedera.hapi.streams.CallOperationType.OP_CALL;
import static com.hedera.hapi.streams.CallOperationType.OP_CREATE2;
import static com.hedera.hapi.streams.ContractActionType.CALL;
import static com.hedera.hapi.streams.ContractActionType.CREATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hedera.hapi.node.base.AccountID;
import com.hedera.hapi.node.base.ContractID;
import com.hedera.hapi.streams.ContractAction;
import com.hedera.pbj.runtime.io.buffer.Bytes;
import org.junit.jupiter.api.Test;

class HistoricalContractActionTest {
    private static final AccountID ACCOUNT =
            AccountID.newBuilder().accountNum(1001).build();
    private static final ContractID CONTRACT =
            ContractID.newBuilder().contractNum(2002).build();
    private static final Bytes ADDRESS = Bytes.wrap(new byte[20]);

    @Test
    void roundTripsCallWithLargeNumericBitPatternsAndEmptyOutput() {
        final var action = ContractAction.newBuilder()
                .callType(CALL)
                .callingAccount(ACCOUNT)
                .recipientContract(CONTRACT)
                .gas(Long.MIN_VALUE)
                .gasUsed(Long.MAX_VALUE)
                .value(-1L)
                .input(Bytes.EMPTY)
                .output(Bytes.EMPTY)
                .callDepth(0)
                .callOperationType(OP_CALL)
                .build();

        assertEquals(action, HistoricalContractAction.fromPbj(action).toPbj());
    }

    @Test
    void roundTripsNestedCreateWithTargetAndRevertReason() {
        final var action = ContractAction.newBuilder()
                .callType(CREATE)
                .callingContract(CONTRACT)
                .targetedAddress(ADDRESS)
                .gas(10)
                .gasUsed(9)
                .value(8)
                .input(Bytes.wrap("input"))
                .revertReason(Bytes.wrap("revert"))
                .callDepth(3)
                .callOperationType(OP_CREATE2)
                .build();

        assertEquals(action, HistoricalContractAction.fromPbj(action).toPbj());
    }

    @Test
    void preservesAbsentResultDistinctFromEmptyResult() {
        final var absent = ContractAction.newBuilder()
                .callType(CALL)
                .callingAccount(ACCOUNT)
                .recipientAccount(ACCOUNT)
                .input(Bytes.EMPTY)
                .callOperationType(OP_CALL)
                .build();
        final var empty = absent.copyBuilder().error(Bytes.EMPTY).build();

        assertEquals(absent, HistoricalContractAction.fromPbj(absent).toPbj());
        assertEquals(empty, HistoricalContractAction.fromPbj(empty).toPbj());
    }

    @Test
    void rejectsMalformedTargetAddressAndRelationshipFields() {
        assertThrows(
                IllegalArgumentException.class,
                () -> HistoricalContractAction.fromPbj(ContractAction.newBuilder()
                        .callType(CALL)
                        .callingAccount(ACCOUNT)
                        .targetedAddress(Bytes.wrap(new byte[19]))
                        .input(Bytes.EMPTY)
                        .callOperationType(OP_CALL)
                        .build()));
        assertThrows(
                IllegalArgumentException.class,
                () -> new HistoricalContractAction(
                        CALL, ACCOUNT, CONTRACT, 0, Bytes.EMPTY, null, null, null, 0, 0, null, null, null, 0, OP_CALL));
        assertThrows(
                IllegalArgumentException.class,
                () -> new HistoricalContractAction(
                        CALL,
                        ACCOUNT,
                        null,
                        0,
                        Bytes.EMPTY,
                        null,
                        null,
                        null,
                        0,
                        0,
                        Bytes.EMPTY,
                        null,
                        null,
                        -1,
                        OP_CALL));
    }
}
