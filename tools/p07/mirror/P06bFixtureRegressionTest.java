// SPDX-License-Identifier: Apache-2.0
package org.hiero.mirror.importer.parser.record;

import static org.assertj.core.api.Assertions.assertThat;

import com.hedera.hapi.block.stream.protoc.Block;
import com.hedera.services.stream.proto.TransactionSidecarRecord;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hiero.mirror.common.domain.transaction.BlockFile;
import org.hiero.mirror.common.domain.transaction.RecordFile;
import org.hiero.mirror.common.domain.transaction.SidecarFile;
import org.hiero.mirror.common.domain.transaction.TransactionType;
import org.hiero.mirror.importer.ImporterIntegrationTest;
import org.hiero.mirror.importer.domain.StreamFileData;
import org.hiero.mirror.importer.downloader.block.BlockFileTransformer;
import org.hiero.mirror.importer.parser.record.sidecar.SidecarProperties;
import org.hiero.mirror.importer.reader.block.BlockStream;
import org.hiero.mirror.importer.reader.block.BlockStreamReader;
import org.hiero.mirror.importer.reader.record.RecordFileReader;
import org.hiero.mirror.importer.reader.record.sidecar.SidecarFileReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class P06bFixtureRegressionTest extends ImporterIntegrationTest {
    private final RecordFileParser recordFileParser;
    private final RecordFileReader recordFileReader;
    private final SidecarFileReader sidecarFileReader;
    private final SidecarProperties sidecarProperties;
    private final BlockStreamReader blockStreamReader;
    private final BlockFileTransformer blockFileTransformer;

    @BeforeEach
    void setup() {
        recordFileParser.clear();
        sidecarProperties.setEnabled(true);
        sidecarProperties.setPersistBytes(true);
    }

    @Test
    void importsRecordsAndSidecars() throws Exception {
        var root = Path.of(System.getenv("P06B_RECORD_STREAMS"));
        final List<Path> records;
        try (var files = Files.walk(root)) {
            records = files.filter(Files::isRegularFile)
                    .filter(path -> !path.toString().contains("/sidecar/"))
                    .filter(path -> path.getFileName().toString().matches(".*\\.rcd(\\.gz)?$"))
                    .filter(P06bFixtureRegressionTest::hasSignature)
                    .sorted()
                    .toList();
        }
        var sidecars = 0;
        for (var path : records) {
            var recordFile = recordFileReader.read(StreamFileData.from(path.toFile()));
            sidecars += attachSidecars(path, recordFile);
            recordFileParser.parse(recordFile);
        }
        var persistedRecords = count("record_file");
        var transactions = count("transaction");
        var contractResults = count("contract_result");
        var contractLogs = count("contract_log");
        var contractActions = count("contract_action");
        var requiredTypes = System.getenv().getOrDefault("P07_REQUIRED_TRANSACTION_TYPES", "");
        if (!requiredTypes.isBlank()) {
            for (var name : requiredTypes.split(",")) {
                var type = TransactionType.valueOf(name);
                var count = jdbcOperations.queryForObject(
                        "select count(*) from transaction where type = ?", Long.class, type.getProtoId());
                System.out.printf("P07_FEATURE=%s COUNT=%d%n", name, count);
                assertThat(count).as("required native transaction type %s", name).isPositive();
            }
            assertPositive("ALIASES", "select count(*) from entity where alias is not null");
            assertPositive("TOKEN_TRANSFERS", "select count(*) from token_transfer");
            assertPositive("CUSTOM_FEES", "select count(*) from custom_fee");
            assertPositive("STAKING_REWARDS", "select count(*) from staking_reward_transfer");
            assertPositive("CHARGED_FEES", "select count(*) from transaction where charged_tx_fee > 0");
        }
        System.out.printf(
                "P07_RECORDS=%d TRANSACTIONS=%d RESULTS=%d LOGS=%d ACTIONS=%d SIDECARS=%d%n",
                persistedRecords, transactions, contractResults, contractLogs, contractActions, sidecars);
        assertThat(persistedRecords).isEqualTo(records.size());
        assertThat(transactions).isPositive();
        var expectedActions = Long.parseLong(System.getenv().getOrDefault("P07_EXPECTED_ACTIONS", "3"));
        assertThat(contractActions).isEqualTo(expectedActions);
        if (expectedActions > 0) {
            assertThat(contractResults).isPositive();
            assertThat(sidecars).isPositive();
        }
    }

    @Test
    void importsBlocks() throws Exception {
        var root = Path.of(System.getenv("P06B_BLOCK_STREAMS"));
        final List<Path> blocks;
        try (var files = Files.walk(root)) {
            blocks = files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".blk.gz"))
                    .filter(path -> path.toFile().length() > 0)
                    .filter(path -> Files.isRegularFile(path.resolveSibling(
                            path.getFileName().toString().replace(".blk.gz", ".mf"))))
                    .sorted()
                    .toList();
        }
        for (var path : blocks) {
            var data = StreamFileData.from(path.toFile());
            final Block block;
            try (InputStream input = data.getInputStream()) {
                block = Block.parseFrom(input);
            }
            var bytes = Files.readAllBytes(path);
            var now = System.currentTimeMillis();
            BlockFile blockFile = blockStreamReader.read(
                    new BlockStream(block.getItemsList(), now, bytes, path.getFileName().toString(), now, bytes.length));
            recordFileParser.parse(blockFileTransformer.transform(blockFile));
        }
        var persisted = count("record_file");
        System.out.printf("P07_BLOCKS=%d PERSISTED=%d%n", blocks.size(), persisted);
        assertThat(persisted).isEqualTo(blocks.size());
    }

    private int attachSidecars(Path recordPath, RecordFile recordFile) {
        var recordsByTimestamp = new LinkedHashMap<Object, List<TransactionSidecarRecord>>();
        var count = 0;
        for (SidecarFile sidecar : recordFile.getSidecars()) {
            var sidecarPath = recordPath.getParent().resolve("sidecar").resolve(sidecar.getName());
            sidecarFileReader.read(sidecar, StreamFileData.from(sidecarPath.toFile()));
            assertThat(sidecar.getActualHash()).isEqualTo(sidecar.getHash());
            count += sidecar.getRecords().size();
            for (var record : sidecar.getRecords()) {
                recordsByTimestamp
                        .computeIfAbsent(record.getConsensusTimestamp(), ignored -> new ArrayList<>())
                        .add(record);
            }
        }
        recordFile.getItems().forEach(item -> item.setSidecarRecords(recordsByTimestamp.getOrDefault(
                item.getTransactionRecord().getConsensusTimestamp(), List.of())));
        return count;
    }

    private long count(String table) {
        return jdbcOperations.queryForObject("select count(*) from " + table, Long.class);
    }

    private void assertPositive(String feature, String query) {
        var count = jdbcOperations.queryForObject(query, Long.class);
        System.out.printf("P07_FEATURE=%s COUNT=%d%n", feature, count);
        assertThat(count).as("required native feature %s", feature).isPositive();
    }

    private static boolean hasSignature(Path path) {
        var basename = path.getFileName().toString().replaceFirst("\\.rcd(?:\\.gz)?$", "");
        return Files.isRegularFile(path.resolveSibling(basename + ".rcd_sig"));
    }
}
