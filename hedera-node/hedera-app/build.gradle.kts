// SPDX-License-Identifier: Apache-2.0
plugins {
    id("org.hiero.gradle.module.library")
    id("org.hiero.gradle.feature.benchmark")
    id("org.hiero.gradle.feature.test-fixtures")
}

description = "Hedera Application - Implementation"

mainModuleInfo {
    annotationProcessor("dagger.compiler")

    // This is needed to pick up and include the native libraries for the netty epoll transport
    runtimeOnly("io.netty.transport.epoll.linux.x86_64")
    runtimeOnly("io.netty.transport.epoll.linux.aarch_64")
    runtimeOnly("io.helidon.grpc.core")
    runtimeOnly("io.helidon.webclient")
    runtimeOnly("io.helidon.webclient.grpc")
    runtimeOnly("io.helidon.webclient.http2")
    runtimeOnly("com.hedera.pbj.grpc.client.helidon")
    runtimeOnly("com.hedera.pbj.grpc.helidon")
    runtimeOnly("org.hiero.consensus.pcli")
}

testModuleInfo {
    requires("com.fasterxml.jackson.databind")
    requires("com.google.protobuf")
    requires("com.google.common.jimfs")
    requires("com.hedera.node.app")
    requires("com.hedera.node.app.test.fixtures")
    requires("com.hedera.node.app.spi.test.fixtures")
    requires("com.hedera.node.config.test.fixtures")
    requires("com.swirlds.merkledb")
    requires("com.swirlds.config.extensions.test.fixtures")
    requires("com.swirlds.platform.core.test.fixtures")
    requires("com.swirlds.state.api.test.fixtures")
    requires("com.swirlds.state.impl.test.fixtures")
    requires("com.swirlds.base.test.fixtures")
    requires("org.hiero.consensus.roster.test.fixtures")
    requires("org.hiero.base.crypto.test.fixtures")
    requires("com.esaulpaugh.headlong")
    requires("org.assertj.core")
    requires("org.bouncycastle.provider")
    requires("org.junit.jupiter.api")
    requires("org.junit.jupiter.params")
    requires("org.mockito")
    requires("org.mockito.junit.jupiter")
    requires("tuweni.bytes")
    requires("uk.org.webcompere.systemstubs.core")
    requires("uk.org.webcompere.systemstubs.jupiter")

    exportsTo("org.hiero.base.utility") // access package "utils" (maybe rename to "util")
    opensTo("com.hedera.node.app.spi.test.fixtures") // log captor injection
    opensTo("com.swirlds.common") // instantiation via reflection
}

jmhModuleInfo {
    requires("com.hedera.node.app")
    requires("com.hedera.node.app.hapi.utils")
    requires("com.hedera.node.app.spi.test.fixtures")
    requires("com.hedera.node.app.test.fixtures")
    requires("com.hedera.node.config")
    requires("com.hedera.node.hapi")
    requires("com.hedera.pbj.runtime")
    requires("com.swirlds.config.api")
    requires("com.swirlds.config.extensions")
    requires("com.swirlds.metrics.api")
    requires("com.swirlds.platform.core")
    requires("com.swirlds.state.api")
    requires("com.hedera.pbj.grpc.helidon")
    requires("com.hedera.pbj.grpc.helidon.config")
    requires("io.helidon.common")
    requires("io.helidon.webserver")
    requires("org.hiero.consensus.model")
    requires("org.hiero.consensus.platformstate")
    requires("jmh.core")
    requires("org.hiero.base.crypto")
}

// Add all the libs dependencies into the jar manifest!
tasks.jar {
    inputs.files(configurations.runtimeClasspath)
    manifest {
        attributes(
            "Main-Class" to "com.hedera.node.app.ServicesMain",
            // Declares JNI usage (netty's NativeLibraryUtil) so the JDK does not print a
            // restricted-method warning for callers in the unnamed module of this JAR
            // when launched via `java -jar`.
            "Enable-Native-Access" to "ALL-UNNAMED",
        )
    }
    doFirst {
        manifest.attributes(
            "Class-Path" to
                inputs.files
                    .filter { it.extension == "jar" }
                    .map { "../../data/lib/" + it.name }
                    .sorted()
                    .joinToString(separator = " ")
        )
    }
}

