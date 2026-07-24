// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.service.contract.impl;

import com.hedera.node.app.service.contract.impl.annotations.CustomOps;
import com.hedera.node.app.service.contract.impl.exec.ActionSidecarContentTracer;
import com.hedera.node.app.service.contract.impl.exec.metrics.ContractMetrics;
import com.hedera.node.app.service.contract.impl.exec.scope.VerificationStrategies;
import com.hedera.node.app.service.contract.impl.handlers.ContractHandlers;
import com.hedera.node.app.service.contract.impl.nativelibverification.NativeLibVerifier;
import com.hedera.node.app.service.entityid.EntityIdFactory;
import com.hedera.node.app.spi.signatures.SignatureVerifier;
import com.hedera.node.config.data.ContractsConfig;
import dagger.BindsInstance;
import dagger.Component;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.time.InstantSource;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import javax.inject.Singleton;
import org.hyperledger.besu.evm.operation.Operation;

/**
 * The contract service component
 */
@Singleton
@Component(modules = ContractServiceModule.class)
public interface ContractServiceComponent {
    /**
     * A factory for creating a {@link ContractServiceComponent}.
     */
    @Component.Factory
    interface Factory {
        /**
         * @param instantSource the source of the current instant
         * @param signatureVerifier the verifier used for signature verification
         * @param verificationStrategies the current verification strategy to use
         * @param addOnTracers all action sidecar content tracer callbacks
         * @param contractMetrics holds all metrics for the smart contract service
         * @param customOps any additional custom operations to use when constructing the EVM
         * @param entityIdFactory a factory for creating entity IDs
         * @return the contract service component
         */
        ContractServiceComponent create(
                @BindsInstance InstantSource instantSource,
                @BindsInstance SignatureVerifier signatureVerifier,
                @BindsInstance VerificationStrategies verificationStrategies,
                @BindsInstance @Nullable Supplier<List<ActionSidecarContentTracer>> addOnTracers,
                @BindsInstance ContractMetrics contractMetrics,
                @BindsInstance @CustomOps Set<Operation> customOps,
                @BindsInstance Supplier<ContractsConfig> contractsConfigSupplier,
                @BindsInstance EntityIdFactory entityIdFactory,
                @BindsInstance NativeLibVerifier nativeLibVerifier);
    }

    /**
     * @return all contract transaction handlers
     */
    ContractHandlers handlers();

    /**
     * @return contract metrics collection, instance
     */
    ContractMetrics contractMetrics();

    /**
     * @return the current instant source
     */
    NativeLibVerifier nativeLibVerifier();
}
