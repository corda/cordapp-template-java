package com.art;

import net.corda.core.contracts.Command;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import java.security.PublicKey;
import java.util.List;

public class ArtContract implements Contract {
    public static final String ID = "com.art.ArtContract";

    @Override
    public void verify(LedgerTransaction tx) {
        Command<ArtCommands> command = tx.getCommand(0);
        ArtCommands commandType = command.getValue();
        List<PublicKey> requiredSigners = command.getSigners();

        ArtState output = tx.outputsOfType(ArtState.class).get(0);

        if (commandType instanceof ArtCommands.Issue) {

            // Appraiser must sign.
            PublicKey appraiserKey = output.getAppraiser().getOwningKey();
            if (!(requiredSigners.contains(appraiserKey)))
                throw new IllegalArgumentException("It must be the appraiser that adds a painting to the blockchain.");

            // No video art.
            if (output.getType().toLowerCase().equals("video art"))
                throw new IllegalArgumentException("Video art isn't really art.");

        } else if (commandType instanceof ArtCommands.Transfer) {
            ArtState input = tx.inputsOfType(ArtState.class).get(0);

            // Owner must sign.
            PublicKey ownerKey = input.getOwner().getOwningKey();
            if (!(requiredSigners.contains(ownerKey)))
                throw new IllegalArgumentException("It must be the owner that transfers a painting on the blockchain.");
        }
    }
}