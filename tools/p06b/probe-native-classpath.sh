#!/usr/bin/env bash
# SPDX-License-Identifier: Apache-2.0
set -euo pipefail

repo_root="${P06B_SOURCE_ROOT:-$(git rev-parse --show-toplevel)}"
java_home="${JAVA_HOME:?JAVA_HOME must identify the validated JDK}"
native_dist="${P06B_NATIVE_DIST:-$repo_root/hedera-node/hedera-app/build/distributions/distribution-native-agent}"
classpath="$native_dist/data/apps/HederaNode.jar:$native_dist/data/lib/*"
workspace="$(mktemp -d)"
trap 'rm -rf -- "$workspace"' EXIT

cat >"$workspace/NativeClasspathProbe.java" <<'JAVA'
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.ServiceLoader;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.hiero.base.crypto.engine.EcdsaSecp256k1Verifier;

public final class NativeClasspathProbe {
    private static final String[] REQUIRED = {
        "com.hedera.node.app.Hedera",
        "com.hedera.node.app.workflows.handle.record.RecordStreamBuilder",
        "com.hedera.node.app.service.contract.impl.records.ContractCallStreamBuilder",
        "com.hedera.node.app.service.contract.impl.records.ContractCreateStreamBuilder",
        "com.hedera.node.app.service.contract.impl.records.ContractDeleteStreamBuilder",
        "com.hedera.node.app.service.contract.impl.records.ContractOperationStreamBuilder",
        "com.hedera.node.app.service.contract.impl.records.ContractUpdateStreamBuilder",
        "com.hedera.node.app.service.contract.impl.records.EthereumTransactionStreamBuilder",
        "com.hedera.node.app.service.contract.ReadableContractStateStore",
        "com.hedera.node.app.service.contract.history.HistoricalReadableContractStateStore",
        "com.hedera.node.app.service.contract.history.HistoricalReadableEvmHookStore",
        "com.hedera.node.app.service.contract.history.HistoricalContractKeyUtils",
        "com.hedera.node.app.services.HistoricalContractRuntimeProvider",
        "com.hedera.node.app.services.HistoricalContractRuntimeProviderFactory",
        "com.hedera.node.app.store.HistoricalContractStoreFactory",
        "org.hiero.base.crypto.engine.EcdsaSecp256k1Verifier"
    };

    private static final String[] PROHIBITED = {
        "com.hedera.node.app.service.contract.impl.ContractServiceImpl",
        "com.hedera.node.app.services.FullContractRuntimeProvider",
        "com.hedera.node.app.services.FullContractRuntimeProviderFactory",
        "com.hedera.node.app.store.FullContractStoreFactory",
        "com.hedera.node.app.workflows.standalone.TransactionExecutors",
        "org.hyperledger.besu.evm.EVM",
        "org.hyperledger.besu.datatypes.Address",
        "org.apache.tuweni.bytes.Bytes",
        "org.hyperledger.besu.nativelib.secp256k1.LibSecp256k1",
        "com.hedera.node.app.service.contract.impl.exec.systemcontracts.HederaSystemContract",
        "com.hedera.node.app.service.contract.impl.state.ProxyWorldUpdater"
    };

    public static void main(String[] args) throws Exception {
        for (String name : REQUIRED) {
            Class.forName(name);
        }
        for (String name : PROHIBITED) {
            try {
                Class.forName(name);
                throw new AssertionError("Prohibited class is available: " + name);
            } catch (ClassNotFoundException expected) {
                // Expected.
            }
        }
        assertOnlyHistoricalFactory(
                "com.hedera.node.app.services.ContractRuntimeProviderFactory",
                "com.hedera.node.app.services.HistoricalContractRuntimeProviderFactory");
        assertOnlyHistoricalFactory(
                "com.hedera.node.app.store.ContractStoreFactory",
                "com.hedera.node.app.store.HistoricalContractStoreFactory");
        verifyNeutralSecp256k1();
        System.out.println("P06B exact native classpath probe: PASS");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void assertOnlyHistoricalFactory(String serviceName, String expectedName)
            throws Exception {
        Class service = Class.forName(serviceName);
        List<String> providers = new ArrayList<>();
        for (Object provider : ServiceLoader.load(service)) {
            providers.add(provider.getClass().getName());
        }
        if (!providers.equals(List.of(expectedName))) {
            throw new AssertionError(serviceName + " providers: " + providers);
        }
    }

    private static void verifyNeutralSecp256k1() {
        var curve = SECNamedCurves.getByName("secp256k1");
        var privateKey = new BigInteger(
                "1c3a1d9f6304298965c9d5b928770f3e830d7b6f7e5bc6f1e7c96a890bcb8734", 16);
        byte[] digest = HexFormat.of().parseHex(
                "a0f3b5d8719c435b4489d1e9f7e403f1792a2df9c62b464730f186b0565e6712");
        var signer = new ECDSASigner();
        signer.init(true, new ECPrivateKeyParameters(privateKey,
                new org.bouncycastle.crypto.params.ECDomainParameters(
                        curve.getCurve(), curve.getG(), curve.getN(), curve.getH())));
        BigInteger[] signature = signer.generateSignature(digest);
        byte[] rawSignature = new byte[64];
        copyUnsigned(signature[0], rawSignature, 0);
        copyUnsigned(signature[1], rawSignature, 32);
        byte[] encodedPublicKey = curve.getG().multiply(privateKey).normalize().getEncoded(false);
        byte[] rawPublicKey = java.util.Arrays.copyOfRange(encodedPublicKey, 1, encodedPublicKey.length);
        if (!new EcdsaSecp256k1Verifier().verify(rawSignature, digest, rawPublicKey)) {
            throw new AssertionError("Neutral secp256k1 verification failed");
        }
    }

    private static void copyUnsigned(BigInteger value, byte[] target, int offset) {
        byte[] source = value.toByteArray();
        int length = Math.min(32, source.length);
        System.arraycopy(source, source.length - length, target, offset + 32 - length, length);
    }
}
JAVA

"$java_home/bin/javac" -cp "$classpath" -d "$workspace" "$workspace/NativeClasspathProbe.java"
"$java_home/bin/java" -cp "$workspace:$classpath" NativeClasspathProbe
