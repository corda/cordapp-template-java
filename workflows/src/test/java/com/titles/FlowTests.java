package com.titles;

import com.google.common.collect.ImmutableList;
import com.titles.flows.TitleIssueFlow;
import com.titles.states.TitleState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Future;

public class FlowTests {
    private MockNetwork network;
    private StartedMockNode a;
    private StartedMockNode b;

    @Before
    public void setup() {
        network = new MockNetwork(new MockNetworkParameters().withCordappsForAllNodes(ImmutableList.of(
                TestCordapp.findCordapp("com.titles.contracts"),
                TestCordapp.findCordapp("com.titles.flows")))
                .withNotarySpecs(ImmutableList.of(new MockNetworkNotarySpec(CordaX500Name.parse("O=Notary,L=London,C=GB")))));
        a = network.createPartyNode(null);
        b = network.createPartyNode(null);
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    @Test
    public void dummyTest() throws Exception {
        TitleIssueFlow.TitleFlowInitiator flow = new TitleIssueFlow.TitleFlowInitiator(
                a.getInfo().getLegalIdentities().get(0),
                b.getInfo().getLegalIdentities().get(0),
                "1234567890", "parcelId");
        Future<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();

        //successful query means the state is stored at node b's vault. Flow went through.
//        QueryCriteria.VaultQueryCriteria inputCriteria = new QueryCriteria.VaultQueryCriteria().withStatus(Vault.StateStatus.UNCONSUMED);
//        TitleState state = a.getServices().getVaultService().queryBy(TitleState.class, inputCriteria)
//                .getStates().get(0).getState().getData();
    }
}
