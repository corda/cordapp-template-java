package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType;
import com.r3.corda.lib.tokens.contracts.types.TokenPointer;
import com.r3.corda.lib.tokens.workflows.flows.evolvable.CreateEvolvableToken;
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
     *  You can create a Fungible/NonFungible token(digital asset) representing an existing evolvableTokenType which we created
     *  in the CreateEvolvableTokenFlow below.
     */
    @StartableByRPC
    public static class IssueEvolvableTokenFlow extends FlowLogic<SignedTransaction>{
        private final String evolvableTokenId;
        private final Long amount;
        private final Party recipient;

        public IssueEvolvableTokenFlow(String evolvableTokenId, Long amount, Party recipient) {
            this.evolvableTokenId = evolvableTokenId;
            this.amount = amount;
            this.recipient = recipient;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            UUID uuid = UUID.fromString(evolvableTokenId);
            QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(uuid), null,
                    Vault.StateStatus.UNCONSUMED, null);
            StateAndRef<EvolvableTokenType> stateAndRef = getServiceHub().getVaultService().
                    queryBy(EvolvableTokenType.class, queryCriteria).getStates().get(0);
            EvolvableTokenType evolvableTokenType = stateAndRef.getState().getData();
            LinearPointer linearPointer = new LinearPointer(evolvableTokenType.getLinearId(), EvolvableTokenType.class);
            TokenPointer token = new TokenPointer(linearPointer, evolvableTokenType.getFractionDigits());
            return (SignedTransaction) subFlow(new IssueTokens(new Amount(amount, token), this.getOurIdentity(), recipient));
        }
    }

    /**
     * Create instance of EvolvableTokenType and call subflow CreateEvolvableToken to create an evolvableTokenType.
     * You can check the created evolvableTokenType in the Nodes vault_liner_states table, evolvableTokenType being
     * a linear state
     */
    @StartableByRPC
    public static class CreateEvolvableTokenFlow extends FlowLogic<SignedTransaction> {

        private final String data;

        public CreateEvolvableTokenFlow(String data) {
            this.data = data;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            EvolvableTokenType evolvableTokenType = new ExampleEvolvableTokenType(data, getOurIdentity(),
                    new UniqueIdentifier(), 0);
            TransactionState transactionState = new TransactionState(evolvableTokenType, notary);
            return (SignedTransaction) subFlow(new CreateEvolvableToken(transactionState));
        }
    }
}

