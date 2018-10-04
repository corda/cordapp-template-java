package com.art;

import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

public class ArtContract implements Contract {
    public static final String ID = "com.art.ArtContract";

    @Override
    public void verify(LedgerTransaction tx) {
        // TODO: Rules.
    }
}