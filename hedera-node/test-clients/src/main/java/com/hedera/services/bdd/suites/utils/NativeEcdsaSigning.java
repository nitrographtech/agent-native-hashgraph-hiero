// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.suites.utils;

import static org.hyperledger.besu.nativelib.secp256k1.LibSecp256k1.CONTEXT;

import com.sun.jna.ptr.IntByReference;
import java.nio.ByteBuffer;
import org.hyperledger.besu.nativelib.secp256k1.LibSecp256k1;

/** ECDSA signing used by retained native authorization tests. */
public final class NativeEcdsaSigning {
    private NativeEcdsaSigning() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static byte[] signMessage(final byte[] messageHash, final byte[] privateKey) {
        final var signature = new LibSecp256k1.secp256k1_ecdsa_recoverable_signature();
        LibSecp256k1.secp256k1_ecdsa_sign_recoverable(CONTEXT, signature, messageHash, privateKey, null, null);

        final var compactSig = ByteBuffer.allocate(64);
        final var recId = new IntByReference(0);
        LibSecp256k1.secp256k1_ecdsa_recoverable_signature_serialize_compact(CONTEXT, compactSig, recId, signature);
        compactSig.flip();

        final var result = new byte[65];
        System.arraycopy(compactSig.array(), 0, result, 0, 64);
        result[64] = (byte) (recId.getValue() + 27);
        return result;
    }
}
