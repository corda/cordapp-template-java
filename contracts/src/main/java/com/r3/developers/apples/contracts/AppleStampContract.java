package com.r3.developers.apples.contracts;

import com.r3.developers.apples.states.AppleStamp;
import net.corda.v5.base.exceptions.CordaRuntimeException;
import net.corda.v5.ledger.utxo.Command;
import net.corda.v5.ledger.utxo.Contract;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;

public class AppleStampContract implements Contract {

    @Override
    public void verify(UtxoLedgerTransaction transaction) {
        // Extract the command from the transaction
        // Verify the transaction according to the intention of the transaction
        final Command command = transaction.getCommands().get(0);
        if (command instanceof AppleCommands.Issue) {
            AppleStamp output = transaction.getOutputStates(AppleStamp.class).get(0);
            require(
                    transaction.getOutputContractStates().size() == 1,
                    "This transaction should only have one AppleStamp state as output"
            );
            require(
                    !output.getStampDesc().isBlank(),
                    "The output AppleStamp state should have clear description of the type of redeemable goods");
        } else if (command instanceof AppleCommands.Redeem) {
            // Transaction verification will happen in BasketOfApplesContract
        } else {
            //Unrecognised Command Type
            throw new IllegalArgumentException(String.format("Incorrect type of AppleStamp commands: %s", command.getClass().toString()));
        }
    }

    private void require(boolean asserted, String errorMessage) {
        if (!asserted) {
            throw new CordaRuntimeException(errorMessage);
        }
    }
}