package com.titles.contracts;

import com.titles.states.TitleState;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import java.util.Arrays;

import static net.corda.testing.node.NodeTestUtils.ledger;


public class TitleTransferTests {
    private final MockServices ledgerServices = new MockServices(Arrays.asList("com"));
    TestIdentity alice = new TestIdentity(new CordaX500Name("Alice",  "TestLand",  "US"));
    TestIdentity bob = new TestIdentity(new CordaX500Name("Bob",  "TestLand",  "US"));
    TestIdentity county = new TestIdentity(new CordaX500Name("County",  "Brooklyn",  "US"));

    @Test
    public void transferCommandTest() {
        TitleState input = new TitleState(alice.getParty(), county.getParty(), "123 Main St", "123456789");
        TitleState output = input.withNewOwner(bob.getParty());

        TitleState badAddress = new TitleState(bob.getParty(), county.getParty(), "125 Main St", "123456789", input.getLinearId());
        TitleState badParcelId = new TitleState(bob.getParty(), county.getParty(), "123 Main St", "123456", input.getLinearId());
        TitleState badCounty = new TitleState(bob.getParty(), alice.getParty(), "123 Main St", "123456789", input.getLinearId());
        TitleState badNewOwner = new TitleState(alice.getParty(), county.getParty(), "123 Main St", "123456789", input.getLinearId());
        TitleState badLinearId = new TitleState(bob.getParty(), county.getParty(), "123 Main St", "123456789");

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(TitleContract.ID, output);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), bob.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Transfer());
                return tx.failsWith("There must be one input on Transfer Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, input);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), bob.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Transfer());
                return tx.failsWith("There must be one output on Transfer Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, input);
                tx.output(TitleContract.ID, badNewOwner);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), bob.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Transfer());
                return tx.failsWith("Owner has to change on Transfer Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, input);
                tx.output(TitleContract.ID, badAddress);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), bob.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Transfer());
                return tx.failsWith("All attributes (except new owner) must be the same on Transfer Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, input);
                tx.output(TitleContract.ID, badParcelId);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), bob.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Transfer());
                return tx.failsWith("All attributes (except new owner) must be the same on Transfer Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, input);
                tx.output(TitleContract.ID, badCounty);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), bob.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Transfer());
                return tx.failsWith("All attributes (except new owner) must be the same on Transfer Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, input);
                tx.output(TitleContract.ID, badLinearId);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), bob.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Transfer());
                return tx.failsWith("All attributes (except new owner) must be the same on Transfer Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, input);
                tx.output(TitleContract.ID, output);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Transfer());
                return tx.failsWith("Old Owner, new Owner, and County must sign on Transfer Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, input);
                tx.output(TitleContract.ID, output);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), bob.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Transfer());
                return tx.verifies();
            });
            return null;
        });
    }
}