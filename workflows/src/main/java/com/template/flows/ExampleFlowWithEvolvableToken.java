package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken;
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType;
import com.r3.corda.lib.tokens.contracts.types.TokenPointer;
import com.r3.corda.lib.tokens.contracts.utilities.TransactionUtilitiesKt;
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens;
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens;
import com.template.states.ExampleEvolvableTokenType;
import net.corda.core.contracts.*;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import java.util.UUID;

public class ExampleFlowWithEvolvableToken {

    private ExampleFlowWithEvolvableToken() {
        //Instantiation not allowed
    }

    /**
     *  Issue Non Fungible Token using IssueTokens flow
     */
    @StartableByRPC
    public static class IssueEvolvableTokenFlow extends FlowLogic<SignedTransaction>{
        private final String evolvableTokenId;
        private final Party recipient;

        public IssueEvolvableTokenFlow(String evolvableTokenId, Party recipient) {
            this.evolvableTokenId = evolvableTokenId;
            this.recipient = recipient;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            //using id of evolvable token type to grab the evolvable Token type from db.
            // you can use any custom criteria depending on your requirements
            UUID uuid = UUID.fromString(evolvableTokenId);

            //construct the query criteria
            QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(uuid), null,
                    Vault.StateStatus.UNCONSUMED);

            // grab the token type off the ledger which was created using CreateEvolvableTokens flow
            StateAndRef<ExampleEvolvableTokenType> stateAndRef = getServiceHub().getVaultService().
                    queryBy(ExampleEvolvableTokenType.class, queryCriteria).getStates().get(0);
            ExampleEvolvableTokenType evolvableTokenType = stateAndRef.getState().getData();

            //get the pointer pointer to the evolvable token type
            TokenPointer tokenPointer = evolvableTokenType.toPointer(evolvableTokenType.getClass());

            //assign the issuer to the token type who will be issuing the tokens
            IssuedTokenType issuedTokenType = new IssuedTokenType(getOurIdentity(), tokenPointer);

            //mention the current holder also
            NonFungibleToken nonFungibleToken = new NonFungibleToken(issuedTokenType, recipient, new UniqueIdentifier(), TransactionUtilitiesKt.getAttachmentIdForGenericParam(tokenPointer));

            //call built in flow to issue non fungible tokens
            return subFlow(new IssueTokens(ImmutableList.of(nonFungibleToken)));
        }
    }

    /**
     * Create instance of EvolvableTokenType and call subflow CreateEvolvableToken to create an evolvableTokenType.
     * You can check the created evolvableTokenType in the Nodes vault_liner_states table, evolvableTokenType being
     * a linear state
     */
    @StartableByRPC
    public static class CreateEvolvableTokenFlow extends FlowLogic<SignedTransaction> {

        private final String importantInformationThatMayChange;

        public CreateEvolvableTokenFlow(String importantInformationThatMayChange) {
            this.importantInformationThatMayChange = importantInformationThatMayChange;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            //grab the notary
            Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            //create token type
            ExampleEvolvableTokenType evolvableTokenType = new ExampleEvolvableTokenType(importantInformationThatMayChange, getOurIdentity(),
                    new UniqueIdentifier(), 0);

            //warp it with transaction state specifying the notary
            TransactionState transactionState = new TransactionState(evolvableTokenType, notary);

            //call built in sub flow CreateEvolvableTokens. This can be called via rpc or in unit testing
            return subFlow(new CreateEvolvableTokens(transactionState));
        }
    }
}

