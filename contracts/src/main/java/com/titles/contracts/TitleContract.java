package com.titles.contracts;

import com.titles.states.TitleState;
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
    public static final String ID = "com.titles.contracts.TitleContract";

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
                require.using("There must be oo inputs on Issue Command.",
                        tx.getInputStates().size() == 0);
                require.using("There must be one output on Issue Command.",
                        tx.getOutputStates().size() == 1);
                TitleState output = tx.outputsOfType(TitleState.class).get(0);
                HashSet<PublicKey> requiredSigners = new HashSet<>(
                        Arrays.asList(
                                output.getOwner().getOwningKey(),
                                output.getCounty().getOwningKey()
                        ));
                HashSet<PublicKey> signerKeys = new HashSet<>(tx.getCommand(0).getSigners());
                require.using(
                        "Owner and county can only sign Issue transaction.",
                        requiredSigners.equals(signerKeys));
                return null;
            });
        } else if (commandData instanceof Commands.Transfer) {
            requireThat(require -> {
                require.using("There must be one input on Transfer Command.",
                        tx.getInputStates().size() == 1);
                require.using("There must be one output on Transfer Command.",
                        tx.getOutputStates().size() == 1);
                TitleState input = tx.inputsOfType(TitleState.class).get(0);
                TitleState output = tx.outputsOfType(TitleState.class).get(0);
                require.using("Owner has to be different on Transfer Command.",
                        !input.getOwner().equals(output.getOwner()));
                require.using("County cannot change on Transfer Command.",
                        input.getCounty().equals(output.getCounty()));
                require.using("Address cannot change on Transfer Command.",
                        input.getAddress().equals(output.getAddress()));
                require.using("ParcelId cannot change on Transfer Command.",
                        input.getParcelId().equals(output.getParcelId()));
                require.using("LinearId cannot change on Transfer Command.",
                        input.getLinearId().equals(output.getLinearId()));
                HashSet<PublicKey> requiredSigners = new HashSet<>(
                        Arrays.asList(
                                input.getOwner().getOwningKey(),
                                output.getOwner().getOwningKey(),
                                input.getCounty().getOwningKey()
                        ));
                HashSet<PublicKey> signerKeys = new HashSet<>(tx.getCommand(0).getSigners());
                require.using(
                        "Old Owner, new Owner, and County must sign Transfer Command.",
                        requiredSigners.equals(signerKeys));
                return null;
            });
        } else if (commandData instanceof Commands.Repossess) {
            requireThat(require -> {
                require.using("There must be one input on Repossess Command.",
                        tx.getInputStates().size() == 1);
                require.using("There must be one output on Repossess Command.",
                        tx.getOutputStates().size() == 1);
                TitleState input = tx.inputsOfType(TitleState.class).get(0);
                TitleState output = tx.outputsOfType(TitleState.class).get(0);
                require.using("Owner has to be different on Repossess Command.",
                        !input.getOwner().equals(output.getOwner()));
                require.using("County cannot change on Repossess Command.",
                        input.getCounty().equals(output.getCounty()));
                require.using("Address cannot change on Repossess Command.",
                        input.getAddress().equals(output.getAddress()));
                require.using("ParcelId cannot change on Repossess Command.",
                        input.getParcelId().equals(output.getParcelId()));
                require.using("LinearId cannot change on Repossess Command.",
                        input.getLinearId().equals(output.getLinearId()));
                require.using("New owner must be the County in a Repossess Command",
                        output.getOwner().equals(output.getCounty()));
                HashSet<PublicKey> requiredSigners = new HashSet<>(
                        Arrays.asList(
                                input.getCounty().getOwningKey()
                        ));
                HashSet<PublicKey> signerKeys = new HashSet<>(tx.getCommand(0).getSigners());
                require.using(
                        "County can only sign Repossess Command.",
                        requiredSigners.equals(signerKeys));
                return null;
            });
        } else if (commandData instanceof Commands.Retire) {
            requireThat(require -> {
                require.using("There must be one input on Retire Command.",
                        tx.getInputStates().size() == 1);
                require.using("There must be no output on Retire Command.",
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
                require.using("There must be two or more inputs on Merge Command.",
                        tx.getInputStates().size() >= 2);
                require.using("There must be one output state on Merge Command.",
                        tx.getOutputStates().size() == 1);
                require.using("Merge Command not yet supported.",
                        false);
                return null;
            });
        } else if (commandData instanceof Commands.Split) {
            // TODO: Implement Split command
            requireThat(require -> {
                require.using("There must be one input on Split Command.",
                        tx.getInputStates().size() == 1);
                require.using("There must be two or more outputs on Split Command.",
                        tx.getOutputStates().size() >= 2);
                require.using("Split Command not yet supported.",
                        false);
                return null;
            });
        }
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        class Issue implements Commands {}
        class Transfer implements Commands {}
        class Repossess implements Commands {}
        class Retire implements Commands {}
        class Merge implements Commands {}
        class Split implements Commands {}
    }
}