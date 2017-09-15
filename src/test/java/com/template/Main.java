package com.template;

import com.google.common.collect.ImmutableList;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.node.services.ServiceInfo;
import net.corda.node.services.transactions.ValidatingNotaryService;
import net.corda.nodeapi.User;
import net.corda.testing.driver.DriverParameters;
import net.corda.testing.driver.NodeHandle;
import net.corda.testing.driver.NodeParameters;

import static java.util.Collections.*;
import static net.corda.core.internal.X500NameUtils.getX500Name;
import static net.corda.testing.driver.Driver.driver;

/**
 * This file is exclusively for being able to run your nodes through an IDE (as opposed to running deployNodes)
 * Do not use in a production environment.
 * <p>
 * To debug your CorDapp:
 * <p>
 * 1. Firstly, run the "Run Template CorDapp" run configuration.
 * 2. Wait for all the nodes to start.
 * 3. Note the debug ports which should be output to the console for each node. They typically start at 5006, 5007,
 * 5008. The "Debug CorDapp" configuration runs with port 5007, which should be "NodeB". In any case, double check
 * the console output to be sure.
 * 4. Set your breakpoints in your CorDapp code.
 * 5. Run the "Debug CorDapp" remote debug run configuration.
 */
public class Main {
    public static void main(String[] args) {
        // No permissions required as we are not invoking flows.
        final User user = new User("user1", "test", emptySet());
        driver(new DriverParameters().setIsDebug(true), dsl -> {
                    dsl.startNode(new NodeParameters()
                            .setProvidedName(new CordaX500Name("Controller", "London", "UK"))
                            .setAdvertisedServices(singleton(new ServiceInfo(ValidatingNotaryService.Companion.getType(), null))));

                    try {
                        NodeHandle nodeA = dsl.startNode(new NodeParameters()
                                .setProvidedName(new CordaX500Name("PartyA", "London", "GB"))
                                .setRpcUsers(ImmutableList.of(user))).get();
                        NodeHandle nodeB = dsl.startNode(new NodeParameters()
                                .setProvidedName(new CordaX500Name("PartyB", "New York", "US"))
                                .setRpcUsers(ImmutableList.of(user))).get();

                        dsl.startWebserver(nodeA);
                        dsl.startWebserver(nodeB);

                        dsl.waitForAllNodesToFinish();
                    } catch (Throwable e) {
                        System.err.println("Encountered exception in node startup: " + e.getMessage());
                        e.printStackTrace();
                    }
                    return null;
                }
        );
    }
}