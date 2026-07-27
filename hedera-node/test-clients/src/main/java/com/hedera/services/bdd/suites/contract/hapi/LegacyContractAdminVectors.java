// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.suites.contract.hapi;

/**
 * Immutable identifiers shared by native and legacy-rejection tests that need an administrative
 * key name without depending on an executable contract suite.
 */
public final class LegacyContractAdminVectors {
    public static final String ADMIN_KEY = "adminKey";

    private LegacyContractAdminVectors() {
        throw new UnsupportedOperationException("Utility class");
    }
}
