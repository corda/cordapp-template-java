package com.art;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;

@InitiatedBy(ArtIssueFlow.class)
public class ArtIssueFlowResponder extends FlowLogic<Void> {
    private FlowSession counterpartySession;

    public ArtIssueFlowResponder(FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        class SignTxFlow extends SignTransactionFlow {
            private SignTxFlow(FlowSession otherPartyFlow, ProgressTracker progressTracker) {
                super(otherPartyFlow, progressTracker);
            }

            @Override
            protected void checkTransaction(SignedTransaction stx) { }
        }

        subFlow(new SignTxFlow(counterpartySession, SignTransactionFlow.Companion.tracker()));

        return null;
    }
}