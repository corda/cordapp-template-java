package com.template.flow;

import net.corda.node.internal.StartedNode;
import net.corda.testing.node.MockNetwork;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FlowTests {
    private MockNetwork net;
    private StartedNode a;
    private StartedNode b;
    private StartedNode c;

    @Before
    public void setup() {
        net = new MockNetwork();
        MockNetwork.BasketOfNodes nodes = net.createSomeNodes(3);
        a = nodes.getPartyNodes().get(0);
        b = nodes.getPartyNodes().get(1);
        c = nodes.getPartyNodes().get(2);
        // For real nodes this happens automatically, but we have to manually register the flow for tests.
        for (StartedNode node : nodes.getPartyNodes()) {
            node.getInternals().registerInitiatedFlow(TemplateFlow.Responder.class);
        }
        net.runNetwork();
    }

    @After
    public void tearDown() {
        net.stopNodes();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void dummyTest() throws Exception {

    }
}