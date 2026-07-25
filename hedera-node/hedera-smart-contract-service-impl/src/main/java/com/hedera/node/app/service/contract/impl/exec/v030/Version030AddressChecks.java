// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.service.contract.impl.exec.v030;

import com.hedera.node.app.service.contract.impl.exec.AddressChecks;
import edu.umd.cs.findbugs.annotations.NonNull;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.evm.frame.MessageFrame;

/**
 * The initial implementation of {@link AddressChecks} from v0.30; did not have a concept of system accounts.
 */
@Singleton
public class Version030AddressChecks implements AddressChecks {
    @Inject
    public Version030AddressChecks() {}

    @Override
    public boolean isPresent(@NonNull final Address address, @NonNull final MessageFrame frame) {
        return isHederaPrecompile(address) || frame.getWorldUpdater().get(address) != null;
    }

    @Override
    public boolean isSystemAccount(@NonNull final Address address) {
        return false;
    }

    @Override
    public boolean isNonUserAccount(@NonNull final Address address) {
        return false;
    }

    @Override
    public boolean isHederaPrecompile(@NonNull final Address address) {
        return false;
    }
}
