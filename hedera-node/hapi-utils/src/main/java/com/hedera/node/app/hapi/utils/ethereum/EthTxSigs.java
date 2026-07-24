// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.hapi.utils.ethereum;

import static com.hedera.node.app.hapi.utils.EthSigsUtils.recoverAddressFromPubKey;

import com.esaulpaugh.headlong.rlp.RLPEncoder;
import com.esaulpaugh.headlong.util.Integers;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.math.BigInteger;
import java.util.Arrays;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.math.ec.ECAlgorithms;

public record EthTxSigs(byte[] publicKey, byte[] address) {
    private static final BigInteger N = SECNamedCurves.getByName("secp256k1").getN();

    public static EthTxSigs extractSignatures(EthTxData ethTx) {
        final var message = calculateSignableMessage(ethTx);
        final var pubKey = extractSig(ethTx.recId(), ethTx.r(), ethTx.s(), message);
        final var address = recoverAddressFromPubKey(pubKey);
        final var compressedKey = SECNamedCurves.getByName("secp256k1")
                .getCurve()
                .decodePoint(pubKey)
                .getEncoded(true);
        return new EthTxSigs(compressedKey, address);
    }

    public static byte[] calculateSignableMessage(EthTxData ethTx) {
        return switch (ethTx.type()) {
            case LEGACY_ETHEREUM -> resolveLegacy(ethTx);
            case EIP1559 -> resolveEIP1559(ethTx);
            case EIP2930 -> resolveEIP2930(ethTx);
        };
    }

    // Legacy transactions do not support EIP1559, so only a single gasPrice field is present.
    // Additionally, they do not include access list information.
    static byte[] resolveLegacy(final EthTxData ethTx) {
        return ethTx.chainId() != null && ethTx.chainId().length > 0
                ? RLPEncoder.list(
                        Integers.toBytes(ethTx.nonce()),
                        ethTx.gasPrice(),
                        Integers.toBytes(ethTx.gasLimit()),
                        ethTx.to(),
                        Integers.toBytesUnsigned(ethTx.value()),
                        ethTx.callData(),
                        ethTx.chainId(),
                        Integers.toBytes(0),
                        Integers.toBytes(0))
                : RLPEncoder.list(
                        Integers.toBytes(ethTx.nonce()),
                        ethTx.gasPrice(),
                        Integers.toBytes(ethTx.gasLimit()),
                        ethTx.to(),
                        Integers.toBytesUnsigned(ethTx.value()),
                        ethTx.callData());
    }

    // A notable difference introduced in EIP1559 is the replacement of the gasPrice field
    // with maxPriorityGas and maxGas fields, enabling more granular control over transaction fees.
    // More details: https://eips.ethereum.org/EIPS/eip-1559
    static byte[] resolveEIP1559(final EthTxData ethTx) {
        return RLPEncoder.sequence(Integers.toBytes(2), new Object[] {
            ethTx.chainId(),
            Integers.toBytes(ethTx.nonce()),
            ethTx.maxPriorityGas(),
            ethTx.maxGas(),
            Integers.toBytes(ethTx.gasLimit()),
            ethTx.to(),
            Integers.toBytesUnsigned(ethTx.value()),
            ethTx.callData(),
            ethTx.accessListAsRlp() != null ? ethTx.accessListAsRlp() : new Object[0]
        });
    }

    // EIP2930 introduces the accessList field, which allows specifying a list of
    // addresses and storage keys the transaction will access.
    // More details: https://eips.ethereum.org/EIPS/eip-2930
    static byte[] resolveEIP2930(final EthTxData ethTx) {
        return RLPEncoder.sequence(Integers.toBytes(1), new Object[] {
            ethTx.chainId(),
            Integers.toBytes(ethTx.nonce()),
            ethTx.gasPrice(),
            Integers.toBytes(ethTx.gasLimit()),
            ethTx.to(),
            Integers.toBytesUnsigned(ethTx.value()),
            ethTx.callData(),
            ethTx.accessListAsRlp() != null ? ethTx.accessListAsRlp() : new Object[0]
        });
    }

    private static byte[] extractSig(int recId, byte[] r, byte[] s, byte[] message) {
        // The only meaningful recovery ids are 0 and 1 (even if the high order bytes
        // were used to encode the chain id, the parity is all that matters here)
        recId = Math.floorMod(recId, 2);

        byte[] dataHash = new Keccak.Digest256().digest(message);

        checkInBounds(r);
        checkInBounds(s);
        final var params = SECNamedCurves.getByName("secp256k1");
        final var rValue = new BigInteger(1, r);
        final var sValue = new BigInteger(1, s);
        final var x = rValue;
        if (x.compareTo(params.getCurve().getField().getCharacteristic()) >= 0) {
            throw new IllegalArgumentException("Could not recover signature");
        }
        final var encodedPoint = new byte[33];
        encodedPoint[0] = (byte) (recId == 0 ? 0x02 : 0x03);
        final var xBytes = x.toByteArray();
        System.arraycopy(
                xBytes,
                Math.max(0, xBytes.length - 32),
                encodedPoint,
                33 - Math.min(32, xBytes.length),
                Math.min(32, xBytes.length));
        final var rPoint = params.getCurve().decodePoint(encodedPoint);
        if (!rPoint.multiply(N).isInfinity()) {
            throw new IllegalArgumentException("Could not recover signature");
        }
        final var e = new BigInteger(1, dataHash);
        final var rInv = rValue.modInverse(N);
        return ECAlgorithms.sumOfTwoMultiplies(
                        params.getG(),
                        e.negate().mod(N).multiply(rInv).mod(N),
                        rPoint,
                        sValue.multiply(rInv).mod(N))
                .normalize()
                .getEncoded(false);
    }

    @VisibleForTesting
    static byte[] concatLeftPadded(final byte[] r, final byte[] s) {
        byte[] signature = new byte[64];
        final var rLeadingZeros = 32 - r.length;
        System.arraycopy(r, 0, signature, rLeadingZeros, r.length);
        final var sLeadingZeros = 32 - s.length;
        System.arraycopy(s, 0, signature, 32 + sLeadingZeros, s.length);
        return signature;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        final EthTxSigs ethTxSigs = (EthTxSigs) other;

        if (!Arrays.equals(publicKey, ethTxSigs.publicKey)) return false;
        return Arrays.equals(address, ethTxSigs.address);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(publicKey);
        result = 31 * result + Arrays.hashCode(address);
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("publicKey", Hex.encodeHexString(publicKey))
                .add("address", Hex.encodeHexString(address))
                .toString();
    }

    /**
     * Returns whether the given curve point is in bounds for the Secp256k1 curve.
     * @param curvePoint the curve point to check
     */
    private static void checkInBounds(@NonNull byte[] curvePoint) {
        final var bi = new BigInteger(1, curvePoint);
        if (bi.compareTo(BigInteger.ONE) < 0) {
            throw new IllegalArgumentException("Curve point must be >= 1");
        }
        if (bi.compareTo(N) >= 0) {
            throw new IllegalArgumentException("Curve point must be < N");
        }
    }
}