// Produce a distinct Nitrograph-native application artifact. This is an application ownership
// boundary only; P06B-7 will remove executable dependency jars after the compatibility API split.
val nativeAppJar =
    tasks.register<Jar>("nativeAppJar") {
        group = "build"
        description =
            "Build the Nitrograph native application jar without full or standalone entry points."
        archiveClassifier.set("native")
        from(sourceSets.main.get().output)
        // The main module descriptor provides both runtime profiles. Until P06B-7 creates a
        // dedicated native module descriptor, the profile-specific artifact runs on the
        // distribution classpath and advertises only its filtered META-INF/services entries.
        exclude("module-info.class")
        exclude("com/hedera/node/app/services/FullContractRuntimeProvider*.class")
        exclude("com/hedera/node/app/services/FullContractRuntimeProviderFactory.class")
        exclude("com/hedera/node/app/store/FullContractStoreFactory.class")
        exclude("com/hedera/node/app/workflows/standalone/**")
        exclude("com/hedera/node/app/fees/StandaloneFeeCalculatorImpl*.class")
        exclude("com/hedera/node/app/service/contract/impl/**")
        exclude("org/hyperledger/besu/**")
        exclude("org/apache/tuweni/**")
        filesMatching(
            "META-INF/services/com.hedera.node.app.services.ContractRuntimeProviderFactory"
        ) {
            filter { line ->
                if (line.endsWith(".FullContractRuntimeProviderFactory")) "" else line
            }
        }
        filesMatching("META-INF/services/com.hedera.node.app.store.ContractStoreFactory") {
            filter { line -> if (line.endsWith(".FullContractStoreFactory")) "" else line }
        }
        manifest {
            attributes(
                "Main-Class" to "com.hedera.node.app.ServicesMain",
                "Enable-Native-Access" to "ALL-UNNAMED",
            )
        }
        inputs.files(configurations.runtimeClasspath)
        doFirst {
            manifest.attributes(
                "Class-Path" to
                    inputs.files
                        .filter {
                            it.extension == "jar" &&
                                !it.name.startsWith("app-service-contract-impl-") &&
                                !it.name.startsWith("besu-") &&
                                !it.name.startsWith("evm-") &&
                                !it.name.startsWith("tuweni-") &&
                                !it.name.startsWith("algorithms-") &&
                                !it.name.startsWith("arithmetic-") &&
                                !it.name.startsWith("blake2bf-") &&
                                !it.name.startsWith("gnark-") &&
                                !it.name.startsWith("jc-kzg-") &&
                                !it.name.startsWith("rlp-") &&
                                !it.name.startsWith("secp256k1-") &&
                                !it.name.startsWith("secp256r1-")
                        }
                        .map { "../../data/lib/" + it.name }
                        .sorted()
                        .joinToString(separator = " ")
            )
        }
    }

// The platform base-crypto artifact uses a Besu-native secp256k1 verifier. Native account
// signatures still require secp256k1 verification, so the Nitrograph distribution replaces that
// one implementation with an API-compatible Bouncy Castle implementation. Platform source and the
// full distribution remain unchanged.
val compileNativeCrypto by
    tasks.registering(JavaCompile::class) {
        source(layout.projectDirectory.dir("src/nativeCrypto/java"))
        classpath = configurations.runtimeClasspath.get()
        destinationDirectory.set(layout.buildDirectory.dir("classes/java/nativeCrypto"))
        options.release.set(25)
    }

val nativeBaseCryptoJar =
    tasks.register<Jar>("nativeBaseCryptoJar") {
        group = "build"
        description = "Build base-crypto for the native distribution without Besu-native secp256k1."
        dependsOn(compileNativeCrypto)
        archiveFileName.set(
            provider {
                configurations.runtimeClasspath
                    .get()
                    .single { it.name.startsWith("base-crypto-") }
                    .name
            }
        )
        from(
            provider {
                zipTree(
                    configurations.runtimeClasspath.get().single {
                        it.name.startsWith("base-crypto-")
                    }
                )
            }
        ) {
            exclude("module-info.class")
            exclude("org/hiero/base/crypto/engine/EcdsaSecp256k1Verifier*.class")
        }
        from(compileNativeCrypto)
    }

// Copy dependencies into `data/lib`
val copyLib =
    tasks.register<Sync>("copyLib") {
        from(project.configurations.getByName("runtimeClasspath"))
        into(layout.projectDirectory.dir("../data/lib"))
    }

// Copy built jar into `data/apps` and rename HederaNode.jar
val copyApp =
    tasks.register<Sync>("copyApp") {
        from(tasks.jar)
        into(layout.projectDirectory.dir("../data/apps"))
        rename { "HederaNode.jar" }
        shouldRunAfter(tasks.named("copyLib"))
    }

// Working directory for 'run' tasks
val nodeWorkingDir = layout.buildDirectory.dir("node")

val copyNodeData =
    tasks.register<Sync>("copyNodeDataAndConfig") {
        into(nodeWorkingDir)

        // Copy things from hedera-node/data
        into("data/lib") { from(copyLib) }
        into("data/apps") { from(copyApp) }
        into("data/onboard") { from(layout.projectDirectory.dir("../data/onboard")) }
        into("data/keys") { from(layout.projectDirectory.dir("../data/keys")) }

        // Copy hedera-node/configuration/dev as hedera-node/hedera-app/build/node/data/config  }
        from(layout.projectDirectory.dir("../configuration/dev")) { into("data/config") }
        from(layout.projectDirectory.file("../config.txt"))
        from(layout.projectDirectory.file("../log4j2.xml"))
        from(layout.projectDirectory.file("../configuration/dev/settings.txt"))
    }

tasks.assemble {
    dependsOn(copyLib)
    dependsOn(copyApp)
    dependsOn(copyNodeData)
}

