// SPDX-License-Identifier: Apache-2.0
/**
 * Temporary test-client compatibility residue after executable contract-engine retirement.
 *
 * <p>P07-9 will neutralize these remaining Besu/Tuweni conversions and collapse this module.
 */
module com.hedera.node.app.service.contract.impl {
    requires transitive com.hedera.node.app.service.contract;
    requires transitive com.hedera.node.hapi;
    requires transitive com.hedera.pbj.runtime;
    requires transitive com.esaulpaugh.headlong;
    requires transitive org.hyperledger.besu.datatypes;
    requires transitive org.hyperledger.besu.evm;
    requires com.hedera.node.app.service.token;
    requires tuweni.bytes;
    requires static transitive com.github.spotbugs.annotations;

    exports com.hedera.node.app.service.contract.impl.schemas to
            com.hedera.node.app,
            com.hedera.node.services.cli,
            com.hedera.node.test.clients,
            com.hedera.state.validator;
    exports com.hedera.node.app.service.contract.impl.utils;
}
