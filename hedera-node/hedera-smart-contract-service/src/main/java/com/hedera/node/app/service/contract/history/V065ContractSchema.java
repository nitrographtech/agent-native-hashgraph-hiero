// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.service.contract.history;

import static com.hedera.hapi.util.HapiUtils.SEMANTIC_VERSION_COMPARATOR;

import com.hedera.hapi.node.base.HookId;
import com.hedera.hapi.node.base.SemanticVersion;
import com.hedera.hapi.node.state.contract.SlotValue;
import com.hedera.hapi.node.state.hooks.EvmHookSlotKey;
import com.hedera.hapi.node.state.hooks.EvmHookState;
import com.hedera.hapi.platform.state.StateKey;
import com.swirlds.state.lifecycle.Schema;
import com.swirlds.state.lifecycle.StateDefinition;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Set;

/**
 * The implementation-neutral schema for retained v0.65 hook metadata and storage.
 *
 * <p>The historical {@code LAMBDA_STORAGE} literal is intentionally preserved.
 */
public class V065ContractSchema extends Schema<SemanticVersion> {
    private static final SemanticVersion VERSION =
            SemanticVersion.newBuilder().major(0).minor(65).build();

    public static final String EVM_HOOK_STATES_KEY = "EVM_HOOK_STATES";
    public static final int EVM_HOOK_STATES_STATE_ID =
            StateKey.KeyOneOfType.CONTRACTSERVICE_I_EVM_HOOK_STATES.protoOrdinal();
    public static final String EVM_HOOK_STORAGE_KEY = "LAMBDA_STORAGE";
    public static final int EVM_HOOK_STORAGE_STATE_ID =
            StateKey.KeyOneOfType.CONTRACTSERVICE_I_EVM_HOOK_STORAGE.protoOrdinal();

    public V065ContractSchema() {
        super(VERSION, SEMANTIC_VERSION_COMPARATOR);
    }

    @Override
    public @NonNull Set<StateDefinition> statesToCreate() {
        return Set.of(
                StateDefinition.keyValue(
                        EVM_HOOK_STATES_STATE_ID, EVM_HOOK_STATES_KEY, HookId.PROTOBUF, EvmHookState.PROTOBUF),
                StateDefinition.keyValue(
                        EVM_HOOK_STORAGE_STATE_ID, EVM_HOOK_STORAGE_KEY, EvmHookSlotKey.PROTOBUF, SlotValue.PROTOBUF));
    }
}
