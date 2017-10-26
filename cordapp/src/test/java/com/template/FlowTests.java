package com.template;

import net.corda.node.internal.StartedNode;
import net.corda.testing.node.MockNetwork;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static net.corda.testing.CoreTestUtils.setCordappPackages;
import static net.corda.testing.CoreTestUtils.unsetCordappPackages;

public class FlowTests {
    private MockNetwork network;
    private StartedNode<MockNetwork.MockNode> a;
    private StartedNode<MockNetwork.MockNode> b;
    private StartedNode<MockNetwork.MockNode> c;

    @Before
    public void setup() {
        setCordappPackages("com.template");
        network = new MockNetwork();
        MockNetwork.BasketOfNodes nodes = network.createSomeNodes(3);
        a = nodes.getPartyNodes().get(0);
        b = nodes.getPartyNodes().get(1);
        c = nodes.getPartyNodes().get(2);
        // For real nodes this happens automatically, but we have to manually register the flow for tests
        for (StartedNode<MockNetwork.MockNode> node : nodes.getPartyNodes()) {
            node.registerInitiatedFlow(TemplateFlow.Responder.class);
        }
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
        unsetCordappPackages();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void test() throws Exception {

    }
}
