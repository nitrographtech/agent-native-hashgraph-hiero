// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.blocks.historical;

import static java.util.Objects.requireNonNull;

import com.hedera.hapi.node.base.ContractID;
import java.util.List;

/**
 * An immutable, implementation-neutral historical EVM log.
 *
 * <p>This value has no execution behavior and is not a persisted or wire-format type.
 */
public record HistoricalLog(
        ContractID contractId,
        HistoricalEthereumAddress address,
        List<HistoricalLogTopic> topics,
        HistoricalLogData data) {
    public HistoricalLog {
        requireNonNull(contractId);
        requireNonNull(address);
        topics = List.copyOf(requireNonNull(topics));
        requireNonNull(data);
    }
}
