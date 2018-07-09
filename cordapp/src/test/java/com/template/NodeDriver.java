package com.template;

import com.google.common.collect.ImmutableList;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.identity.CordaX500Name;
import net.corda.node.services.transactions.ValidatingNotaryService;
import net.corda.testing.driver.DriverParameters;
import net.corda.testing.driver.NodeHandle;
import net.corda.testing.driver.NodeParameters;
import net.corda.testing.node.User;
import com.google.common.collect.ImmutableSet;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static net.corda.testing.driver.Driver.driver;

/**
 * This file is exclusively for being able to run your nodes through an IDE (as opposed to using deployNodes)
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
public class NodeDriver {
    public static void main(String[] args) {
        final User user = new User("user1", "test", ImmutableSet.of("ALL"));
        driver(new DriverParameters().withIsDebug(true).withWaitForAllNodesToFinish(true), dsl -> {
                    CordaFuture<NodeHandle> partyAFuture = dsl.startNode(new NodeParameters()
                            .withProvidedName(new CordaX500Name("PartyA", "London", "GB"))
                            .withRpcUsers(ImmutableList.of(user)));
                    CordaFuture<NodeHandle> partyBFuture = dsl.startNode(new NodeParameters()
                            .withProvidedName(new CordaX500Name("PartyB", "New York", "US"))
                            .withRpcUsers(ImmutableList.of(user)));

                    try {
                        dsl.startWebserver(partyAFuture.get());
                        dsl.startWebserver(partyBFuture.get());
                    } catch (Throwable e) {
                        System.err.println("Encountered exception in node startup: " + e.getMessage());
                        e.printStackTrace();
                    }

                    return null;
                }
        );
    }
}
