// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.service.contract.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.hedera.hapi.node.base.SemanticVersion;
import com.swirlds.state.lifecycle.SchemaRegistry;
import org.junit.jupiter.api.Test;

class HistoricalContractStateServiceTest {
    @Test
    void registersEveryRetainedSchemaVersion() {
        final var registry = mock(SchemaRegistry.class);

        new HistoricalContractStateService().registerSchemas(registry);

        verify(registry).registerAll(isA(V0490ContractSchema.class), isA(V065ContractSchema.class));
    }

    @Test
    void preservesVersionsStateIdsKeysAndCodecs() {
        final var v049 = new V0490ContractSchema();
        final var v065 = new V065ContractSchema();

        assertThat(v049.getVersion()).isEqualTo(version(0, 49, 0));
        assertThat(v065.getVersion()).isEqualTo(version(0, 65, 0));
        assertThat(v049.statesToCreate())
                .extracting("stateKey")
                .containsExactlyInAnyOrder(V0490ContractSchema.STORAGE_KEY, V0490ContractSchema.BYTECODE_KEY);
        assertThat(v065.statesToCreate())
                .extracting("stateKey")
                .containsExactlyInAnyOrder(
                        V065ContractSchema.EVM_HOOK_STATES_KEY, V065ContractSchema.EVM_HOOK_STORAGE_KEY);
        assertThat(V065ContractSchema.EVM_HOOK_STORAGE_KEY).isEqualTo("LAMBDA_STORAGE");
    }

    @Test
    void preservesLegacySchemaClassNamesInNeutralArtifact() throws ReflectiveOperationException {
        final var v049 = Class.forName("com.hedera.node.app.service.contract.impl.schemas.V0490ContractSchema");
        final var v065 = Class.forName("com.hedera.node.app.service.contract.impl.schemas.V065ContractSchema");

        assertThat(v049.getSuperclass()).isEqualTo(V0490ContractSchema.class);
        assertThat(v065.getSuperclass()).isEqualTo(V065ContractSchema.class);
        assertThat(v049.getConstructor().newInstance()).isInstanceOf(V0490ContractSchema.class);
        assertThat(v065.getConstructor().newInstance()).isInstanceOf(V065ContractSchema.class);
    }

    private static SemanticVersion version(final int major, final int minor, final int patch) {
        return SemanticVersion.newBuilder()
                .major(major)
                .minor(minor)
                .patch(patch)
                .build();
    }
}
