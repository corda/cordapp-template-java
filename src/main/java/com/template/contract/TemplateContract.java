package com.template.contract;

import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

/**
 * Define your contract here.
 */
public class TemplateContract implements Contract {
    /**
     * The verify() function of the contract of each of the transaction's input and output states must not throw an
     * exception for a transaction to be considered valid.
     */
    @Override
    public void verify(LedgerTransaction tx) {}
}