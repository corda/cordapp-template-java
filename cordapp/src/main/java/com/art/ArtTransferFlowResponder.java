package com.art;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;

@InitiatedBy(ArtTransferFlow.class)
public class ArtTransferFlowResponder extends FlowLogic<Void> {
    private FlowSession counterpartySession;

    public ArtTransferFlowResponder(FlowSession counterpartySession) {
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