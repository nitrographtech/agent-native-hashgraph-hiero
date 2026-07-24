// SPDX-License-Identifier: Apache-2.0
module com.hedera.node.fixture.tooling {
    uses com.hedera.node.app.service.contract.impl.exec.ActionSidecarContentTracerFactory;

    requires com.hedera.node.app.service.contract.impl;
    requires com.hedera.node.app.service.entityid;
    requires com.hedera.node.hapi;
    requires com.hedera.node.test.clients;
    requires com.hedera.pbj.runtime;
    requires com.swirlds.platform.core;
    requires com.github.spotbugs.annotations;
    requires org.hiero.consensus.model;
    requires org.hyperledger.besu.datatypes;
    requires org.hyperledger.besu.evm;
    requires java.base;
    requires org.apache.logging.log4j;
    requires org.bouncycastle.provider;
    requires org.junit.jupiter.api;
    requires tuweni.bytes;

    exports com.hedera.services.bdd.fixturetooling.tracing to
            com.hedera.node.app.service.contract.impl;

    opens com.hedera.services.bdd.fixturetooling.tracing to
            org.junit.platform.commons;

    provides com.hedera.node.app.service.contract.impl.exec.ActionSidecarContentTracerFactory with
            com.hedera.services.bdd.fixturetooling.tracing.FixtureActionTracerFactory;
}
