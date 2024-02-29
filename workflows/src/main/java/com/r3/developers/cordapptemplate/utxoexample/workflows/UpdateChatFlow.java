package com.r3.developers.cordapptemplate.utxoexample.workflows;

import com.r3.developers.cordapptemplate.utxoexample.contracts.ChatContract;
import com.r3.developers.cordapptemplate.utxoexample.states.ChatState;
import net.corda.v5.application.flows.ClientRequestBody;
import net.corda.v5.application.flows.ClientStartableFlow;
import net.corda.v5.application.flows.CordaInject;
import net.corda.v5.application.flows.FlowEngine;
import net.corda.v5.application.marshalling.JsonMarshallingService;
import net.corda.v5.application.membership.MemberLookup;
import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.base.exceptions.CordaRuntimeException;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.UtxoLedgerService;
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction;
import net.corda.v5.ledger.utxo.transaction.UtxoTransactionBuilder;
import net.corda.v5.membership.MemberInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static java.util.Objects.*;
import static java.util.stream.Collectors.toList;

// See Chat CorDapp Design section of the getting started docs for a description of this flow.
public class UpdateChatFlow implements ClientStartableFlow {

    private final static Logger log = LoggerFactory.getLogger(UpdateChatFlow.class);

    @CordaInject
    public JsonMarshallingService jsonMarshallingService;

    @CordaInject
    public MemberLookup memberLookup;

    // Injects the UtxoLedgerService to enable the flow to make use of the Ledger API.
    @CordaInject
    public UtxoLedgerService ledgerService;

    // FlowEngine service is required to run SubFlows.
    @CordaInject
    public FlowEngine flowEngine;

    @Suspendable
    @Override
    public String call(ClientRequestBody requestBody) {

        log.info("UpdateNewChatFlow.call() called");

        try {
            // Obtain the deserialized input arguments to the flow from the requestBody.
             UpdateChatFlowArgs flowArgs = requestBody.getRequestBodyAs(jsonMarshallingService, UpdateChatFlowArgs.class);

            // Look up the latest unconsumed ChatState with the given id.
            // Note, this code brings all unconsumed states back, then filters them.
            // This is an inefficient way to perform this operation when there are a large number of chats.
            // Note, you will get this error if you input an id which has no corresponding ChatState (common error).
            List<StateAndRef<ChatState>> chatStateAndRefs = ledgerService.findUnconsumedStatesByExactType(ChatState.class, 100, Instant.now()).getResults();
            List<StateAndRef<ChatState>> chatStateAndRefsWithId = chatStateAndRefs.stream()
                    .filter(sar -> sar.getState().getContractState().getId().equals(flowArgs.getId())).collect(toList());
            if (chatStateAndRefsWithId.size() != 1) throw new CordaRuntimeException("Multiple or zero Chat states with id " + flowArgs.getId() + " found");
            StateAndRef<ChatState> chatStateAndRef = chatStateAndRefsWithId.get(0);

            // Get MemberInfos for the Vnode running the flow and the otherMember.
            MemberInfo myInfo = memberLookup.myInfo();
            ChatState state = chatStateAndRef.getState().getContractState();

            List<MemberInfo> members = state.getParticipants().stream().map(
                    it -> requireNonNull(memberLookup.lookup(it), "Member not found from public Key "+ it + ".")
            ).collect(toList());
            members.remove(myInfo);
            if(members.size() != 1) throw new RuntimeException("Should be only one participant other than the initiator");
            MemberInfo otherMember = members.get(0);

            // Create a new ChatState using the updateMessage helper function.
            ChatState newChatState = state.updateMessage(myInfo.getName(), flowArgs.getMessage());

            // Use UTXOTransactionBuilder to build up the draft transaction.
            UtxoTransactionBuilder txBuilder = ledgerService.createTransactionBuilder()
                    .setNotary(chatStateAndRef.getState().getNotaryName())
                    .setTimeWindowBetween(Instant.now(), Instant.now().plusMillis(Duration.ofDays(1).toMillis()))
                    .addOutputState(newChatState)
                    .addInputState(chatStateAndRef.getRef())
                    .addCommand(new ChatContract.Update())
                    .addSignatories(newChatState.getParticipants());

            // Convert the transaction builder to a UTXOSignedTransaction. Verifies the content of the
            // UtxoTransactionBuilder and signs the transaction with any required signatories that belong to
            // the current node.
            UtxoSignedTransaction signedTransaction = txBuilder.toSignedTransaction();

            // Call FinalizeChatSubFlow which will finalise the transaction.
            // If successful the flow will return a String of the created transaction id,
            // if not successful it will return an error message.
            return flowEngine.subFlow(new FinalizeChatSubFlow(signedTransaction, otherMember.getName()));


        }
        // Catch any exceptions, log them and rethrow the exception.
        catch (Exception e) {
            log.warn("Failed to process utxo flow for request body " + requestBody + " because: " + e.getMessage());
            throw e;
        }
    }
}

/*
RequestBody for triggering the flow via REST:
{
    "clientRequestId": "update-1",
    "flowClassName": "com.r3.developers.cordapptemplate.utxoexample.workflows.UpdateChatFlow",
    "requestBody": {
        "id":" ** fill in id **",
        "message": "How are you today?"
        }
}
 */