// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.fixtures.p06a;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.MessageDigest;
import java.security.SecureRandom;
import org.hiero.base.crypto.DetRandomProvider;
import org.hiero.base.crypto.SigningSchema;
import org.hiero.consensus.crypto.KeysAndCertsGenerator;
import org.hiero.consensus.model.node.KeysAndCerts;
import org.hiero.consensus.model.node.NodeId;

/**
 * Generates the signing identity used only by the P06A historical fixture harness.
 *
 * <p>THIS IDENTITY IS PUBLIC, COMPROMISED BY DESIGN, AND MUST NEVER BE USED OUTSIDE
 * THE P06A FIXTURE HARNESS.
 */
public final class P06aPublicFixtureIdentity {
    public static final String P06A_PUBLIC_FIXTURE_IDENTITY_SEED =
            "NITROGRAPH-P06A-PUBLIC-COMPROMISED-FIXTURE-IDENTITY-V1";
    private static final String WORKFLOW_ENV = "P06A_PUBLIC_FIXTURE_WORKFLOW";
    private static final String WORKFLOW_VALUE = "explicitly-enabled";
    private static final long EXPECTED_NETWORK_ID = 123L;
    private static final long MINIMUM_NODE_ID = 0L;
    private static final long MAXIMUM_NODE_ID = 3L;

    private P06aPublicFixtureIdentity() {}

    /**
     * Returns a deterministic, compromised-by-design identity for the isolated four-node
     * P06A fixture harness.
     *
     * <p>THIS IDENTITY IS PUBLIC, COMPROMISED BY DESIGN, AND MUST NEVER BE USED OUTSIDE
     * THE P06A FIXTURE HARNESS.
     */
    public static KeysAndCerts fixtureKeysAndCerts(final long networkId, final long nodeId) {
        if (!WORKFLOW_VALUE.equals(System.getenv(WORKFLOW_ENV))) {
            throw new IllegalArgumentException(WORKFLOW_ENV + "=" + WORKFLOW_VALUE + " is required");
        }
        if (networkId != EXPECTED_NETWORK_ID) {
            throw new IllegalArgumentException("Fixture network id must be " + EXPECTED_NETWORK_ID);
        }
        if (nodeId < MINIMUM_NODE_ID || nodeId > MAXIMUM_NODE_ID) {
            throw new IllegalArgumentException(
                    "Fixture node id must be between " + MINIMUM_NODE_ID + " and " + MAXIMUM_NODE_ID);
        }
        try {
            return KeysAndCertsGenerator.generate(
                    NodeId.of(nodeId),
                    SigningSchema.RSA,
                    deterministicRandom("signing", networkId, nodeId),
                    deterministicRandom("agreement", networkId, nodeId));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to derive P06A public fixture identity", e);
        }
    }

    private static SecureRandom deterministicRandom(final String purpose, final long networkId, final long nodeId)
            throws Exception {
        final byte[] seed = MessageDigest.getInstance("SHA-384")
                .digest((P06A_PUBLIC_FIXTURE_IDENTITY_SEED
                                + "|"
                                + purpose
                                + "|network="
                                + networkId
                                + "|node="
                                + nodeId)
                        .getBytes(UTF_8));
        final SecureRandom random = DetRandomProvider.getDetRandom();
        random.setSeed(seed);
        return random;
    }

}
