// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.suites.utils;

import java.util.List;

/** Account-number vectors used by retained native account and transfer tests. */
public final class NativeAccountTestVectors {
    public static final List<Long> nonExistingSystemAccounts = List.of(351L, 352L, 353L, 354L, 355L, 356L, 357L, 358L);
    public static final List<Long> existingSystemAccounts = List.of(800L, 999L, 1000L);
    public static final List<Long> systemAccounts =
            List.of(0L, 1L, 9L, 10L, 358L, 359L, 360L, 361L, 750L, 751L, 799L, 800L, 999L, 1000L);
    public static final List<Long> callOperationsSuccessSystemAccounts = List.of(0L, 1L, 358L, 750L, 751L, 999L, 1000L);

    private NativeAccountTestVectors() {
        throw new UnsupportedOperationException("Utility class");
    }
}
