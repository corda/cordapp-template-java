package com.art;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.List;

@InitiatingFlow
@StartableByRPC
public class ArtTransferFlow extends FlowLogic<Void> {
    private final Party newOwner;
    private final String artist;
    private final String title;

    public ArtTransferFlow(Party newOwner, String artist, String title) {
        this.newOwner = newOwner;
        this.artist = artist;
        this.title = title;
    }

    private final ProgressTracker progressTracker = new ProgressTracker();

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        final List<StateAndRef<ArtState>> artStateAndRefs = getServiceHub().getVaultService().queryBy(ArtState.class).getStates();
        StateAndRef<ArtState> inputArtStateAndRef = artStateAndRefs.stream().filter(artStateAndRef -> {
            ArtState state = artStateAndRef.getState().getData();
            return state.getArtist().equals(artist) && state.getTitle().equals(title);
        }).findFirst().get();
        ArtState inputArtState = inputArtStateAndRef.getState().getData();

        ArtState outputArtState = new ArtState(inputArtState.getAppraiser(), newOwner, artist, title, inputArtState.getType());
        final Command<ArtCommands.Transfer> txCommand = new Command<>(
                new ArtCommands.Transfer(),
                ImmutableList.of(getOurIdentity().getOwningKey()));
        final TransactionBuilder txBuilder = new TransactionBuilder(inputArtStateAndRef.getState().getNotary())
                .addInputState(inputArtStateAndRef)
                .addOutputState(outputArtState, ArtContract.ID)
                .addCommand(txCommand);

        txBuilder.verify(getServiceHub());

        final SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

        subFlow(new FinalityFlow(signedTx));

        return null;
    }
}
