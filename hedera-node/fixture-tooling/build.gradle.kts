// SPDX-License-Identifier: Apache-2.0
plugins { id("org.hiero.gradle.module.application") }

description = "Authenticated historical fixture identity and release verification tooling"

tasks.test { include("**/*Test.class") }

tasks.register<JavaExec>("generateP06aFixtureIdentity") {
    group = "fixture tooling"
    description = "Generate the explicitly enabled, compromised P06A fixture identity"
    classpath = configurations.runtimeClasspath.get().plus(files(tasks.jar))
    mainClass.set("com.hedera.services.bdd.fixturetooling.p06a.P06aFixtureIdentityGenerator")
    args(
        "--enable-public-p06a-fixture-identity",
        "--acknowledge-compromised-test-key",
        "--network-id",
        providers.gradleProperty("p06aFixtureNetworkId").getOrElse(""),
        "--node-id",
        providers.gradleProperty("p06aFixtureNodeId").getOrElse(""),
        "--output-directory",
        providers.gradleProperty("p06aFixtureIdentityOutput").getOrElse(""),
    )
}
