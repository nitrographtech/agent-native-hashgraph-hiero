// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.hapi.utils;

import com.hedera.pbj.runtime.io.buffer.Bytes;
import java.security.MessageDigest;
import java.util.Arrays;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jcajce.provider.digest.Keccak;

public class MiscCryptoUtils {
    private static final int EVM_ADDRESS_SIZE = 20;

    /** Length of an uncompressed ECDSA public key */
    private static final int ECDSA_UNCOMPRESSED_KEY_SIZE = 64;

    /** Length of an uncompressed ECDSA public key including a header byte */
    private static final int ECDSA_UNCOMPRESSED_KEY_SIZE_WITH_HEADER_BYTE = ECDSA_UNCOMPRESSED_KEY_SIZE + 1;

    private static final X9ECParameters SECP256K1 = SECNamedCurves.getByName("secp256k1");

    private MiscCryptoUtils() {
        throw new UnsupportedOperationException("Utility Class");
    }

    public static byte[] keccak256DigestOf(final byte[] msg) {
        return new Keccak.Digest256().digest(msg);
    }

    public static Bytes keccak256DigestOf(final Bytes msg) {
        final MessageDigest digest = new Keccak.Digest256();
        msg.writeTo(digest);
        return Bytes.wrap(digest.digest());
    }

    /**
     * Given a 33-byte compressed ECDSA(secp256k1) public key, returns the uncompressed key as a
     * 64-byte array whose first 32 bytes are the x-coordinate of the key and second 32 bytes are
     * the y-coordinate of the key.
     *
     * @param compressedKey a compressed ECDSA(secp256k1) public key
     * @return the raw bytes of the public key coordinates
     * @throws IllegalArgumentException if the compressed key not parsable
     */
    public static byte[] decompressSecp256k1(final byte[] compressedKey) {
        try {
            final var encoded =
                    SECP256K1.getCurve().decodePoint(compressedKey).normalize().getEncoded(false);
            return Arrays.copyOfRange(encoded, 1, encoded.length);
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to parse public key", e);
        }
    }

    /**
     * Given a 64-byte decompressed ECDSA(secp256k1) public key, returns the compressed key as a
     * 33-byte array whose first byte is the parity of the y coordinate and the following 32 bytes
     * are the x-coordinate of the key.
     *
     * @param decompressedKey a decompressed ECDSA(secp256k1) public key
     * @return the raw bytes of the compressed public key
     * @throws IllegalArgumentException if the decompressed key not parsable
     */
    public static byte[] compressSecp256k1(final byte[] decompressedKey) {
        final byte[] decompressedBytes = new byte[ECDSA_UNCOMPRESSED_KEY_SIZE_WITH_HEADER_BYTE];
        decompressedBytes[0] = 0x04;
        System.arraycopy(decompressedKey, 0, decompressedBytes, 1, ECDSA_UNCOMPRESSED_KEY_SIZE);
        try {
            return SECP256K1
                    .getCurve()
                    .decodePoint(decompressedBytes)
                    .normalize()
                    .getEncoded(true);
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to parse public key", e);
        }
    }

    /**
     * Given a 64-byte decompressed ECDSA(secp256k1) public key, returns the evm address
     * derived from the last 20 bytes of the keccak256 hash of the public key.
     *
     * @param decompressedKey a decompressed ECDSA(secp256k1) public key
     * @return the raw bytes of the evm address derived from that key
     */
    public static byte[] extractEvmAddressFromDecompressedECDSAKey(final byte[] decompressedKey) {
        final var publicKeyHash = MiscCryptoUtils.keccak256DigestOf(decompressedKey);
        return Arrays.copyOfRange(publicKeyHash, publicKeyHash.length - EVM_ADDRESS_SIZE, publicKeyHash.length);
    }
}
