package com.template.flow;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;

/**
 * Define your flow here.
 */
public class TemplateFlow {
    /**
     * You can add a constructor to each FlowLogic subclass to pass objects into the flow.
     */
    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<Void> {
        /**
         * Define the initiator's flow logic here.
         */
        @Suspendable
        @Override public Void call() { return null; }
    }

    @InitiatedBy(Initiator.class)
    public static class Responder extends FlowLogic<Void> {
        private FlowSession counterpartySession;

        public Responder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        /**
         * Define the acceptor's flow logic here.
         */
        @Suspendable
        @Override
        public Void call() { return null; }
    }
}
