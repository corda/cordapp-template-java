package com.r3.developers.apples.contracts;

import com.r3.developers.apples.states.AppleStamp;
import com.r3.developers.apples.states.BasketOfApples;
import net.corda.v5.base.exceptions.CordaRuntimeException;
import net.corda.v5.ledger.utxo.Command;
import net.corda.v5.ledger.utxo.Contract;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import java.util.List;

public class BasketOfApplesContract implements Contract {

    @Override
    public void verify(UtxoLedgerTransaction transaction) {
        // Extract the command from the transaction
        final Command command = transaction.getCommands().get(0);

        if (command instanceof AppleCommands.PackBasket) {
            //Retrieve the output state of the transaction
            BasketOfApples output = transaction.getOutputStates(BasketOfApples.class).get(0);
            require(
                    transaction.getOutputContractStates().size() == 1,
                    "This transaction should only output one BasketOfApples state"
            );
            require(
                    !output.getDescription().isBlank(),
                    "The output BasketOfApples state should have clear description of Apple product"
            );
            require(
                    output.getWeight() > 0,
                    "The output BasketOfApples state should have non zero weight"
            );
        } else if (command instanceof AppleCommands.Redeem) {
            require(
                    transaction.getInputContractStates().size() == 2,
                    "This transaction should consume two states"
            );

            // Retrieve the inputs to this transaction, which should be exactly one AppleStamp
            // and one BasketOfApples
            List<AppleStamp> stampInputs = transaction.getInputStates(AppleStamp.class);
            List<BasketOfApples> basketInputs = transaction.getInputStates(BasketOfApples.class);

            require(
                    !stampInputs.isEmpty() && !basketInputs.isEmpty(),
                    "This transaction should have exactly one AppleStamp and one BasketOfApples input state"
            );
            require(
                    stampInputs.get(0).getIssuer().equals(basketInputs.get(0).getFarm()),
                    "The issuer of the Apple stamp should be the producing farm of this basket of apple"
            );
            require(
                    basketInputs.get(0).getWeight() > 0,
                    "The basket of apple has to weigh more than 0"
            );
        } else {
            throw new IllegalArgumentException(String.format("Incorrect type of BasketOfApples commands: %s", command.getClass().toString()));
        }
    }

    private void require(boolean asserted, String errorMessage) {
        if (!asserted) {
            throw new CordaRuntimeException(errorMessage);
        }
    }
}