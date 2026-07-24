// SPDX-License-Identifier: Apache-2.0
plugins { id("org.hiero.gradle.module.application") }

description = "Authenticated historical fixture generation tooling"

tasks.test { include("**/*Test.class") }

val fullDistribution = project(":app").tasks.named("distributionFull")
val generateFixtureNodeKeys = project(":app").tasks.named("generateNodeKeys")

fullDistribution.configure { mustRunAfter(generateFixtureNodeKeys) }

val fixtureFullNodeDistribution =
    tasks.register<Sync>("fixtureFullNodeDistribution") {
        group = "fixture tooling"
        description = "Assemble a transient full-node distribution with fixture-only action tracing"
        dependsOn(fullDistribution, generateFixtureNodeKeys, tasks.jar)
        from(project(":app").layout.buildDirectory.dir("distributions/distribution-full"))
        from(tasks.jar) { into("data/lib") }
        into(layout.buildDirectory.dir("distributions/fixture-full-node"))
    }

tasks.register<JavaExec>("runFixtureFullNode") {
    group = "fixture tooling"
    description = "Run the transient full node with fixture-only action tracing enabled"
    dependsOn(fixtureFullNodeDistribution)
    workingDir = layout.buildDirectory.dir("distributions/fixture-full-node").get().asFile
    jvmArgs = listOf("-cp", "data/lib/*:data/apps/*")
    mainClass.set("com.hedera.node.app.ServicesMain")
    args = listOf("-local", "0")
}

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

tasks.register<JavaExec>("populateP06aHistoricalContractState") {
    group = "fixture tooling"
    description = "Populate retained V0.65 contract maps through a pinned full-runtime node"
    classpath = configurations.runtimeClasspath.get().plus(files(tasks.jar))
    workingDir = project(":test-clients").projectDir
    mainClass.set(
        "com.hedera.services.bdd.fixturetooling.p06a.HistoricalContractStateFixtureCreation"
    )
}
