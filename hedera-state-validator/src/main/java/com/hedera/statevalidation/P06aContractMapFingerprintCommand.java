// SPDX-License-Identifier: Apache-2.0
package com.hedera.statevalidation;

import static com.hedera.pbj.runtime.ProtoParserTools.TAG_FIELD_OFFSET;
import static com.hedera.statevalidation.util.ConfigUtils.getVirtualMapValueParseMaxSizeBytes;

import com.hedera.hapi.platform.state.StateKey;
import com.hedera.hapi.platform.state.StateValue;
import com.hedera.pbj.runtime.Codec;
import com.hedera.pbj.runtime.io.buffer.Bytes;
import com.hedera.statevalidation.util.StateUtils;
import com.swirlds.state.lifecycle.StateMetadata;
import com.swirlds.state.merkle.VirtualMapStateImpl;
import com.swirlds.virtualmap.datasource.VirtualLeafBytes;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import org.hiero.base.crypto.Hash;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

/**
 * Produces content-addressed fingerprints of the four retained historical contract maps.
 *
 * <p>This is an offline verification command. It hashes the exact PBJ key and value wrapper bytes
 * stored in the signed state's virtual map; it does not invoke contract handlers.
 */
@Command(name = "p06a-contract-map-fingerprint", description = "Fingerprint retained P06A historical contract maps.")
public final class P06aContractMapFingerprintCommand implements Runnable {
    private static final String SERVICE = "ContractService";
    private static final List<MapSpec> MAPS = List.of(
            new MapSpec("STORAGE", "0.49.0"),
            new MapSpec("BYTECODE", "0.49.0"),
            new MapSpec("EVM_HOOK_STATES", "0.65.0"),
            new MapSpec("LAMBDA_STORAGE", "0.65.0"));

    @ParentCommand
    private StateOperatorCommand parent;

    @Option(
            names = {"-o", "--out"},
            required = true,
            description = "Output JSON file.")
    private Path output;

