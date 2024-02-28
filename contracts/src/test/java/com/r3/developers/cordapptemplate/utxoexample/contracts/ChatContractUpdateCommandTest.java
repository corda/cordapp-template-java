package com.r3.developers.cordapptemplate.utxoexample.contracts;

import com.r3.corda.ledger.utxo.testing.ContractTest;
import com.r3.developers.cordapptemplate.utxoexample.states.ChatState;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static com.r3.developers.cordapptemplate.utxoexample.contracts.ChatContract.*;

/**
 * This class is an implementation of ContractTest. This provides functions to easily perform unit tests on contracts.
 * This allows us to unit test our implementation of contracts without having to trigger a workflow.
 **/

// This specific class is involved with testing the scenarios involving the ChatState state and the Update command.
public class ChatContractUpdateCommandTest extends ContractTest {

    // The following is a helper function to create a ChatState with default values.
    // This is done so that we can easily re-use this block of code when writing our tests that require an input state
    @SuppressWarnings("unchecked")
    private StateAndRef<ChatState> createInitialChatState() {
        ChatState outputChatState = new ChatContractCreateCommandTest().outputChatState;
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addOutputState(outputChatState)
                .addCommand(new ChatContract.Create())
                .addSignatories(outputChatState.participants)
                .toSignedTransaction();
        transaction.toLedgerTransaction();
        return (StateAndRef<ChatState>) transaction.getOutputStateAndRefs().get(0);
    }

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
        StateAndRef<ChatState> existingState = createInitialChatState();
        ChatState updatedOutputChatState = existingState.getState().getContractState().updateMessage(bobName, "bobResponse");
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addInputState(existingState.getRef())
                .addOutputState(updatedOutputChatState)
                .addCommand(new ChatContract.Update())
                .addSignatories(updatedOutputChatState.participants)
                .toSignedTransaction();
        /**
         *  The assertVerifies function is the general way to test if a contract test passes or fails a transaction.
         *  If the transaction is verified, then it means that the contract tests pass.
         **/
        assertVerifies(transaction);
    }

    @Test
    public void shouldNotHaveNoInputState() {
        // The following test builds a transaction that would fail due to not providing a input state, when the contract
        // expects exactly one input state
        StateAndRef<ChatState> existingState = createInitialChatState();
        ChatState updatedOutputChatState = existingState.getState().getContractState().updateMessage(bobName, "bobResponse");
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addOutputState(updatedOutputChatState)
                .addCommand(new ChatContract.Update())
                .addSignatories(updatedOutputChatState.participants)
                .toSignedTransaction();
        /**
         * The assertFailsWith function is the general way to test for unhappy path test cases contract tests.
         *
         * NOTE: the assertFailsWith method tests if the exact string of the error message matches the expected message
         *       to test if the string of the error message contains a substring within the error message, use the
         *       assertFailsWithMessageContaining() function using the same arguments.
         **/
        assertFailsWith(transaction, "Failed requirement: " + UPDATE_COMMAND_SHOULD_HAVE_ONLY_ONE_INPUT_STATE);
    }

    @Test
    public void shouldNotHaveTwoInputStates() {
        // The following test builds a transaction that would fail due to having two input states, when the contract
        // expects exactly one.
        StateAndRef<ChatState> existingState = createInitialChatState();
        ChatState updatedOutputChatState = existingState.getState().getContractState().updateMessage(bobName, "bobResponse");
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addInputState(existingState.getRef())
                .addInputState(existingState.getRef())
                .addOutputState(updatedOutputChatState)
                .addCommand(new ChatContract.Update())
                .addSignatories(updatedOutputChatState.participants)
                .toSignedTransaction();
        assertFailsWith(transaction, "Failed requirement: " + UPDATE_COMMAND_SHOULD_HAVE_ONLY_ONE_INPUT_STATE);
    }

    @Test
    public void shouldNotHaveTwoOutputStates() {
        // The following test builds a transaction that would fail due to having two output states, when the contract
        // expects exactly one.
        StateAndRef<ChatState> existingState = createInitialChatState();
        ChatState updatedOutputChatState = existingState.getState().getContractState().updateMessage(bobName, "bobResponse");
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addInputState(existingState.getRef())
                .addOutputState(updatedOutputChatState)
                .addOutputState(updatedOutputChatState)
                .addCommand(new ChatContract.Update())
                .addSignatories(updatedOutputChatState.participants)
                .toSignedTransaction();
        assertFailsWith(transaction, "Failed requirement: " + UPDATE_COMMAND_SHOULD_HAVE_ONLY_ONE_OUTPUT_STATE);
    }

    @Test
    public void idShouldNotChange() {
        // The following test builds a transaction that would fail because the contract makes sure that the id of the
        // output state does not change from the input state.
        StateAndRef<ChatState> existingState = createInitialChatState();
        ChatState esDetails = existingState.getState().getContractState();
        ChatState updatedOutputChatState = new ChatState(
                UUID.randomUUID(),
                esDetails.getChatName(),
                bobName,
                "bobResponse",
                esDetails.getParticipants()
        );
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addInputState(existingState.getRef())
                .addOutputState(updatedOutputChatState)
                .addCommand(new ChatContract.Update())
                .addSignatories(updatedOutputChatState.participants)
                .toSignedTransaction();
        assertFailsWith(transaction, "Failed requirement: " + UPDATE_COMMAND_ID_SHOULD_NOT_CHANGE);
    }

    @Test
    public void chatNameShouldNotChange() {
        // The following test builds a transaction that would fail because the contract makes sure that the chatName of
        // the output state does not change from the chat name from the input state.
        StateAndRef<ChatState> existingState = createInitialChatState();
        ChatState esDetails = existingState.getState().getContractState();
        ChatState updatedOutputChatState = new ChatState(
                esDetails.getId(),
                "newName",
                bobName,
                "bobResponse",
                esDetails.getParticipants()
        );
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addInputState(existingState.getRef())
                .addOutputState(updatedOutputChatState)
                .addCommand(new ChatContract.Update())
                .addSignatories(updatedOutputChatState.participants)
                .toSignedTransaction();
        assertFailsWith(transaction, "Failed requirement: " + UPDATE_COMMAND_CHATNAME_SHOULD_NOT_CHANGE);
    }

    @Test
    public void participantsShouldNotChange() {
        // The following test builds a transaction that would fail because the contract makes sure that the list of
        // participants from the output state does not change from the list of participants from the input state.
        StateAndRef<ChatState> existingState = createInitialChatState();
        ChatState esDetails = existingState.getState().getContractState();
        ChatState updatedOutputChatState = new ChatState(
                esDetails.getId(),
                esDetails.getChatName(),
                bobName,
                "bobResponse",
                List.of(bobKey, charlieKey) //  The input state lists 'Alice' and 'Bob' as the participants
        );
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addInputState(existingState.getRef())
                .addOutputState(updatedOutputChatState)
                .addCommand(new ChatContract.Update())
                .addSignatories(updatedOutputChatState.participants)
                .toSignedTransaction();
        assertFailsWith(transaction, "Failed requirement: " + UPDATE_COMMAND_PARTICIPANTS_SHOULD_NOT_CHANGE);
    }

    @Test
    public void outputStateMustBeSigned() {
        // The following test builds a transaction that would fail because it does not include signatories, where the
        // contract expects all the participants to be signatories.
        StateAndRef<ChatState> existingState = createInitialChatState();
        ChatState updatedOutputChatState = existingState.getState().getContractState().updateMessage(bobName, "bobResponse");
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addInputState(existingState.getRef())
                .addOutputState(updatedOutputChatState)
                .addCommand(new ChatContract.Update())
                .toSignedTransaction();
        assertFailsWith(transaction, "Failed requirement: " + TRANSACTION_SHOULD_BE_SIGNED_BY_ALL_PARTICIPANTS);
    }

    @Test
    public void outputStateCannotBeSignedByOnlyOneParticipant() {
        // The following test builds a transaction that would fail because it only includes one signatory, where the
        // contract expects all the participants to be signatories.
        StateAndRef<ChatState> existingState = createInitialChatState();
        ChatState updatedOutputChatState = existingState.getState().getContractState().updateMessage(bobName, "bobResponse");
        UtxoSignedTransaction transaction = getLedgerService()
                .createTransactionBuilder()
                .addInputState(existingState.getRef())
                .addOutputState(updatedOutputChatState)
                .addCommand(new ChatContract.Update())
                .addSignatories(updatedOutputChatState.participants.get(0))
                .toSignedTransaction();
        assertFailsWith(transaction, "Failed requirement: " + TRANSACTION_SHOULD_BE_SIGNED_BY_ALL_PARTICIPANTS);
    }
}
