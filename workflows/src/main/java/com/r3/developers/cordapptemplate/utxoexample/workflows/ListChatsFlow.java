package com.r3.developers.cordapptemplate.utxoexample.workflows;

import com.r3.developers.cordapptemplate.utxoexample.states.ChatState;
import net.corda.v5.application.flows.ClientRequestBody;
import net.corda.v5.application.flows.ClientStartableFlow;
import net.corda.v5.application.flows.CordaInject;
import net.corda.v5.application.marshalling.JsonMarshallingService;
import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.UtxoLedgerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

// See Chat CorDapp Design section of the getting started docs for a description of this flow.
public class ListChatsFlow implements ClientStartableFlow {

    private final static Logger log = LoggerFactory.getLogger(ListChatsFlow.class);

    @CordaInject
    public JsonMarshallingService jsonMarshallingService;

    // Injects the UtxoLedgerService to enable the flow to make use of the Ledger API.
    @CordaInject
    public UtxoLedgerService utxoLedgerService;

    @Suspendable
    @Override
    public String call(ClientRequestBody requestBody) {

        log.info("ListChatsFlow.call() called");

        // Queries the VNode's vault for unconsumed states and converts the result to a serializable DTO.
        List<StateAndRef<ChatState>> states = utxoLedgerService.findUnconsumedStatesByType(ChatState.class);
        List<ChatStateResults> results = states.stream().map( stateAndRef ->
            new ChatStateResults(
                    stateAndRef.getState().getContractState().getId(),
                    stateAndRef.getState().getContractState().getChatName(),
                    stateAndRef.getState().getContractState().getMessageFrom().toString(),
                    stateAndRef.getState().getContractState().getMessage()
                    )
        ).collect(Collectors.toList());

        // Uses the JsonMarshallingService's format() function to serialize the DTO to Json.
        return jsonMarshallingService.format(results);
    }
}

/*
RequestBody for triggering the flow via REST:
{
    "clientRequestId": "list-1",
    "flowClassName": "com.r3.developers.cordapptemplate.utxoexample.workflows.ListChatsFlow",
    "requestBody": {}
}
*/