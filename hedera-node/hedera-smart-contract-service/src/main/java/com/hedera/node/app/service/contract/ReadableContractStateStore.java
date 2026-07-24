// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.service.contract;

import com.hedera.hapi.node.base.ContractID;
import com.hedera.hapi.node.state.contract.Bytecode;
import com.hedera.hapi.node.state.contract.SlotKey;
import com.hedera.hapi.node.state.contract.SlotValue;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/** Neutral read-only access to retained historical contract bytecode and storage. */
public interface ReadableContractStateStore {
    @Nullable
    Bytecode getBytecode(@NonNull ContractID contractId);

    @Nullable
    SlotValue getSlotValue(@NonNull SlotKey key);

    long getNumSlots();

    long getNumBytecodes();
}
