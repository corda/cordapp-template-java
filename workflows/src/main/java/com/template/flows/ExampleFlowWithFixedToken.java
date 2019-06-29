package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.tokens.money.FiatCurrency;
import com.r3.corda.lib.tokens.workflows.flows.shell.IssueTokens;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;
import java.util.Currency;

@StartableByRPC
public class ExampleFlowWithFixedToken extends FlowLogic<SignedTransaction> {
    private final ProgressTracker progressTracker = new ProgressTracker();

    private final String currency;
    private final Long amount;
    private final Party recipient;

    public ExampleFlowWithFixedToken(String currency, Long amount, Party recipient) {
        this.currency = currency;
        this.amount = amount;
        this.recipient = recipient;
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        FiatCurrency token = new FiatCurrency(Currency.getInstance(this.currency));
        return (SignedTransaction) subFlow(new IssueTokens(token, amount, this.getOurIdentity(), recipient));
    }
}
