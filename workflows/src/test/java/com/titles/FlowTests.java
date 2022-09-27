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
    private StartedMockNode county;

    @Before
    public void setup() {
        network = new MockNetwork(new MockNetworkParameters().withCordappsForAllNodes(ImmutableList.of(
                TestCordapp.findCordapp("com.titles.contracts"),
                TestCordapp.findCordapp("com.titles.flows")))
                .withNotarySpecs(ImmutableList.of(new MockNetworkNotarySpec(CordaX500Name.parse("O=Notary,L=London,C=GB")))));
        a = network.createPartyNode(CordaX500Name.parse("O=Alice,L=NYC,C=US"));
        b = network.createPartyNode(CordaX500Name.parse("O=Bob,L=NYC,C=US"));
        county = network.createPartyNode(CordaX500Name.parse("O=County,L=NYC,C=US"));
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
                county.getInfo().getLegalIdentities().get(0),
                "1 West St New York NY USA 10004", "parcelId");
        Future<SignedTransaction> future = county.startFlow(flow);
        network.runNetwork();

        //successful query means the state is stored at node b's vault. Flow went through.
        QueryCriteria.VaultQueryCriteria inputCriteria = new QueryCriteria.VaultQueryCriteria(
//                .withStatus(
                Vault.StateStatus.UNCONSUMED);
        TitleState state = a.getServices().getVaultService().queryBy(TitleState.class)
                .getStates().get(0).getState().getData();
        System.out.print(99);
    }
}
