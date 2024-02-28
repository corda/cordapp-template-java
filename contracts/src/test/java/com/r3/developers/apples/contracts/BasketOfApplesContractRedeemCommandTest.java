package com.r3.developers.apples.contracts;

import com.r3.developers.apples.states.AppleStamp;
import com.r3.developers.apples.states.BasketOfApples;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction;
import org.junit.jupiter.api.Test;
import java.util.List;

/**
 * This class is an implementation of the ApplesContractTest which implements the ContractTest abstract class.
 * The ContractTest class provides functions to easily perform unit tests on contracts.
 * The ApplesContractTest adds additional default values for states as well as helper functions to make utxoLedgerTransactions.
 * This allows us to test our implementation of contracts without having to trigger a workflow.
 **/

// This specific class is involved with testing the scenarios involving the BasketOfApples state and the AppleCommands.Redeem command.
public class BasketOfApplesContractRedeemCommandTest extends ApplesContractTest {

    /**
     * All Tests must start with the @Test annotation. Tests can be run individually by running them with your IDE.
     * Alternatively, tests can be grouped up and tested by running the test from the line defining the class above.
     * If you need help to write tests, think of a happy path scenario and then think of every line of code in the contract
     * where the transaction could fail.
     * It helps to meaningfully name tests so that you know exactly what success case or specific error you are testing for.
     **/

    @Test
    public void happyPath() {
        // The following test builds a transaction that should pass all the contract verification checks.
        // The buildTransaction function helps create a utxoLedgerTransaction that can be referenced for contract tests

        // Notice the use of the helper functions written in ApplesContractTest to help build the transaction.
        // This is because we need to refer to the state reference of an input state, which is not so easy to repeatably
        // make without helper functions.
        StateAndRef inputAppleStampState = createInputStateAppleStamp(outputAppleStampState);
        StateAndRef inputBasketOfApplesStates = createInputStateBasketOfApples(outputBasketOfApplesState);
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addInputStates(List.of(inputAppleStampState.getRef(), inputBasketOfApplesStates.getRef()))
                .addOutputState(outputBasketOfApplesState)
                .addCommand(new AppleCommands.Redeem())
                .addSignatories(outputBasketOfApplesStateParticipants)
                .toSignedTransaction();
        /**
         *  The assertVerifies function is the general way to test if a contract test passes or fails a transaction.
         *  If the transaction is verified, then it means that the contract tests pass.
         **/
        assertVerifies(transaction);
    }

    @Test
    public void inputContractStateSizeNotTwo() {
        // The following test builds a transaction that would fail due to not meeting the expectation that a transaction
        // in this CorDapp for this state should consume exactly two input states.
        StateAndRef inputAppleStampState = createInputStateAppleStamp(outputAppleStampState);
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addInputStates(inputAppleStampState.getRef())
                .addOutputState(outputBasketOfApplesState)
                .addCommand(new AppleCommands.Redeem())
                .addSignatories(outputBasketOfApplesStateParticipants)
                .toSignedTransaction();
        /**
         * The assertFailsWith function is the general way to test for unhappy path test cases contract tests.
         *
         * The transaction defined above will fail because the contract expects two input states. However, we built
         * a transaction with one input states. So we expect the transaction to fail, and only 'pass' our test if we
         * can match the error message we expect.
         *
         * NOTE: the assertFailsWith method tests if the exact string of the error message matches the expected message
         *       to test if the string of the error message contains a substring within the error message, use the
         *       assertFailsWithMessageContaining() function using the same arguments.
         **/
        assertFailsWith(transaction, "This transaction should consume two states");
    }

    @Test
    public void twoAppleStampStateInputs() {
        // The following test builds a transaction that would fail due to not meeting the expectation that a transaction
        // must contain exactly one AppleStamp state and one BasketOfApplesState state.
        StateAndRef inputAppleStampState = createInputStateAppleStamp(outputAppleStampState);
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addInputStates(List.of(inputAppleStampState.getRef(), inputAppleStampState.getRef()))
                .addOutputState(outputBasketOfApplesState)
                .addCommand(new AppleCommands.Redeem())
                .addSignatories(outputBasketOfApplesStateParticipants)
                .toSignedTransaction();
        assertFailsWith(transaction, "This transaction should have exactly one AppleStamp and one BasketOfApples input state");
    }

    @Test
    public void appleStampIssuerDifferentFromBasketFarm() {
        // The following test builds a transaction that would fail due to having the issuer not matching the farm.
        // This is because the issuer of the apple stamp should be the farm that grew the apples.
        AppleStamp appleStampStateDifferentIssuer = new AppleStamp(
                outputAppleStampStateId,
                outputAppleStampStateStampDesc,
                aliceKey,
                outputAppleStampStateHolder,
                List.of(aliceKey, outputAppleStampStateHolder)
        );
        StateAndRef invalidInputAppleStampState = createInputStateAppleStamp(appleStampStateDifferentIssuer);
        StateAndRef inputBasketOfApplesStates = createInputStateBasketOfApples(outputBasketOfApplesState);
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addInputStates(List.of(invalidInputAppleStampState.getRef(), inputBasketOfApplesStates.getRef()))
                .addOutputState(outputBasketOfApplesState)
                .addCommand(new AppleCommands.Redeem())
                .addSignatories(outputBasketOfApplesStateParticipants)
                .toSignedTransaction();
        assertFailsWith(transaction, "The issuer of the Apple stamp should be the producing farm of this basket of apple");
    }

    @Test
    public void basketWeightIsZero() {
        // The following test builds a transaction that would fail due to asking to redeem a basket without a valid weight.
        BasketOfApples inputBasketOfApplesStatesZeroWeight = new BasketOfApples(
                outputBasketOfApplesStateDescription,
                outputBasketOfApplesStateFarm,
                outputBasketOfApplesStateOwner,
                0,
                outputBasketOfApplesStateParticipants
        );
        StateAndRef inputAppleStampState = createInputStateAppleStamp(outputAppleStampState);
        StateAndRef invalidInputBasketOfApplesStates = createInputStateBasketOfApples(inputBasketOfApplesStatesZeroWeight);
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addInputStates(List.of(inputAppleStampState.getRef(), invalidInputBasketOfApplesStates.getRef()))
                .addOutputState(outputBasketOfApplesState)
                .addCommand(new AppleCommands.Redeem())
                .addSignatories(outputBasketOfApplesStateParticipants)
                .toSignedTransaction();
        assertFailsWith(transaction, "The basket of apple has to weigh more than 0");
    }

    /**
     * Note how this test suite does not contain tests to check for missing or invalid commands.
     * This is because The class to test for BasketOfApples state and AppleCommands.PackBasket command already included
     * tests for the same contract, so implementing it again would be redundantly testing the contract
     **/
}