package net.corda.test;

import net.corda.core.concurrent.CordaFuture;
import net.corda.core.identity.Party;
import net.corda.core.node.services.ServiceInfo;
import net.corda.node.services.config.VerifierType;
import net.corda.node.services.transactions.SimpleNotaryService;
import net.corda.testing.driver.NodeHandle;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static java.util.Collections.*;
import static net.corda.testing.TestConstants.*;
import static net.corda.testing.driver.Driver.driver;

public class DriverBasedTest {
    @Test
    public void runDriverTest() {
        Party notary = getDUMMY_NOTARY();
        Party bankA = getDUMMY_BANK_A();
        Party bankB = getDUMMY_BANK_B();

        driver(true, dsl -> {
            try {
                HashSet<ServiceInfo> notaryServices = new HashSet<>(Arrays.asList(new ServiceInfo(SimpleNotaryService.Companion.getType(), null)));

                // This starts three nodes simultaneously with startNode, which returns a future that completes when the node
                // has completed startup. Then these are all resolved with getOrThrow which returns the NodeHandle list.
                List<CordaFuture<NodeHandle>> handles = Arrays.asList(
                        dsl.startNode(notary.getName(), notaryServices, emptyList(), VerifierType.InMemory, emptyMap(), null),
                        dsl.startNode(bankA.getName(), emptySet(), emptyList(), VerifierType.InMemory, emptyMap(), null),
                        dsl.startNode(bankB.getName(), emptySet(), emptyList(), VerifierType.InMemory, emptyMap(), null)
                );
                NodeHandle notaryHandle = handles.get(0).get();
                NodeHandle nodeAHandle = handles.get(1).get();
                NodeHandle nodeBHandle = handles.get(2).get();

                // This test will call via the RPC proxy to find a party of another node to verify that the nodes have
                // started and can communicate. This is a very basic test, in practice tests would be starting flows,
                // and verifying the states in the vault and other important metrics to ensure that your CorDapp is working
                // as intended.
                Assert.assertEquals(notaryHandle.getRpc().partyFromX500Name(bankA.getName()).getName(), bankA.getName());
                Assert.assertEquals(nodeAHandle.getRpc().partyFromX500Name(bankB.getName()).getName(), bankB.getName());
                Assert.assertEquals(nodeBHandle.getRpc().partyFromX500Name(notary.getName()).getName(), notary.getName());
            } catch (Exception e) {
               throw new RuntimeException("Caught exception during test" , e);
            }

            return null;
        });
    }
}