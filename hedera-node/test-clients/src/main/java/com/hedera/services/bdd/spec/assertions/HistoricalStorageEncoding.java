// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.spec.assertions;

import com.google.protobuf.ByteString;
import java.math.BigInteger;
import java.util.HexFormat;

/** Exact encodings used when validating storage values from historical contract sidecars. */
public final class HistoricalStorageEncoding {
    private HistoricalStorageEncoding() {
        throw new UnsupportedOperationException("Utility class");
    }

    /** Returns the unsigned, minimal-width big-endian representation of a non-negative value. */
    public static ByteString formattedAssertionValue(final long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Historical storage values must be non-negative");
        }
        if (value == 0) {
            return ByteString.EMPTY;
        }
        final var signed = BigInteger.valueOf(value).toByteArray();
        return signed[0] == 0 ? ByteString.copyFrom(signed, 1, signed.length - 1) : ByteString.copyFrom(signed);
    }

    /** Returns the unsigned, minimal-width encoding of a hexadecimal uint256 value. */
    public static ByteString formattedAssertionValue(final String hexValue) {
        final var unprefixed =
                hexValue.startsWith("0x") || hexValue.startsWith("0X") ? hexValue.substring(2) : hexValue;
        if (unprefixed.length() > 64) {
            throw new IllegalArgumentException("Historical storage values must fit in uint256");
        }
        final var evenLength = (unprefixed.length() & 1) == 0 ? unprefixed : "0" + unprefixed;
        final byte[] decoded;
        try {
            decoded = HexFormat.of().parseHex(evenLength);
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("Malformed hexadecimal storage value", e);
        }
        var firstNonZero = 0;
        while (firstNonZero < decoded.length && decoded[firstNonZero] == 0) {
            firstNonZero++;
        }
        return ByteString.copyFrom(decoded, firstNonZero, decoded.length - firstNonZero);
    }
}
