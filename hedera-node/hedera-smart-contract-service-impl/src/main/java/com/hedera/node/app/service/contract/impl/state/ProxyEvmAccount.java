// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.service.contract.impl.state;

import com.hedera.hapi.node.base.AccountID;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.evm.Code;
import org.hyperledger.besu.evm.code.CodeFactory;

/**
 * A concrete subclass of {@link AbstractProxyEvmAccount} that represents a contract account.
 *
 * Non-contract accounts expose no executable redirect bytecode.
 *
 */
public class ProxyEvmAccount extends AbstractProxyEvmAccount {

    public ProxyEvmAccount(final AccountID accountID, @NonNull final DispatchingEvmFrameState state) {
        super(accountID, state);
    }

    @Override
    public @NonNull Code getEvmCode(@NonNull final Bytes functionSelector, @NonNull final CodeFactory codeFactory) {
        return codeFactory.createCode(Bytes.EMPTY, false);
    }

    @Override
    public @NonNull Bytes getCode() {
        return Bytes.EMPTY;
    }

    @Override
    public com.hedera.pbj.runtime.io.buffer.Bytes getCodePBJ() {
        return com.hedera.pbj.runtime.io.buffer.Bytes.EMPTY;
    }

    @Override
    public @NonNull Hash getCodeHash() {
        return Hash.EMPTY;
    }
}
