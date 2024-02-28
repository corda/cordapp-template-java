package com.r3.developers.cordapptemplate.utxoexample.contracts;

import com.r3.developers.cordapptemplate.utxoexample.states.ChatState;
import net.corda.v5.base.exceptions.CordaRuntimeException;
import net.corda.v5.ledger.utxo.Command;
import net.corda.v5.ledger.utxo.Contract;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ChatContract implements Contract {

    private final static Logger log = LoggerFactory.getLogger(ChatContract.class);

    // Use constants to hold the error messages
    // This allows the tests to use them, meaning if they are updated you won't need to fix tests just because the wording was updated
    static final String REQUIRE_SINGLE_COMMAND = "Require a single command.";
    static final String UNKNOWN_COMMAND = "Unsupported command";
    static final String OUTPUT_STATE_SHOULD_ONLY_HAVE_TWO_PARTICIPANTS = "The output state should have two and only two participants.";
    static final String TRANSACTION_SHOULD_BE_SIGNED_BY_ALL_PARTICIPANTS = "The transaction should have been signed by both participants.";

    static final String CREATE_COMMAND_SHOULD_HAVE_NO_INPUT_STATES = "When command is Create there should be no input states.";
    static final String CREATE_COMMAND_SHOULD_HAVE_ONLY_ONE_OUTPUT_STATE = "When command is Create there should be one and only one output state.";

    static final String UPDATE_COMMAND_SHOULD_HAVE_ONLY_ONE_INPUT_STATE = "When command is Update there should be one and only one input state.";
    static final String UPDATE_COMMAND_SHOULD_HAVE_ONLY_ONE_OUTPUT_STATE = "When command is Update there should be one and only one output state.";
    static final String UPDATE_COMMAND_ID_SHOULD_NOT_CHANGE = "When command is Update id must not change.";
    static final String UPDATE_COMMAND_CHATNAME_SHOULD_NOT_CHANGE = "When command is Update chatName must not change.";
    static final String UPDATE_COMMAND_PARTICIPANTS_SHOULD_NOT_CHANGE = "When command is Update participants must not change.";

    public static class Create implements Command { }
    public static class Update implements Command { }

    @Override
    public void verify(UtxoLedgerTransaction transaction) {

        requireThat( transaction.getCommands().size() == 1, REQUIRE_SINGLE_COMMAND);
        Command command = transaction.getCommands().get(0);

        ChatState output = transaction.getOutputStates(ChatState.class).get(0);

        requireThat(output.getParticipants().size() == 2, OUTPUT_STATE_SHOULD_ONLY_HAVE_TWO_PARTICIPANTS);
        requireThat(transaction.getSignatories().containsAll(output.getParticipants()), TRANSACTION_SHOULD_BE_SIGNED_BY_ALL_PARTICIPANTS);

        if(command.getClass() == Create.class) {
            requireThat(transaction.getInputContractStates().isEmpty(), CREATE_COMMAND_SHOULD_HAVE_NO_INPUT_STATES);
            requireThat(transaction.getOutputContractStates().size() == 1, CREATE_COMMAND_SHOULD_HAVE_ONLY_ONE_OUTPUT_STATE);
        }
        else if(command.getClass() == Update.class) {
            requireThat(transaction.getInputContractStates().size() == 1, UPDATE_COMMAND_SHOULD_HAVE_ONLY_ONE_INPUT_STATE);
            requireThat(transaction.getOutputContractStates().size() == 1, UPDATE_COMMAND_SHOULD_HAVE_ONLY_ONE_OUTPUT_STATE);

            ChatState input = transaction.getInputStates(ChatState.class).get(0);
            requireThat(input.getId().equals(output.getId()), UPDATE_COMMAND_ID_SHOULD_NOT_CHANGE);
            requireThat(input.getChatName().equals(output.getChatName()), UPDATE_COMMAND_CHATNAME_SHOULD_NOT_CHANGE);
            requireThat(
                    input.getParticipants().containsAll(output.getParticipants()) &&
                    output.getParticipants().containsAll(input.getParticipants()),
                    UPDATE_COMMAND_PARTICIPANTS_SHOULD_NOT_CHANGE);
        }
        else {
            throw new CordaRuntimeException(UNKNOWN_COMMAND);
        }
    }

    private void requireThat(boolean asserted, String errorMessage) {
        if(!asserted) {
            throw new CordaRuntimeException("Failed requirement: " + errorMessage);
        }
    }
}
