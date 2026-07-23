// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.authorization;

import static com.hedera.node.app.authorization.EntityAddressUtils.entityNumberOfLongZero;
import static com.hedera.node.app.authorization.EntityAddressUtils.isLongZeroAddress;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EntityAddressUtilsTest {
    @Test
    void preservesLongZeroGoldenVectors() {
        assertThat(entityNumberOfLongZero(addressOf(0))).isZero();
        assertThat(entityNumberOfLongZero(addressOf(424242))).isEqualTo(424242);
        assertThat(entityNumberOfLongZero(addressOf(-1L))).isEqualTo(-1L);
    }

    @Test
    void rejectsNonCanonicalWidthsAndPrefixes() {
        assertThat(isLongZeroAddress(new byte[19])).isFalse();
        assertThat(isLongZeroAddress(new byte[21])).isFalse();
        final var nonZeroPrefix = addressOf(1);
        nonZeroPrefix[0] = 1;
        assertThat(isLongZeroAddress(nonZeroPrefix)).isFalse();
        assertThatThrownBy(() -> entityNumberOfLongZero(nonZeroPrefix)).isInstanceOf(IllegalArgumentException.class);
    }

    private static byte[] addressOf(final long number) {
        final var address = new byte[20];
        for (int i = 0; i < Long.BYTES; i++) {
            address[address.length - 1 - i] = (byte) (number >>> (Byte.SIZE * i));
        }
        return address;
    }
}
