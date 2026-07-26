// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.spec.utilops;

import static com.hedera.services.bdd.spec.transactions.contract.HapiParserUtil.stripSelector;
import static com.hedera.services.bdd.suites.contract.Utils.extractBytecodeUnhexed;
import static com.hedera.services.bdd.suites.contract.Utils.getABIFor;
import static com.hedera.services.bdd.suites.contract.Utils.getResourcePath;

import com.esaulpaugh.headlong.abi.Function;
import com.esaulpaugh.headlong.abi.Tuple;
import com.google.protobuf.ByteString;
import com.hedera.node.app.hapi.utils.ByteStringUtils;
import com.hedera.services.bdd.suites.contract.Utils;
import org.apache.commons.lang3.StringUtils;

/** Bytecode vectors used to validate retained historical sidecar output. */
public final class HistoricalSidecarFixtures {
    private HistoricalSidecarFixtures() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ByteString getInitcode(final String binFileName, final Object... constructorArgs) {
        final var initCode = extractBytecodeUnhexed(getResourcePath(binFileName, ".bin"));
        final byte[] params;
        if (constructorArgs.length == 0) {
            params = new byte[] {};
        } else {
            final var abi = getABIFor(Utils.FunctionType.CONSTRUCTOR, StringUtils.EMPTY, binFileName);
            params = Function.fromJson(abi)
                    .encodeCall(Tuple.from(constructorArgs))
                    .array();
        }
        final var byteCode = ByteStringUtils.wrapUnsafely(params.length > 4 ? stripSelector(params) : params);
        return initCode.concat(byteCode);
    }
}
