// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.fixturetooling.p06a;

import com.hedera.services.bdd.fixtures.p06a.P06aPublicFixtureIdentity;
import com.swirlds.platform.crypto.EnhancedKeyStoreLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.Security;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Writes the compromised-by-design signing identity used only by the P06A fixture harness.
 *
 * <p>THIS IDENTITY IS PUBLIC TEST MATERIAL AND MUST NEVER BE USED OUTSIDE THE P06A FIXTURE
 * HARNESS.
 */
public final class P06aFixtureIdentityGenerator {
    private static final String WORKFLOW_ENV = "P06A_PUBLIC_FIXTURE_WORKFLOW";
    private static final String WORKFLOW_VALUE = "explicitly-enabled";
    private static final long EXPECTED_NETWORK_ID = 123L;

    private P06aFixtureIdentityGenerator() {}

    public static void main(final String... args) throws Exception {
        final Map<String, String> options = parse(args);
        requireFlag(options, "--enable-public-p06a-fixture-identity");
        requireFlag(options, "--acknowledge-compromised-test-key");
        if (!WORKFLOW_VALUE.equals(System.getenv(WORKFLOW_ENV))) {
            throw new IllegalArgumentException(WORKFLOW_ENV + "=" + WORKFLOW_VALUE + " is required");
        }
        final long networkId = parseExact(options, "--network-id", EXPECTED_NETWORK_ID);
        final long nodeId = Long.parseLong(required(options, "--node-id"));
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
        final var keysAndCerts = P06aPublicFixtureIdentity.fixtureKeysAndCerts(networkId, nodeId);
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

    private static Map<String, String> parse(final String[] args) {
        final Map<String, String> options = new HashMap<>();
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
}
