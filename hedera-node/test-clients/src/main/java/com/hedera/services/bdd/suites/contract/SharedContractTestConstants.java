// SPDX-License-Identifier: Apache-2.0
package com.hedera.services.bdd.suites.contract;

/** Names shared by contract and native-service test suites. */
public final class SharedContractTestConstants {
    public static final String APPROVE = "approve";
    public static final String ERC_20_CONTRACT = "ERC20Contract";
    public static final String PAY_RECEIVABLE_CONTRACT = "PayReceivable";
    public static final String RECEIVER = "yahcliReceiver";
    public static final String RECIPIENT = "recipient";
    public static final String TRANSFERRING_CONTRACT = "Transferring";
    public static final String TRANSFER_FROM = "transferFrom";
    public static final String TRANSFER_SIGNATURE = "Transfer(address,address,uint256)";
    public static final String TRANSFER_SIG_NAME = "transferSig";

    private SharedContractTestConstants() {}
}
