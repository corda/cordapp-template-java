package com.r3.developers.cordapptemplate.utxoexample.workflows;

import net.corda.v5.application.flows.*;
import net.corda.v5.application.messaging.FlowMessaging;
import net.corda.v5.application.messaging.FlowSession;
import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.base.types.MemberX500Name;
import net.corda.v5.ledger.utxo.UtxoLedgerService;
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

// See Chat CorDapp Design section of the getting started docs for a description of this flow.

// @InitiatingFlow declares the protocol which will be used to link the initiator to the responder.
@InitiatingFlow(protocol = "finalize-chat-protocol")
public class FinalizeChatSubFlow implements SubFlow<String> {

    private final static Logger log = LoggerFactory.getLogger(FinalizeChatSubFlow.class);
    private final UtxoSignedTransaction signedTransaction;
    private final MemberX500Name otherMember;

    public FinalizeChatSubFlow(UtxoSignedTransaction signedTransaction, MemberX500Name otherMember) {
        this.signedTransaction = signedTransaction;
        this.otherMember = otherMember;
    }

    // Injects the UtxoLedgerService to enable the flow to make use of the Ledger API.
    @CordaInject
    public UtxoLedgerService ledgerService;

    @CordaInject
    public FlowMessaging flowMessaging;

    @Override
    @Suspendable
    public String call() {

        log.info("FinalizeChatFlow.call() called");

        // Initiates a session with the other Member.
        FlowSession session = flowMessaging.initiateFlow(otherMember);

        // Calls the Corda provided finalise() function which gather signatures from the counterparty,
        // notarises the transaction and persists the transaction to each party's vault.
        // On success returns the id of the transaction created. (This is different to the ChatState id)
        String result;
        try {
            List<FlowSession> sessionsList = Arrays.asList(session);

            UtxoSignedTransaction finalizedSignedTransaction = ledgerService.finalize(
                    signedTransaction,
                    sessionsList
            ).getTransaction();

            result = finalizedSignedTransaction.getId().toString();
            log.info("Success! Response: " + result);

        }
        // Soft fails the flow and returns the error message without throwing a flow exception.
        catch (Exception e) {
            log.warn("Finality failed", e);
            result = "Finality failed, " + e.getMessage();
        }
        // Returns the transaction id converted as a string
        return result;
    }
}