    @Override
    public void run() {
        try {
            parent.initializeStateDir();
            final VirtualMapStateImpl state = (VirtualMapStateImpl) StateUtils.getDefaultState();
            final Map<Integer, StateMetadata<?, ?>> metadata =
                    state.getServices().get(SERVICE);
            if (metadata == null) {
                throw new IllegalStateException("ContractService metadata is not registered");
            }
            final List<MapFingerprint> fingerprints = new ArrayList<>();
            for (final MapSpec spec : MAPS) {
                final int stateId = StateUtils.stateIdFor(
                        SERVICE, spec.stateKey().equals("LAMBDA_STORAGE") ? "EVM_HOOK_STORAGE" : spec.stateKey());
                final StateMetadata<?, ?> stateMetadata = metadata.get(stateId);
                if (stateMetadata == null) {
                    throw new IllegalStateException("Historical state is not registered: " + spec.stateKey());
                }
                fingerprints.add(fingerprint(state, spec, stateId, stateMetadata));
            }
            Files.writeString(output, toJson(StateUtils.getOriginalStateHash(), fingerprints));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static MapFingerprint fingerprint(
            @NonNull final VirtualMapStateImpl state,
            @NonNull final MapSpec spec,
            final int stateId,
            @NonNull final StateMetadata<?, ?> metadata)
            throws Exception {
        final List<RawEntry> entries = new ArrayList<>();
        final var records = state.getRoot().getRecords();
        final var virtualMetadata = state.getRoot().getMetadata();
        for (long path = virtualMetadata.getFirstLeafPath(); path <= virtualMetadata.getLastLeafPath(); path++) {
            final VirtualLeafBytes leaf = records.findLeafRecord(path);
            if (leaf == null) {
                continue;
            }
            final var keyData = leaf.keyBytes().toReadableSequentialData();
            final int actualStateId = keyData.readVarInt(false) >> TAG_FIELD_OFFSET;
            if (actualStateId == stateId) {
                entries.add(new RawEntry(
                        leaf.keyBytes().toByteArray(), leaf.valueBytes().toByteArray()));
            }
        }
        entries.sort(Comparator.comparing(RawEntry::key, Arrays::compareUnsigned));

        final MessageDigest keyDigest = MessageDigest.getInstance("SHA-256");
        final MessageDigest valueDigest = MessageDigest.getInstance("SHA-256");
        final MessageDigest aggregateDigest = MessageDigest.getInstance("SHA-256");
        for (final RawEntry entry : entries) {
            updateLengthPrefixed(keyDigest, entry.key());
            updateLengthPrefixed(valueDigest, entry.value());
            updateLengthPrefixed(aggregateDigest, entry.key());
            updateLengthPrefixed(aggregateDigest, entry.value());
        }
        final var definition = metadata.stateDefinition();
        return new MapFingerprint(
                spec,
                stateId,
                entries.size(),
                hex(keyDigest.digest()),
                hex(valueDigest.digest()),
                hex(aggregateDigest.digest()),
                definition.keyCodec().getClass().getName(),
                definition.valueCodec().getClass().getName(),
                entries.isEmpty() ? null : representative(entries.getFirst()));
    }

    private static String representative(@NonNull final RawEntry entry) throws Exception {
        final StateKey key = StateKey.PROTOBUF.parse(Bytes.wrap(entry.key()));
        final StateValue value = StateValue.PROTOBUF.parse(
                Bytes.wrap(entry.value()).toReadableSequentialData(),
                false,
                false,
                Codec.DEFAULT_MAX_DEPTH,
                getVirtualMapValueParseMaxSizeBytes());
        return "{\"key\":"
                + quote(StateUtils.keyToJson(key.key()))
                + ",\"value\":"
                + quote(StateUtils.valueToJson(value.value()))
                + "}";
    }

    private static void updateLengthPrefixed(@NonNull final MessageDigest digest, @NonNull final byte[] bytes)
            throws Exception {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(Integer.BYTES + bytes.length);
        try (final DataOutputStream data = new DataOutputStream(buffer)) {
            data.writeInt(bytes.length);
            data.write(bytes);
        }
        digest.update(buffer.toByteArray());
    }

    private static String toJson(@NonNull final Hash root, @NonNull final List<MapFingerprint> maps) {
        final StringBuilder json = new StringBuilder();
        json.append("{\n  \"algorithm\":\"SHA-256 over lexicographically sorted, length-prefixed PBJ wrapper bytes\",")
                .append("\n  \"state_root\":")
                .append(quote(root.toString()))
                .append(",\n  \"maps\":[\n");
        for (int i = 0; i < maps.size(); i++) {
            final MapFingerprint map = maps.get(i);
            json.append("    {\"service\":\"ContractService\",\"state_key\":")
                    .append(quote(map.spec().stateKey()))
                    .append(",\"schema_version\":")
                    .append(quote(map.spec().schemaVersion()))
                    .append(",\"present\":true,\"state_id\":")
                    .append(map.stateId())
                    .append(",\"cardinality\":")
                    .append(map.cardinality())
                    .append(",\"key_inventory_sha256\":")
                    .append(quote(map.keyHash()))
                    .append(",\"value_inventory_sha256\":")
                    .append(quote(map.valueHash()))
                    .append(",\"aggregate_content_sha256\":")
                    .append(quote(map.aggregateHash()))
                    .append(",\"key_codec\":")
                    .append(quote(map.keyCodec()))
                    .append(",\"value_codec\":")
                    .append(quote(map.valueCodec()))
                    .append(",\"representative\":")
                    .append(map.representative() == null ? "null" : map.representative())
                    .append("}");
            if (i + 1 < maps.size()) {
                json.append(',');
            }
            json.append('\n');
        }
        return json.append("  ]\n}\n").toString();
    }

    private static String quote(final String value) {
        if (value == null) {
            return "null";
        }
        final String bounded = value.length() <= 512
                ? value
                : value.substring(0, 512) + "...[decoded value truncated; full bytes are included in hashes]";
        return "\""
                + bounded.replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                + "\"";
    }

    private static String hex(final byte[] value) {
        return HexFormat.of().formatHex(value);
    }

    private record MapSpec(String stateKey, String schemaVersion) {}

    private record RawEntry(byte[] key, byte[] value) {}

    private record MapFingerprint(
            MapSpec spec,
            int stateId,
            int cardinality,
            String keyHash,
            String valueHash,
            String aggregateHash,
            String keyCodec,
            String valueCodec,
            String representative) {}
}
