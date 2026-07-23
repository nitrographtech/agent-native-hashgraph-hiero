// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.authorization;

import static java.util.Objects.requireNonNull;

/**
 * Neutral utilities for the 20-byte, long-zero entity-address convention.
 *
 * <p>This class performs only byte inspection. It has no Besu, Tuweni, or EVM execution dependency.
 */
final class EntityAddressUtils {
    private static final int ADDRESS_LENGTH = 20;
    private static final int ENTITY_NUMBER_OFFSET = ADDRESS_LENGTH - Long.BYTES;

    private EntityAddressUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    static boolean isLongZeroAddress(final byte[] address) {
        requireNonNull(address);
        if (address.length != ADDRESS_LENGTH) {
            return false;
        }
        for (int i = 0; i < ENTITY_NUMBER_OFFSET; i++) {
            if (address[i] != 0) {
                return false;
            }
        }
        return true;
    }

    static long entityNumberOfLongZero(final byte[] address) {
        requireNonNull(address);
        if (!isLongZeroAddress(address)) {
            throw new IllegalArgumentException("Address is not a 20-byte long-zero entity address");
        }
        long number = 0;
        for (int i = ENTITY_NUMBER_OFFSET; i < ADDRESS_LENGTH; i++) {
            number = (number << Byte.SIZE) | Byte.toUnsignedLong(address[i]);
        }
        return number;
    }
}