// Generate the signing PEM file expected by EnhancedKeyStoreLoader for the local node.
// KeysAndCertsGenerator is deterministic per nodeId, so the resulting cert matches the
// gossipCaCertificate baked into hedera-node/configuration/dev/genesis-network.json.
val generateNodeKeys =
    tasks.register<JavaExec>("generateNodeKeys") {
        description = "Generate the local node's signing key PEM file used by the run task."
        dependsOn(copyNodeData)
        workingDir = nodeWorkingDir.get().asFile
        jvmArgs = listOf("-cp", "data/lib/*:data/apps/*")
        mainClass.set("org.hiero.consensus.pcli.Pcli")
        args = listOf("generate-keys", "0", "--path", "data/keys")
        outputs.file(nodeWorkingDir.get().file("data/keys/s-private-node1.pem"))
    }

// Create the "run" task for running a Hedera consensus node
tasks.register<JavaExec>("run") {
    group = "application"
    description = "Run a Hedera consensus node instance."
    dependsOn(tasks.assemble)
    dependsOn(generateNodeKeys)
    workingDir = nodeWorkingDir.get().asFile
    jvmArgs = listOf("-cp", "data/lib/*:data/apps/*")
    mainClass.set("com.hedera.node.app.ServicesMain")

    // Add arguments for the application to run a local node
    args = listOf("-local", "0")
}

val distributionFull =
    tasks.register<Sync>("distributionFull") {
        group = "distribution"
        description =
            "Assemble the behavioral-control distribution with every upstream service enabled."
        dependsOn(copyNodeData)
        from(nodeWorkingDir)
        into(layout.buildDirectory.dir("distributions/distribution-full"))
    }

tasks.register<Sync>("distributionNativeAgent") {
    group = "distribution"
    description = "Assemble the native-agent distribution with the contract service disabled."
    dependsOn(copyNodeData, nativeAppJar, nativeBaseCryptoJar)
    from(nodeWorkingDir) {
        exclude("data/apps/HederaNode.jar")
        exclude("data/lib/app-service-contract-impl-*")
        exclude("data/lib/base-crypto-*")
        exclude("data/lib/besu-*")
        exclude("data/lib/evm-*")
        exclude("data/lib/tuweni-*")
        exclude("data/lib/algorithms-*")
        exclude("data/lib/arithmetic-*")
        exclude("data/lib/blake2bf-*")
        exclude("data/lib/gnark-*")
        exclude("data/lib/jc-kzg-*")
        exclude("data/lib/rlp-*")
        exclude("data/lib/secp256k1-*")
        exclude("data/lib/secp256r1-*")
    }
    from(nativeAppJar) {
        into("data/apps")
        rename { "HederaNode.jar" }
    }
    from(nativeBaseCryptoJar) { into("data/lib") }
    into(layout.buildDirectory.dir("distributions/distribution-native-agent"))
    filesMatching("data/config/application.properties") {
        filter { line -> if (line == "contracts.enabled=true") "contracts.enabled=false" else line }
    }
}

val cleanRun =
    tasks.register<Delete>("cleanRun") {
        val prjDir = layout.projectDirectory.dir("..")
        delete(prjDir.dir("database"))
        delete(prjDir.dir("output"))
        delete(prjDir.dir("settingsUsed.txt"))
        delete(prjDir.dir("swirlds.jar"))
        delete(prjDir.asFileTree.matching { include("MainNetStats*") })
        val dataDir = prjDir.dir("data")
        delete(dataDir.dir("accountBalances"))
        delete(dataDir.dir("apps"))
        delete(dataDir.dir("lib"))
        delete(dataDir.dir("recordstreams"))
        delete(dataDir.dir("saved"))
    }

tasks.clean { dependsOn(cleanRun) }

tasks.register("showHapiVersion") {
    inputs.property("version", project.version)
    doLast { println(inputs.properties["version"]) }
}

var updateDockerEnvTask =
    tasks.register<Exec>("updateDockerEnv") {
        description =
            "Creates the .env file in the docker folder that contains environment variables for docker"
        group = "docker"

        workingDir(layout.projectDirectory.dir("../docker"))
        commandLine("./update-env.sh", project.version)
    }

dependencies { api(project(":config")) }

tasks.register<Exec>("createDockerImage") {
    description = "Creates the docker image of the services based on the current version"
    group = "docker"

    dependsOn(updateDockerEnvTask, tasks.assemble)
    workingDir(layout.projectDirectory.dir("../docker"))
    commandLine("./docker-build.sh", project.version, layout.projectDirectory.dir("..").asFile)
}

tasks.register<Exec>("startDockerContainers") {
    description = "Starts docker containers of the services based on the current version"
    group = "docker"

    dependsOn(updateDockerEnvTask)
    workingDir(layout.projectDirectory.dir("../docker"))
    commandLine("docker-compose", "up")
}

tasks.register<Exec>("stopDockerContainers") {
    description = "Stops running docker containers of the services"
    group = "docker"

    dependsOn(updateDockerEnvTask)
    workingDir(layout.projectDirectory.dir("../docker"))
    commandLine("docker-compose", "stop")
}
