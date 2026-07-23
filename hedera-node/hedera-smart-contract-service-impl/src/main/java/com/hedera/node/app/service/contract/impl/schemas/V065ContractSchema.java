// SPDX-License-Identifier: Apache-2.0
package com.hedera.node.app.service.contract.impl.schemas;

/**
 * The state definitions for <a href="https://hips.hedera.com/hip/hip-1195">HIP-1195, "Hiero hooks and an application to allowances"</a>.
 * These include,
 * <ul>
 *     <li>A key/value state for EVM hook metadata.</li>
 *     <li>A key/value state for EVM hook storage slots.</li>
 * </ul>
 */
@Deprecated(forRemoval = false)
public class V065ContractSchema extends com.hedera.node.app.service.contract.history.V065ContractSchema {}
