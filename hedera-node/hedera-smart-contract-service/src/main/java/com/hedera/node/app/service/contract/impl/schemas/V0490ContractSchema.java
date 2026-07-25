// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.service.contract.impl.schemas;

/**
 * The schema for the {@code v0.49.0} version of the contract service. Since {@code v0.49.7} was
 * the first release of the modularized contract service, this schema defines states to create
 * for both the contract storage and bytecode.
 */
@Deprecated(forRemoval = false)
public class V0490ContractSchema extends com.hedera.node.app.service.contract.history.V0490ContractSchema {}
