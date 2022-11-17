package com.titles.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.titles.contracts.TitleContract;
import com.titles.states.TitleState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TitleReposesFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction>{

        //private variables
        private Party newOwner;
        private UniqueIdentifier linearId;

        private final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating transaction based on new IOU.");
        private final ProgressTracker.Step VERIFYING_TRANSACTION = new ProgressTracker.Step("Verifying contract constraints.");
        private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with our private key.");
        private final ProgressTracker.Step GATHERING_SIGS = new ProgressTracker.Step("Gathering the owner signature.") {
            @Override
            public ProgressTracker childProgressTracker() {
                return CollectSignaturesFlow.Companion.tracker();
            }
        };
        private final ProgressTracker.Step FINALISING_TRANSACTION = new ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
            @Override
            public ProgressTracker childProgressTracker() {
                return FinalityFlow.Companion.tracker();
            }
        };
        private final ProgressTracker progressTracker = new ProgressTracker(
                GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION,
                SIGNING_TRANSACTION,
                GATHERING_SIGS,
                FINALISING_TRANSACTION
        );
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        //public constructor
        public Initiator(Party newOwner, UniqueIdentifier linearId) throws FlowException {
            this.newOwner = newOwner;
            this.linearId = linearId;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            progressTracker.setCurrentStep(GENERATING_TRANSACTION);
            // get input based on linearId referenced
            //TODO: should this be in constructor?
            List<StateAndRef<TitleState>> titleStateAndRefs = getServiceHub().getVaultService()
                    .queryBy(TitleState.class).getStates();
            StateAndRef<TitleState> inputStateAndRef = titleStateAndRefs.stream().filter(titleStateAndRef -> {
                TitleState state = titleStateAndRef.getState().getData();
                return state.getLinearId().equals(linearId);
            }).findAny().orElseThrow(() -> new IllegalArgumentException("Title not found."));

            TitleState input = inputStateAndRef.getState().getData();

            TitleState output = input.withNewOwner(newOwner);

            List<PublicKey> signers = Arrays.asList(
                    input.getOwner().getOwningKey(),
                    input.getCounty().getOwningKey(),
                    output.getOwner().getOwningKey()
            );
            // build tx
            TransactionBuilder builder = new TransactionBuilder(inputStateAndRef.getState().getNotary())
                    .addInputState(inputStateAndRef)
                    .addOutputState(output)
                    .addCommand(new TitleContract.Commands.Transfer(), signers);
            // verify tx
            progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
            builder.verify(getServiceHub());

            // sign tx
            progressTracker.setCurrentStep(SIGNING_TRANSACTION);
            SignedTransaction selfSigned = getServiceHub().signInitialTransaction(builder);

            // get others sigs
            progressTracker.setCurrentStep(GATHERING_SIGS);
            List<Party> otherParties = Arrays.asList(input.getOwner(), input.getCounty(), output.getOwner());
//            Party me = getOurIdentity();
//            otherParties.remove(getOurIdentity());
            List<FlowSession> sessions = otherParties.stream().filter(party -> !party.equals(getOurIdentity())).map(party ->
                    initiateFlow(party)).collect(Collectors.toList());
//            List<FlowSession> sessions = otherParties.stream().map(el -> initiateFlow(el)).collect(Collectors.toList());
            // TODO: ask why FinalityFlow vs. CollectSignaturesFlow.
            SignedTransaction stx = subFlow(new CollectSignaturesFlow(selfSigned, sessions));

            // finalize
            progressTracker.setCurrentStep(FINALISING_TRANSACTION);
            return subFlow(new FinalityFlow(stx, sessions));
        }
    }

    @InitiatedBy(Initiator.class)
    public static class TransferFlowResponder extends FlowLogic<Void>{
        //private variable
        private FlowSession counterpartySession;

        //Constructor
        public TransferFlowResponder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Suspendable
        @Override
        public Void call() throws FlowException {
//        public SignedTransaction call() throws FlowException {
//            private SignTxFlow(FlowSession otherPartyFlow, ProgressTracker progressTracker) {
//                super(otherPartyFlow, progressTracker);
//            }
            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(counterpartySession) {
                @Suspendable
                @Override
                protected void checkTransaction(SignedTransaction stx) throws FlowException {
                    /*
                     * SignTransactionFlow will automatically verify the transaction and its signatures before signing it.
                     * However, just because a transaction is contractually valid doesn’t mean we necessarily want to sign.
                     * What if we don’t want to deal with the counterparty in question, or the value is too high,
                     * or we’re not happy with the transaction’s structure? checkTransaction
                     * allows us to define these additional checks. If any of these conditions are not met,
                     * we will not sign the transaction - even if the transaction and its signatures are contractually valid.
                     * ----------
                     * For this hello-world cordapp, we will not implement any aditional checks.
                     * */
                }
            });
            //Stored the transaction into data base.
            subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
            return null;
        }
    }

}