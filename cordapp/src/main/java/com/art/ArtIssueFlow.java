package com.art;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.corda.core.contracts.Command;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

@InitiatingFlow
@StartableByRPC
public class ArtIssueFlow extends FlowLogic<Void> {
    private final Party owner;
    private final String artist;
    private final String title;
    private final String type;

    public ArtIssueFlow(Party owner, String artist, String title, String type) {
        this.owner = owner;
        this.artist = artist;
        this.title = title;
        this.type = type;
    }

    private final ProgressTracker progressTracker = new ProgressTracker();

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        ArtState artState = new ArtState(getOurIdentity(), owner, artist, title, type);
        final Command<ArtCommands.Issue> txCommand = new Command<>(
                new ArtCommands.Issue(),
                ImmutableList.of(getOurIdentity().getOwningKey()));
        final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(artState, ArtContract.ID)
                .addCommand(txCommand);

        txBuilder.verify(getServiceHub());

        final SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

        subFlow(new FinalityFlow(signedTx));

        return null;
    }
}
