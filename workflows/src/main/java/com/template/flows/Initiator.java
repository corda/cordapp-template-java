package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.TemplateContract;
import com.template.states.TemplateState;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class Initiator extends FlowLogic<SignedTransaction> {

    // We will not use these ProgressTracker for this Hello-World sample
    private final ProgressTracker progressTracker = new ProgressTracker();
    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    //private variables
    private Party sender ;
    private Party receiver;

    //public constructor
    public Initiator(Party sendTo){
        this.receiver = sendTo;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        //Hello World message
        String msg = "Hello-World";
        this.sender = getOurIdentity();

        // Step 1. Get a reference to the notary service on our network and our key pair.
        // Note: ongoing work to support multiple notary identities is still in progress.
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        //Compose the State that carries the Hello World message
        final TemplateState output = new TemplateState(msg,sender,receiver);

        // Step 3. Create a new TransactionBuilder object.
        final TransactionBuilder builder = new TransactionBuilder(notary);

        // Step 4. Add the iou as an output state, as well as a command to the transaction builder.
        builder.addOutputState(output);
        builder.addCommand(new TemplateContract.Commands.Send(), Arrays.asList(this.sender.getOwningKey(),this.receiver.getOwningKey()) );


        // Step 5. Verify and sign it with our KeyPair.
        builder.verify(getServiceHub());
        final SignedTransaction ptx = getServiceHub().signInitialTransaction(builder);


        // Step 6. Collect the other party's signature using the SignTransactionFlow.
        List<Party> otherParties = output.getParticipants().stream().map(el -> (Party)el).collect(Collectors.toList());
        otherParties.remove(getOurIdentity());
        List<FlowSession> sessions = otherParties.stream().map(el -> initiateFlow(el)).collect(Collectors.toList());

        SignedTransaction stx = subFlow(new CollectSignaturesFlow(ptx, sessions));

        // Step 7. Assuming no exceptions, we can now finalise the transaction
        return subFlow(new FinalityFlow(stx, sessions));
    }
}
