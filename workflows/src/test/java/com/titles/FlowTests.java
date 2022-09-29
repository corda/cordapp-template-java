package com.titles;

import com.google.common.collect.ImmutableList;
import com.titles.flows.TitleIssueFlow;
import com.titles.flows.TitleTransferFlow;
import com.titles.states.TitleState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.Future;




public class FlowTests {
    private MockNetwork network;
    private StartedMockNode a;
    private StartedMockNode b;
    private StartedMockNode county;
    String address = "1 West St New York NY USA 10004";
    String parcelId = "p1234";

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
    public void issueFlowTest() throws Exception {
        TitleIssueFlow.Initiator issueFlow = new TitleIssueFlow.Initiator(
                a.getInfo().getLegalIdentities().get(0),
                county.getInfo().getLegalIdentities().get(0),
                address, parcelId);
        Future<SignedTransaction> future = county.startFlow(issueFlow);
        network.runNetwork();

        //successful query means the state is stored at node a's vault. Flow went through.
//        QueryCriteria.VaultQueryCriteria inputCriteria = new QueryCriteria.VaultQueryCriteria(
//                Vault.StateStatus.UNCONSUMED);
        TitleState state = a.getServices().getVaultService().queryBy(TitleState.class)
                .getStates().get(0).getState().getData();

        assertNotNull(state); // we have a state?

        // county and owner correct?
        assertEquals(state.getCounty(), county.getInfo().getLegalIdentities().get(0));
        assertEquals(state.getOwner(), a.getInfo().getLegalIdentities().get(0));
        assertEquals(state.getAddress(), address);
        assertEquals(state.getParcelId(), parcelId);
    }
    @Test
    public void transferFlowTest() throws Exception {
        // same as issue state to start
        TitleIssueFlow.Initiator issueFlow = new TitleIssueFlow.Initiator(
                a.getInfo().getLegalIdentities().get(0),
                county.getInfo().getLegalIdentities().get(0),
                address, parcelId);
        Future<SignedTransaction> issueFuture = county.startFlow(issueFlow);
        network.runNetwork();
        TitleState state = a.getServices().getVaultService().queryBy(TitleState.class)
                .getStates().get(0).getState().getData();
        // lets transfer the state
        TitleTransferFlow.Initiator transferFlow = new TitleTransferFlow.Initiator(
                b.getInfo().getLegalIdentities().get(0),
                state.getLinearId()
        );

        Future<SignedTransaction> transferFuture = county.startFlow(transferFlow);
        network.runNetwork();
        TitleState transferedState = b.getServices().getVaultService().queryBy(TitleState.class)
                .getStates().get(0).getState().getData();
        assertNotNull(state); // we have a state?

        // county and owner correct?
        assertEquals(transferedState.getCounty(), county.getInfo().getLegalIdentities().get(0));
        assertEquals(transferedState.getOwner(), b.getInfo().getLegalIdentities().get(0));
        assertEquals(transferedState.getAddress(), address);
        assertEquals(transferedState.getParcelId(), parcelId);

        // TODO: how to check that Alice had her state consumed?
        System.out.println(88);
    }
}
