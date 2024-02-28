package com.r3.developers.apples.contracts;

import com.r3.corda.ledger.utxo.testing.ContractTest;
import com.r3.developers.apples.states.AppleStamp;
import com.r3.developers.apples.states.BasketOfApples;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction;
import java.security.PublicKey;
import java.util.List;
import java.util.UUID;

/**
 * The following is the base implementation of the Contract Tests for the Apples CorDapp-template-tutorial.
 *
 * - The AppleContractTest abstract class implements the ContractTest class.
 * - For full contract test coverage, we generally create a class for every command scenario for every state.
 * - Each of these classes will implement the abstract class to incorporate ContractTest general testing functionality
 *   as well as the functionality specific for this CordApp tutorial example.
 *
 * In this case, we have 3 scenarios (you can refer to contract files for the apples tutorial):
 *      1. AppleStamp state, AppleCommands.Issue command
 *      2. BasketOfApples state, AppleCommand.PackBasket command
 *      3. BasketOfApples state, AppleCommand.Redeem command
 *
 * The variables and methods within this abstract class are written to enable code re-use such that states are set up
 * automatically so that you only need to worry about the logic of your contracts
 **/

public abstract class ApplesContractTest extends ContractTest {

    /**
     * The following are implementations of default values for the AppleStamp state for contract testing.
     * Some values such as the public keys already have default values implemented by the ContractTest class.
     * for more information, navigate to their declaration
     */

    // Default values for AppleStamp state
    protected UUID outputAppleStampStateId = UUID.randomUUID();
    protected String outputAppleStampStateStampDesc = "Can be exchanged for a single basket of apples";
    protected PublicKey outputAppleStampStateIssuer = bobKey;
    protected PublicKey outputAppleStampStateHolder = daveKey;
    protected List<PublicKey> outputAppleStampStateParticipants = List.of(bobKey, daveKey);
    protected AppleStamp outputAppleStampState = new AppleStamp(
            outputAppleStampStateId,
            outputAppleStampStateStampDesc,
            outputAppleStampStateIssuer,
            outputAppleStampStateHolder,
            outputAppleStampStateParticipants
    );

    // Default values for BasketOfApples state
    protected String outputBasketOfApplesStateDescription = "Golden delicious apples, picked on 11th May 2023";
    protected PublicKey outputBasketOfApplesStateFarm = bobKey;
    protected PublicKey outputBasketOfApplesStateOwner = bobKey;
    protected int outputBasketOfApplesStateWeight = 214;
    protected List<PublicKey> outputBasketOfApplesStateParticipants = List.of(bobKey);
    protected BasketOfApples outputBasketOfApplesState = new BasketOfApples(
            outputBasketOfApplesStateDescription,
            outputBasketOfApplesStateFarm,
            outputBasketOfApplesStateOwner,
            outputBasketOfApplesStateWeight,
            outputBasketOfApplesStateParticipants
    );

    // Helper function to create input AppleStamp state when building a transaction for contract testing.
    // The argument for outputState for this and the next helper function is set to the default apple state for happy path scenarios.
    // To capture negative test cases or other edge cases, they are written within individual tests.
    @SuppressWarnings("unchecked")
    protected StateAndRef<AppleStamp> createInputStateAppleStamp(AppleStamp outputState) {
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addOutputState(outputState)
                .addCommand(new AppleCommands.Issue())
                .addSignatories(outputState.getParticipants())
                .toSignedTransaction();
        transaction.toLedgerTransaction();
        return (StateAndRef<AppleStamp>) transaction.getOutputStateAndRefs().get(0);
    }

    // Helper function to create input BasketOfApples state when building a transaction for contract testing.
    @SuppressWarnings("unchecked")
    protected StateAndRef<BasketOfApples> createInputStateBasketOfApples(BasketOfApples outputState) {
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addOutputState(outputState)
                .addCommand(new AppleCommands.PackBasket())
                .addSignatories(outputState.getParticipants())
                .toSignedTransaction();
        transaction.toLedgerTransaction();
        return (StateAndRef<BasketOfApples>) transaction.getOutputStateAndRefs().get(0);
    }
}