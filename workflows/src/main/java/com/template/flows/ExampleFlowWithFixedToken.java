package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.tokens.contracts.states.FungibleToken;
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType;
import com.r3.corda.lib.tokens.contracts.types.TokenType;
import com.r3.corda.lib.tokens.contracts.utilities.TransactionUtilitiesKt;
import com.r3.corda.lib.tokens.money.FiatCurrency;
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens;
import net.corda.core.contracts.Amount;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;

@StartableByRPC
public class ExampleFlowWithFixedToken extends FlowLogic<SignedTransaction> {
    private final ProgressTracker progressTracker = new ProgressTracker();

    private final String currency;
    private final Long quantity;
    private final Party recipient;

    public ExampleFlowWithFixedToken(String currency, Long quantity, Party recipient) {
        this.currency = currency;
        this.quantity = quantity;
        this.recipient = recipient;
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        //get the fixed token type
        TokenType token = FiatCurrency.Companion.getInstance(currency);

        //assign the issuer who will be issuing the tokens
        IssuedTokenType issuedTokenType = new IssuedTokenType(getOurIdentity(), token);

        //specify how much amount to issue to holder
        Amount<IssuedTokenType> amount = new Amount(quantity, issuedTokenType);

        //create fungible amount specifying the new owner
        FungibleToken fungibleToken  = new FungibleToken(amount, recipient, TransactionUtilitiesKt.getAttachmentIdForGenericParam(token));

        //use built in flow for issuing tokens on ledger
        return subFlow(new IssueTokens(ImmutableList.of(fungibleToken)));
    }
}
