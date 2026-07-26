// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.spec.assertions;

import static com.hedera.services.bdd.spec.assertions.HistoricalStorageEncoding.formattedAssertionValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;

class HistoricalStorageEncodingTest {
    @Test
    void encodesUnsignedValuesAtMinimalWidth() {
        assertEquals(ByteString.EMPTY, formattedAssertionValue(0));
        assertEquals(ByteString.copyFrom(new byte[] {0x06, 0x79, 0x32}), formattedAssertionValue(424242));
        assertEquals(
                ByteString.copyFrom(new byte[] {0x7f, -1, -1, -1, -1, -1, -1, -1}),
                formattedAssertionValue(Long.MAX_VALUE));
    }

    @Test
    void normalizesHexWithoutChangingUnsignedSemantics() {
        assertEquals(ByteString.EMPTY, formattedAssertionValue(""));
        assertEquals(ByteString.EMPTY, formattedAssertionValue("0x00"));
        assertEquals(ByteString.copyFrom(new byte[] {0x0f}), formattedAssertionValue("0x000f"));
        assertEquals(ByteString.copyFrom(new byte[] {0x0f}), formattedAssertionValue("f"));
        assertEquals(ByteString.copyFrom(new byte[] {(byte) 0x80}), formattedAssertionValue("80"));
    }

    @Test
    void rejectsNegativeMalformedAndOverWidthValues() {
        assertThrows(IllegalArgumentException.class, () -> formattedAssertionValue(-1));
        assertThrows(IllegalArgumentException.class, () -> formattedAssertionValue("0xgg"));
        assertThrows(IllegalArgumentException.class, () -> formattedAssertionValue("01".repeat(33)));
    }
}
