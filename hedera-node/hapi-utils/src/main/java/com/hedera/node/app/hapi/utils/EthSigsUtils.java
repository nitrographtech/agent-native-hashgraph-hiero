// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.hapi.utils;

import static java.lang.System.arraycopy;
import static java.util.Objects.requireNonNull;

import com.hedera.pbj.runtime.io.buffer.Bytes;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.math.BigInteger;
import java.util.Arrays;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jcajce.provider.digest.Keccak;

/**
 * Utility class for recovering EVM addresses from keys.
 */
public final class EthSigsUtils {
    private static final X9ECParameters SECP256K1 = SECNamedCurves.getByName("secp256k1");

    private EthSigsUtils() {}

    /**
     * Recover the address from a private key.
     * @param privateKeyBytes The private key bytes.
     * @return The address.
     */
    public static byte[] recoverAddressFromPrivateKey(@NonNull final byte[] privateKeyBytes) {
        requireNonNull(privateKeyBytes);
        // Create public key from private key
        // Return address from public key
        final var privateKey = new BigInteger(1, privateKeyBytes);
        if (privateKey.signum() == 0 || privateKey.compareTo(SECP256K1.getN()) >= 0) {
            return new byte[0];
        }
        return recoverAddressFromUncompressedPubKey(
                SECP256K1.getG().multiply(privateKey).normalize().getEncoded(false));
    }

    /**
     * Recover the address from a public key.
     * @param pubKeyBytes The public key bytes.
     * @return The address.
     */
    public static byte[] recoverAddressFromPubKey(@NonNull final byte[] pubKeyBytes) {
        requireNonNull(pubKeyBytes);
        try {
            return recoverAddressFromUncompressedPubKey(
                    SECP256K1.getCurve().decodePoint(pubKeyBytes).normalize().getEncoded(false));
        } catch (final IllegalArgumentException e) {
            return new byte[0];
        }
    }

    /**
     * Recover the address from a public key.
     * @param pubKeyBytes The public key bytes.
     * @return The address.
     */
    public static Bytes recoverAddressFromPubKey(@NonNull final Bytes pubKeyBytes) {
        requireNonNull(pubKeyBytes);
        return Bytes.wrap(recoverAddressFromPubKey(pubKeyBytes.toByteArray()));
    }

    /**
     * Recover the address from a public key.
     * @param encoded The uncompressed public key.
     * @return The address.
     */
    private static byte[] recoverAddressFromUncompressedPubKey(@NonNull final byte[] encoded) {
        requireNonNull(encoded);
        final var preHash = Arrays.copyOfRange(encoded, 1, encoded.length);
        var keyHash = new Keccak.Digest256().digest(preHash);
        var address = new byte[20];
        arraycopy(keyHash, 12, address, 0, 20);
        return address;
    }
}
