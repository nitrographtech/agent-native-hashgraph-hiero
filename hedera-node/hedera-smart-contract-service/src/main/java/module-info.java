// SPDX-License-Identifier: Apache-2.0
/**
 * Provides the classes necessary to manage Hedera Smart Contract Service.
 */
module com.hedera.node.app.service.contract {
    exports com.hedera.node.app.service.contract;
    exports com.hedera.node.app.service.contract.history;
    exports com.hedera.node.app.service.contract.impl.records;
    exports com.hedera.node.app.service.contract.impl.schemas to
            com.hedera.node.app,
            com.hedera.node.services.cli,
            com.hedera.node.test.clients,
            com.hedera.state.validator;

    uses com.hedera.node.app.service.contract.ContractService;

    requires transitive com.hedera.node.app.service.token;
    requires transitive com.hedera.node.app.spi;
    requires transitive com.hedera.node.hapi;
    requires transitive com.hedera.pbj.runtime;
    requires transitive com.swirlds.state.api;
    requires static transitive com.github.spotbugs.annotations;
}
