package com.r3.developers.cordapptemplate.utxoexample.contracts;

import com.r3.corda.ledger.utxo.testing.ContractTest;
import com.r3.developers.cordapptemplate.utxoexample.states.ChatState;
import net.corda.v5.ledger.utxo.Command;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static com.r3.developers.cordapptemplate.utxoexample.contracts.ChatContract.*;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class is an implementation of ContractTest. This provides functions to easily perform unit tests on contracts.
 * This allows us to unit test our implementation of contracts without having to trigger a workflow.
 **/

// This specific class is involved with testing the scenarios involving the ChatState state and the Create command.
public class ChatContractCreateCommandTest extends ContractTest {

    // The following are default values for states so that tests can easily refer and re-use them
    protected ChatState outputChatState = new ChatState(
            UUID.randomUUID(),
            "aliceChatName",
            aliceName,
            "aliceChatMessage",
            List.of(aliceKey, bobKey)
    );

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
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addOutputState(outputChatState)
                .addCommand(new ChatContract.Create())
                .addSignatories(outputChatState.participants)
                .toSignedTransaction();
        /**
         *  The assertVerifies function is the general way to test if a contract test passes or fails a transaction.
         *  If the transaction is verified, then it means that the contract tests pass.
         **/
        assertVerifies(transaction);
    }

    @Test
    public void missingCommand() {
        // The following test builds a transaction that would fail due to not having a command.
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addOutputState(outputChatState)
                .toSignedTransaction();
        assertFailsWith(transaction, "Failed requirement: " + REQUIRE_SINGLE_COMMAND);
    }

    @Test
    public void shouldNotAcceptUnknownCommand() {
        // The following test builds a transaction that would fail due to providing an invalid command.
        class MyDummyCommand implements Command {
        }

        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addOutputState(outputChatState)
                .addCommand(new MyDummyCommand())
                .addSignatories(outputChatState.participants)
                .toSignedTransaction();

        assertFailsWith(transaction, UNKNOWN_COMMAND);
    }

    @Test
    public void outputStateCannotHaveZeroParticipants() {
        // The following test builds a transaction that would fail due to not providing participants, when the contract
        // expects exactly two participants.
        ChatState state = new ChatState(
                UUID.randomUUID(),
                "myChatName",
                aliceName,
                "myChatMessage",
                emptyList()
        );
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addOutputState(state)
                .addCommand(new ChatContract.Create())
                .toSignedTransaction();
        assertFailsWith(transaction, "Failed requirement: " + OUTPUT_STATE_SHOULD_ONLY_HAVE_TWO_PARTICIPANTS);
    }

    @Test
    public void outputStateCannotHaveOneParticipant() {
        // The following test builds a transaction that would fail due to not providing the right number of participants.
        // This test provides a list of only one participant, when the contract expects exactly two participants.
        ChatState state = new ChatState(
                UUID.randomUUID(),
                "myChatName",
                aliceName,
                "myChatMessage",
                List.of(aliceKey)
        );
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addOutputState(state)
                .addCommand(new ChatContract.Create())
                .toSignedTransaction();
        assertFailsWith(transaction, "Failed requirement: " + OUTPUT_STATE_SHOULD_ONLY_HAVE_TWO_PARTICIPANTS);
    }

    @Test
    public void outputStateCannotHaveThreeParticipants() {
        // The following test builds a transaction that would fail due to not providing the right number of participants.
        // This test provides a list of three participants, when the contract expects exactly two participants.
        ChatState state = new ChatState(
                UUID.randomUUID(),
                "myChatName",
                aliceName,
                "myChatMessage",
                List.of(aliceKey, bobKey, charlieKey)
        );
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addOutputState(state)
                .addCommand(new ChatContract.Create())
                .toSignedTransaction();
        assertFailsWith(transaction, "Failed requirement: " + OUTPUT_STATE_SHOULD_ONLY_HAVE_TWO_PARTICIPANTS);
    }

    @Test
    public void outputStateMustBeSigned() {
        // The following test builds a transaction that would fail due to not signing the transaction.
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addOutputState(outputChatState)
                .addCommand(new ChatContract.Create())
                .toSignedTransaction();
        assertFailsWith(transaction, "Failed requirement: " + TRANSACTION_SHOULD_BE_SIGNED_BY_ALL_PARTICIPANTS);
    }

    @Test
    public void outputStateCannotBeSignedByOnlyOneParticipant() {
        // The following test builds a transaction that would fail due to being signed by only one participant and not
        // all participants.
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addOutputState(outputChatState)
                .addCommand(new ChatContract.Create())
                .addSignatories(outputChatState.participants.get(0))
                .toSignedTransaction();
        assertFailsWith(transaction, "Failed requirement: " + TRANSACTION_SHOULD_BE_SIGNED_BY_ALL_PARTICIPANTS);
    }

    @Test
    public void shouldNotIncludeInputState() {
        // The following test builds a transaction that would fail due to providing an input state when the contract did
        // not expect one
        happyPath(); // generate an existing state to search for
        StateAndRef<ChatState> existingState = getLedgerService().findUnconsumedStatesByType(ChatState.class).get(0); // doesn't matter which as this will fail validation
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addInputState(existingState.getRef())
                .addOutputState(outputChatState)
                .addCommand(new ChatContract.Create())
                .addSignatories(outputChatState.participants)
                .toSignedTransaction();
        assertFailsWith(transaction, "Failed requirement: " + CREATE_COMMAND_SHOULD_HAVE_NO_INPUT_STATES);
    }

    @Test
    public void shouldNotHaveTwoOutputStates() {
        // The following test builds a transaction that would fail due to providing two output states when the contract
        // only
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addOutputState(outputChatState)
                .addOutputState(outputChatState)
                .addCommand(new ChatContract.Create())
                .addSignatories(outputChatState.participants)
                .toSignedTransaction();
        assertFailsWith(transaction, "Failed requirement: " + CREATE_COMMAND_SHOULD_HAVE_ONLY_ONE_OUTPUT_STATE);
    }
}
