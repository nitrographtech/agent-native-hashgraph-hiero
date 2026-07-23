// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.service.contract.history;

import static com.hedera.hapi.util.HapiUtils.SEMANTIC_VERSION_COMPARATOR;

import com.hedera.hapi.node.base.ContractID;
import com.hedera.hapi.node.base.SemanticVersion;
import com.hedera.hapi.node.state.contract.Bytecode;
import com.hedera.hapi.node.state.contract.SlotKey;
import com.hedera.hapi.node.state.contract.SlotValue;
import com.hedera.hapi.platform.state.StateKey;
import com.swirlds.state.lifecycle.Schema;
import com.swirlds.state.lifecycle.StateDefinition;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Set;

/**
 * The implementation-neutral schema for retained contract bytecode and storage.
 *
 * <p>These definitions preserve the exact service state identifiers used since v0.49.0. Registering
 * them makes historical state readable; it does not provide executable contract behavior.
 */
public class V0490ContractSchema extends Schema<SemanticVersion> {
    private static final SemanticVersion VERSION =
            SemanticVersion.newBuilder().major(0).minor(49).patch(0).build();

    public static final String STORAGE_KEY = "STORAGE";
    public static final int STORAGE_STATE_ID = StateKey.KeyOneOfType.CONTRACTSERVICE_I_STORAGE.protoOrdinal();
    public static final String BYTECODE_KEY = "BYTECODE";
    public static final int BYTECODE_STATE_ID = StateKey.KeyOneOfType.CONTRACTSERVICE_I_BYTECODE.protoOrdinal();

    public V0490ContractSchema() {
        super(VERSION, SEMANTIC_VERSION_COMPARATOR);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public @NonNull Set<StateDefinition> statesToCreate() {
        return Set.of(
                StateDefinition.keyValue(STORAGE_STATE_ID, STORAGE_KEY, SlotKey.PROTOBUF, SlotValue.PROTOBUF),
                StateDefinition.keyValue(BYTECODE_STATE_ID, BYTECODE_KEY, ContractID.PROTOBUF, Bytecode.PROTOBUF));
    }
}
