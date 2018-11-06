package com.template;

import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCClientConfiguration;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.messaging.DataFeed;
import net.corda.core.node.services.Vault;
import net.corda.core.utilities.NetworkHostAndPort;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Demonstration of how to use the CordaRPCClient to connect to a Corda node and
 * perform RPC operations on the node.
 */
public class TemplateClient {
    private static final Logger logger = LoggerFactory.getLogger(TemplateClient.class);
    private static final String RPC_USERNAME = "user1";
    private static final String RPC_PASSWORD = "test";

    public static void main(String[] args) {
        // Create an RPC connection to the node.
        if (args.length != 1) throw new IllegalArgumentException("Usage: TemplateClient <node address>");
        final NetworkHostAndPort nodeAddress = NetworkHostAndPort.parse(args[0]);
        final CordaRPCClient client = new CordaRPCClient(nodeAddress, CordaRPCClientConfiguration.DEFAULT);
        final CordaRPCOps proxy = client.start(RPC_USERNAME, RPC_PASSWORD).getProxy();

        // Interact with the node.
        // For example, here we grab all existing ContractStates and log them.
        final List<StateAndRef<ContractState>> existingContractStates = proxy.vaultQuery(ContractState.class).getStates();
        for (StateAndRef<ContractState> stateAndRef : existingContractStates) {
            logger.info("{}", stateAndRef.getState().getData());
        }
    }
}