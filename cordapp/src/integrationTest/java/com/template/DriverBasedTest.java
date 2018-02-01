package com.template;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.utilities.NetworkHostAndPort;
import net.corda.node.services.transactions.SimpleNotaryService;
import net.corda.nodeapi.internal.ServiceInfo;
import net.corda.testing.driver.DriverParameters;
import net.corda.testing.driver.NodeHandle;
import net.corda.testing.driver.NodeParameters;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;

import static net.corda.testing.TestConstants.*;
import static net.corda.testing.driver.Driver.driver;
import static org.junit.Assert.assertEquals;

public class DriverBasedTest {
    private Party notary = getDUMMY_NOTARY();
    private Party bankA = getDUMMY_BANK_A();
    private Party bankB = getDUMMY_BANK_B();
    private List<CordaX500Name> nodeNames = ImmutableList.of(notary.getName(), bankA.getName(), bankB.getName());

    @Test
    public void nodeTest() {
        driver(new DriverParameters().setIsDebug(true).setStartNodesInProcess(true), dsl -> {
            HashSet<ServiceInfo> notaryServices = Sets.newHashSet(new ServiceInfo(SimpleNotaryService.Companion.getType(), null));

            // This starts three nodes simultaneously with startNode, which returns a future that completes when the node
            // has completed startup. Then these are all resolved with getOrThrow which returns the NodeHandle list.
            List<CordaFuture<NodeHandle>> handleFutures = ImmutableList.of(
                    dsl.startNode(new NodeParameters().setProvidedName(nodeNames.get(0)).setAdvertisedServices(notaryServices)),
                    dsl.startNode(new NodeParameters().setProvidedName(nodeNames.get(1))),
                    dsl.startNode(new NodeParameters().setProvidedName(nodeNames.get(2)))
            );

            try {
                NodeHandle notaryHandle = handleFutures.get(0).get();
                NodeHandle partyAHandle = handleFutures.get(1).get();
                NodeHandle partyBHandle = handleFutures.get(2).get();

                // This test will call via the RPC proxy to find a party of another node to verify that the nodes have
                // started and can communicate. This is a very basic test, in practice tests would be starting flows,
                // and verifying the states in the vault and other important metrics to ensure that your CorDapp is working
                // as intended.
                assertEquals(notaryHandle.getRpc().wellKnownPartyFromX500Name(bankA.getName()).getName(), bankA.getName());
                assertEquals(partyAHandle.getRpc().wellKnownPartyFromX500Name(bankB.getName()).getName(), bankB.getName());
                assertEquals(partyBHandle.getRpc().wellKnownPartyFromX500Name(notary.getName()).getName(), notary.getName());
            } catch (Exception e) {
                throw new RuntimeException("Caught exception during test", e);
            }

            return null;
        });
    }

    @Test
    public void nodeWebserverTest() {
        driver(new DriverParameters().setIsDebug(true).setStartNodesInProcess(true), dsl -> {
            HashSet<ServiceInfo> notaryServices = Sets.newHashSet(new ServiceInfo(SimpleNotaryService.Companion.getType(), null));

            List<CordaFuture<NodeHandle>> handleFutures = ImmutableList.of(
                    dsl.startNode(new NodeParameters().setProvidedName(notary.getName()).setAdvertisedServices(notaryServices)),
                    dsl.startNode(new NodeParameters().setProvidedName(bankA.getName())),
                    dsl.startNode(new NodeParameters().setProvidedName(bankB.getName()))
            );

            try {
                // This test starts each node's webserver and makes an HTTP call to retrieve the body of a GET endpoint on
                // the node's webserver, to verify that the nodes' webservers have started and have loaded the API.
                for (CordaFuture<NodeHandle> handleFuture : handleFutures) {
                    NodeHandle nodeHandle = handleFuture.get();

                    dsl.startWebserver(nodeHandle).get();

                    NetworkHostAndPort nodeAddress = nodeHandle.getWebAddress();
                    String url = String.format("http://%s/api/template/templateGetEndpoint", nodeAddress);

                    Request request = new Request.Builder().url(url).build();
                    OkHttpClient client = new OkHttpClient();
                    Response response = client.newCall(request).execute();

                    assertEquals("Template GET endpoint.", response.body().string());
                }
            } catch (Exception e) {
                throw new RuntimeException("Caught exception during test", e);
            }

            return null;
        });
    }
}