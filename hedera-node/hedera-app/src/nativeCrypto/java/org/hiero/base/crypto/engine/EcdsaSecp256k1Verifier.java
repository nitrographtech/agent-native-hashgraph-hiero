// SPDX-License-Identifier: Apache-2.0
package org.hiero.base.crypto.engine;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.math.BigInteger;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;

/**
 * Native-distribution implementation of the platform secp256k1 verifier.
 *
 * <p>This preserves the platform API while removing the Besu-native implementation from the
 * Nitrograph native artifact. The full distribution continues to use the upstream implementation.
 */
public class EcdsaSecp256k1Verifier {
    public static final int ECDSA_KECCAK_256_SIZE = 32;
    public static final int ECDSA_UNCOMPRESSED_KEY_SIZE = 64;
    public static final int ECDSA_SIGNATURE_SIZE = 64;

    private static final X9ECParameters CURVE = SECNamedCurves.getByName("secp256k1");
    private static final ECDomainParameters DOMAIN =
            new ECDomainParameters(CURVE.getCurve(), CURVE.getG(), CURVE.getN(), CURVE.getH());

    public boolean verify(
            @NonNull final byte[] rawSig,
            @NonNull final byte[] msgHash,
            @NonNull final byte[] pubKey) {
        if (msgHash.length != ECDSA_KECCAK_256_SIZE
                || pubKey.length != ECDSA_UNCOMPRESSED_KEY_SIZE
                || rawSig.length != ECDSA_SIGNATURE_SIZE) {
            return false;
        }
        try {
            final var encodedPublicKey = new byte[ECDSA_UNCOMPRESSED_KEY_SIZE + 1];
            encodedPublicKey[0] = 0x04;
            System.arraycopy(pubKey, 0, encodedPublicKey, 1, pubKey.length);
            final var point = CURVE.getCurve().decodePoint(encodedPublicKey).normalize();
            final var r = new BigInteger(1, rawSig, 0, 32);
            var s = new BigInteger(1, rawSig, 32, 32);
            if (r.signum() == 0
                    || r.compareTo(CURVE.getN()) >= 0
                    || s.signum() == 0
                    || s.compareTo(CURVE.getN()) >= 0) {
                return false;
            }
            final var halfCurveOrder = CURVE.getN().shiftRight(1);
            if (s.compareTo(halfCurveOrder) > 0) {
                s = CURVE.getN().subtract(s);
            }
            final var verifier = new ECDSASigner();
            verifier.init(false, new ECPublicKeyParameters(point, DOMAIN));
            return verifier.verifySignature(msgHash, r, s);
        } catch (final IllegalArgumentException e) {
            return false;
        }
    }
}
