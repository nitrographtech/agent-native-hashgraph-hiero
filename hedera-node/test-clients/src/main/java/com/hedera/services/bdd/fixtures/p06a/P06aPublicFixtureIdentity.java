// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.fixtures.p06a;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.swirlds.platform.crypto.EnhancedKeyStoreLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Security;
import java.util.HexFormat;
import java.util.Map;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
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

    public static void main(final String... args) throws Exception {
        final Map<String, String> options = parse(args);
        requireFlag(options, "--enable-public-p06a-fixture-identity");
        requireFlag(options, "--acknowledge-compromised-test-key");
        if (!WORKFLOW_VALUE.equals(System.getenv(WORKFLOW_ENV))) {
            throw new IllegalArgumentException(WORKFLOW_ENV + "=" + WORKFLOW_VALUE + " is required");
        }
        final long networkId = parseExact(options, "--network-id", EXPECTED_NETWORK_ID);
        final long nodeId = parseAllowedNodeId(options, "--node-id");
        final Path output = Path.of(required(options, "--output-directory"))
                .toAbsolutePath()
                .normalize();
        if (!Files.isDirectory(output)) {
            throw new IllegalArgumentException("Output must be an existing caller-created directory");
        }
        final String nodeName = "node" + (nodeId + 1);
        final Path privatePem = output.resolve("s-private-" + nodeName + ".pem");
        final Path publicPem = output.resolve("s-public-" + nodeName + ".pem");
        if (Files.exists(privatePem) || Files.exists(publicPem)) {
            throw new IllegalArgumentException("Refusing to overwrite fixture identity material");
        }

        Security.addProvider(new BouncyCastleProvider());
        final var keysAndCerts = fixtureKeysAndCerts(networkId, nodeId);
        EnhancedKeyStoreLoader.writePemFile(
                true, privatePem, keysAndCerts.sigKeyPair().getPrivate().getEncoded());
        EnhancedKeyStoreLoader.writePemFile(
                false, publicPem, keysAndCerts.sigCert().getEncoded());
        final String fingerprint = HexFormat.of()
                .formatHex(MessageDigest.getInstance("SHA-256")
                        .digest(keysAndCerts.sigCert().getEncoded()));
        System.out.println("P06A compromised public fixture certificate SHA-256: " + fingerprint);
        System.out.println("Generated identity for fixture network " + networkId + ", node " + nodeId);
        System.out.println("Private material exists only in the caller-specified temporary directory.");
    }

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

    private static Map<String, String> parse(final String[] args) {
        final Map<String, String> options = new java.util.HashMap<>();
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("--enable-public-p06a-fixture-identity")
                    || arg.equals("--acknowledge-compromised-test-key")) {
                options.put(arg, "true");
            } else if (arg.startsWith("--") && i + 1 < args.length) {
                options.put(arg, args[++i]);
            } else {
                throw new IllegalArgumentException("Unknown or incomplete option: " + arg);
            }
        }
        return options;
    }

    private static void requireFlag(final Map<String, String> options, final String flag) {
        if (!"true".equals(options.get(flag))) {
            throw new IllegalArgumentException(flag + " is required");
        }
    }

    private static String required(final Map<String, String> options, final String option) {
        final String value = options.get(option);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(option + " is required");
        }
        return value;
    }

    private static long parseExact(final Map<String, String> options, final String option, final long expected) {
        final long value = Long.parseLong(required(options, option));
        if (value != expected) {
            throw new IllegalArgumentException(
                    option + " must be " + expected + " for the isolated P06A fixture harness");
        }
        return value;
    }

    private static long parseAllowedNodeId(final Map<String, String> options, final String option) {
        final long value = Long.parseLong(required(options, option));
        if (value < MINIMUM_NODE_ID || value > MAXIMUM_NODE_ID) {
            throw new IllegalArgumentException(option + " must be between " + MINIMUM_NODE_ID + " and "
                    + MAXIMUM_NODE_ID + " for the isolated P06A fixture harness");
        }
        return value;
    }
}
