package com.r3.developers.apples.contracts;

import com.r3.developers.apples.states.BasketOfApples;
import net.corda.v5.ledger.utxo.Command;
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction;
import org.junit.jupiter.api.Test;

/**
 * This class is an implementation of the ApplesContractTest which implements the ContractTest abstract class.
 * The ContractTest class provides functions to easily perform unit tests on contracts.
 * The ApplesContractTest adds additional default values for states as well as helper functions to make utxoLedgerTransactions.
 * This allows us to test our implementation of contracts without having to trigger a workflow.
 **/

// This specific class is involved with testing the scenarios involving the BasketOfApples state and the AppleCommands.PackBasket command.
public class BasketOfApplesContractPackBasketCommandTest extends ApplesContractTest {

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
        // The buildTransaction function helps create a utxoLedgerTransaction that can be referenced for contract tests.
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addOutputState(outputBasketOfApplesState)
                .addCommand(new AppleCommands.PackBasket())
                .addSignatories(outputBasketOfApplesStateParticipants)
                .toSignedTransaction();
        /**
         *  The assertVerifies function is the general way to test if a contract test passes or fails a transaction.
         *  If the transaction is verified, then it means that the contract tests pass.
         **/
        assertVerifies(transaction);
    }

    @Test
    public void outputContractStateSizeNoteOne() {
        // The following test builds a transaction that would fail due to not meeting the expectation that a transaction
        // in this CorDapp for this state should only contain one output state.
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addOutputState(outputBasketOfApplesState)
                .addOutputState(outputBasketOfApplesState)
                .addCommand(new AppleCommands.PackBasket())
                .addSignatories(outputBasketOfApplesStateParticipants)
                .toSignedTransaction();
        /**
         * The assertFailsWith function is the general way to test for unhappy path test cases contract tests.
         *
         * The transaction defined above will fail because the contract expects only one output state. However, we built
         * a transaction with two output states. So we expect the transaction to fail, and only 'pass' our test if we
         * can match the error message we expect.
         *
         * NOTE: the assertFailsWith method tests if the exact string of the error message matches the expected message
         *       to test if the string of the error message contains a substring within the error message, use the
         *       assertFailsWithMessageContaining() function using the same arguments.
         **/
        assertFailsWith(transaction, "This transaction should only output one BasketOfApples state");
    }

    @Test
    public void blankDescription() {
        // The following test builds a transaction that would fail due to having an empty stamp description string.
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addOutputState(
                        new BasketOfApples(
                                "",
                                outputBasketOfApplesStateFarm,
                                outputBasketOfApplesStateOwner,
                                outputBasketOfApplesStateWeight,
                                outputBasketOfApplesStateParticipants
                        )
                )
                .addCommand(new AppleCommands.PackBasket())
                .addSignatories(outputBasketOfApplesStateParticipants)
                .toSignedTransaction();
        assertFailsWith(transaction, "The output BasketOfApples state should have clear description of Apple product");
    }

    @Test
    public void basketWeightIsZero() {
        // The following test builds a transaction that would fail due to having a weight not greater than 0
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addOutputState(
                        new BasketOfApples(
                                outputBasketOfApplesStateDescription,
                                outputBasketOfApplesStateFarm,
                                outputBasketOfApplesStateOwner,
                                0,
                                outputBasketOfApplesStateParticipants
                        )
                )
                .addCommand(new AppleCommands.PackBasket())
                .addSignatories(outputBasketOfApplesStateParticipants)
                .toSignedTransaction();
        assertFailsWith(transaction, "The output BasketOfApples state should have non zero weight");
    }

    @Test
    public void basketWeightIsNegative() {
        // The following test builds a transaction that would fail due to having a weight not greater than 0
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addOutputState(
                        new BasketOfApples(
                                outputBasketOfApplesStateDescription,
                                outputBasketOfApplesStateFarm,
                                outputBasketOfApplesStateOwner,
                                -1,
                                outputBasketOfApplesStateParticipants
                        )
                )
                .addCommand(new AppleCommands.PackBasket())
                .addSignatories(outputBasketOfApplesStateParticipants)
                .toSignedTransaction();
        assertFailsWith(transaction, "The output BasketOfApples state should have non zero weight");
    }

    @Test
    public void missingCommand() {
        // The following test builds a transaction that would fail due to not having a command.
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addOutputState(outputBasketOfApplesState)
                .addSignatories(outputBasketOfApplesStateParticipants)
                .toSignedTransaction();
        assertFailsWith(transaction, "Index 0 out of bounds for length 0");
    }

    @Test
    public void unknownCommand() {
        // The following test builds a transaction that would fail due to providing an invalid command.
        class DummyCommand implements Command {
        }

        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addOutputState(outputBasketOfApplesState)
                .addCommand(new DummyCommand())
                .addSignatories(outputBasketOfApplesStateParticipants)
                .toSignedTransaction();
        assertFailsWith(transaction, "Incorrect type of BasketOfApples commands: " +
                "class com.r3.developers.apples.contracts.BasketOfApplesContractPackBasketCommandTest$1DummyCommand");
    }
}