package com.r3.developers.cordapptemplate.utxoexample.workflows;

import com.r3.developers.cordapptemplate.utxoexample.states.ChatState;
import net.corda.v5.application.flows.ClientRequestBody;
import net.corda.v5.application.flows.ClientStartableFlow;
import net.corda.v5.application.flows.CordaInject;
import net.corda.v5.application.marshalling.JsonMarshallingService;
import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.base.exceptions.CordaRuntimeException;
import net.corda.v5.crypto.SecureHash;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.UtxoLedgerService;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.toList;

// See Chat CorDapp Design section of the getting started docs for a description of this flow.
public class GetChatFlow implements ClientStartableFlow {

    private final static Logger log = LoggerFactory.getLogger(GetChatFlow.class);

    @CordaInject
    public JsonMarshallingService jsonMarshallingService;

    // Injects the UtxoLedgerService to enable the flow to make use of the Ledger API.
    @CordaInject
    public UtxoLedgerService ledgerService;

    @Override
    @Suspendable
    public String call(ClientRequestBody requestBody)  {

        // Obtain the deserialized input arguments to the flow from the requestBody.
        GetChatFlowArgs flowArgs = requestBody.getRequestBodyAs(jsonMarshallingService, GetChatFlowArgs.class);

        // Look up the latest unconsumed ChatState with the given id.
        // Note, this code brings all unconsumed states back, then filters them.
        // This is an inefficient way to perform this operation when there are a large number of chats.
        // Note, you will get this error if you input an id which has no corresponding ChatState (common error).
        List<StateAndRef<ChatState>> chatStateAndRefs = ledgerService.findUnconsumedStatesByType(ChatState.class);
        List<StateAndRef<ChatState>> chatStateAndRefsWithId = chatStateAndRefs.stream()
                .filter(sar -> sar.getState().getContractState().getId().equals(flowArgs.getId())).collect(toList());
        if (chatStateAndRefsWithId.size() != 1) throw new CordaRuntimeException("Multiple or zero Chat states with id " + flowArgs.getId() + " found");
        StateAndRef<ChatState> chatStateAndRef = chatStateAndRefsWithId.get(0);

        // Calls resolveMessagesFromBackchain() which retrieves the chat history from the backchain.
        return jsonMarshallingService.format(resolveMessagesFromBackchain(chatStateAndRef, flowArgs.getNumberOfRecords() ));
    }

    // resoveMessageFromBackchain() starts at the stateAndRef provided, which represents the unconsumed head of the
    // backchain for this particular chat, then walks the chain backwards for the number of transaction specified in
    // the numberOfRecords argument. For each transaction it adds the MessageAndSender representing the
    // message and who sent it to a list which is then returned.
    @Suspendable
    private List<MessageAndSender> resolveMessagesFromBackchain(StateAndRef<?> stateAndRef, int numberOfRecords) {

        // Set up a Mutable List to collect the MessageAndSender(s)
        List<MessageAndSender> messages = new LinkedList<>();

        // Set up initial conditions for walking the backchain.
        StateAndRef<?> currentStateAndRef = stateAndRef;
        int recordsToFetch = numberOfRecords;
        boolean moreBackchain = true;

        // Continue to loop until the start of the backchain or enough records have been retrieved.
        while (moreBackchain) {

            // Obtain the transaction id from the current StateAndRef and fetch the transaction from the vault.
            SecureHash transactionId = currentStateAndRef.getRef().getTransactionId();
            UtxoLedgerTransaction transaction = requireNonNull(
                 ledgerService.findLedgerTransaction(transactionId),
                 "Transaction " +  transactionId + " not found."
            );

            // Get the output state from the transaction and use it to create a MessageAndSender Object which
            // is appended to the mutable list.
            List<ChatState> chatStates = transaction.getOutputStates(ChatState.class);
            if (chatStates.size() != 1) throw new CordaRuntimeException(
                    "Expecting one and only one ChatState output for transaction " + transactionId + ".");
            ChatState output = chatStates.get(0);

            messages.add(new MessageAndSender(output.getMessageFrom().toString(), output.getMessage()));
            // Decrement the number of records to fetch.
            recordsToFetch--;

            // Get the reference to the input states.
            List<StateAndRef<?>> inputStateAndRefs = transaction.getInputStateAndRefs();

            // Check if there are no more input states (start of chain) or we have retrieved enough records.
            // Check the transaction is not malformed by having too many input states.
            // Set the currentStateAndRef to the input StateAndRef, then repeat the loop.
	        if (inputStateAndRefs.isEmpty() || recordsToFetch == 0) {
	            moreBackchain = false;
	        } else if (inputStateAndRefs.size() > 1) {
	            throw new CordaRuntimeException("More than one input state found for transaction " + transactionId + ".");
	        } else {
	            currentStateAndRef = inputStateAndRefs.get(0);
	        }
        }
        return messages;
    }
}

/*
RequestBody for triggering the flow via REST:
{
    "clientRequestId": "get-1",
    "flowClassName": "com.r3.developers.cordapptemplate.utxoexample.workflows.GetChatFlow",
    "requestBody": {
        "id":"** fill in id **",
        "numberOfRecords":"4"
    }
}
 */

