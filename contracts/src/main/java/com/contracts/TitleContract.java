package com.template.contracts;

import com.template.states.TemplateState;
import com.template.states.TitleState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.AbstractParty;
import net.corda.core.transactions.LedgerTransaction;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashSet;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class TitleContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.template.contracts.TitleContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {

        /* We can use the requireSingleCommand function to extract command data from transaction.
         * However, it is possible to have multiple commands in a single transaction.*/
        final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        final CommandData commandData = command.getValue();

        if (commandData instanceof Commands.Issue) {
            requireThat(require -> {
                require.using("No inputs should be consumed on Issue transaction.",
                        tx.getInputStates().size() == 0);
                require.using("There should be one output state on Issue transaction.",
                        tx.getOutputStates().size() == 1);
                TitleState output = tx.outputsOfType(TitleState.class).get(0);
                HashSet<PublicKey> requiredSigners = new HashSet<>(
                        Arrays.asList(
                                output.getOwner().getOwningKey(),
                                output.getCounty().getOwningKey()
                        ));
                HashSet<PublicKey> signerKeys = new HashSet<>(tx.getCommand(0).getSigners());
                require.using(
                        "Owner and county can only may sign Issue transaction.",
                        requiredSigners.equals(signerKeys));
                return null;
            });
        } else if (commandData instanceof Commands.Transfer) {
            requireThat(require -> {
                require.using("There should be one input should be consumed on Transfer transaction.",
                        tx.getInputStates().size() == 1);
                require.using("There should be one output state on Transfer transaction.",
                        tx.getOutputStates().size() == 1);
                TitleState input = tx.inputsOfType(TitleState.class).get(0);
                TitleState output = tx.outputsOfType(TitleState.class).get(0);
                require.using("County cannot change County on Transfer transaction.",
                        input.getCounty().equals(output.getCounty()));
                require.using("Owner has to be different on Transfer transaction.",
                        !input.getOwner().equals(output.getOwner()));
                HashSet<PublicKey> requiredSigners = new HashSet<>(
                        Arrays.asList(
                                input.getOwner().getOwningKey(),
                                output.getOwner().getOwningKey(),
                                input.getCounty().getOwningKey()
                        ));
                HashSet<PublicKey> signerKeys = new HashSet<>(tx.getCommand(0).getSigners());
                require.using(
                        "Old Owner, new Owner, and County can only sign Transfer transaction.",
                        requiredSigners.equals(signerKeys));
                return null;
            });
        } else if (commandData instanceof Commands.Retire) {
            requireThat(require -> {
                require.using("There should be one input should be consumed on Retire transaction.",
                        tx.getInputStates().size() == 1);
                require.using("There should be no output state on Retire transaction.",
                        tx.getOutputStates().size() == 0);
                TitleState input = tx.inputsOfType(TitleState.class).get(0);
                HashSet<PublicKey> requiredSigners = new HashSet<>(
                        Arrays.asList(
                                input.getOwner().getOwningKey(),
                                input.getCounty().getOwningKey()
                        ));
                HashSet<PublicKey> signerKeys = new HashSet<>(tx.getCommand(0).getSigners());
                require.using(
                        "Old owner has to sign Retire transaction.",
                        requiredSigners.equals(signerKeys));
                return null;
            });
        } else if (commandData instanceof Commands.Merge) {
            // TODO: Implement Merge command
            requireThat(require -> {
                require.using("There should be two or more inputs consumed on Merge transaction.",
                        tx.getInputStates().size() >= 2);
                require.using("There should be one output state on Merge transaction.",
                        tx.getOutputStates().size() == 1);
                require.using("Merge transaction not yet supported.",
                        false);
                return null;
            });
        } else if (commandData instanceof Commands.Split) {
            // TODO: Implement Split command
            requireThat(require -> {
                require.using("There should be one input on Split transaction.",
                        tx.getInputStates().size() == 1);
                require.using("There should be two or more output states on Split transaction.",
                        tx.getOutputStates().size() >= 2);
                require.using("Split transaction not yet supported.",
                        false);
                return null;
            });
        }
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        //In our hello-world app, We will only have one command.
        class Issue implements Commands {}
        class Transfer implements Commands {}
        class Retire implements Commands {}
        class Merge implements Commands {}
        class Split implements Commands {}
    }
}