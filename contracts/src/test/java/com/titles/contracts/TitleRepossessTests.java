package com.titles.contracts;

import com.titles.states.TitleState;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import java.util.Arrays;

import static net.corda.testing.node.NodeTestUtils.ledger;


public class TitleRepossessTests {
    private final MockServices ledgerServices = new MockServices(Arrays.asList("com"));
    TestIdentity alice = new TestIdentity(new CordaX500Name("Alice",  "TestLand",  "US"));
    TestIdentity bob = new TestIdentity(new CordaX500Name("Bob",  "TestLand",  "US"));
    TestIdentity county = new TestIdentity(new CordaX500Name("County",  "Brooklyn",  "US"));

    @Test
    public void repossessCommandTest() {
        TitleState input = new TitleState(alice.getParty(), county.getParty(), "123 Main St", "123456789");
        TitleState inputWithCountyAlready = new TitleState(county.getParty(), county.getParty(), "123 Main St", "123456789");
        TitleState outputBadOwner = new TitleState(bob.getParty(), county.getParty(), "123 Main St", "123456789");
        TitleState output = input.withNewOwner(county.getParty());
        TitleState outputChangedAddress = new TitleState(county.getParty(), county.getParty(), "123 Yellow Brick Rd", "123456789");
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(TitleContract.ID, output);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Repossess());
                return tx.failsWith("There must be one input on Repossess Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, input);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Repossess());
                return tx.failsWith("There must be one output on Repossess Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, inputWithCountyAlready);
                tx.output(TitleContract.ID, output);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Repossess());
                return tx.failsWith("Owner is already County, nothing to do on Repossess Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, input);
                tx.output(TitleContract.ID, outputBadOwner);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Repossess());
                return tx.failsWith("New owner must be County on Repossess Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, input);
                tx.output(TitleContract.ID, outputChangedAddress);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Repossess());
                return tx.failsWith("All attributes (except new owner as county) must be the same on Repossess Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, input);
                tx.output(TitleContract.ID, output);
                tx.command(
                        Arrays.asList(alice.getPublicKey()),
                        new TitleContract.Commands.Repossess());
                return tx.failsWith("County can only sign Repossess Command.");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, input);
                tx.output(TitleContract.ID, output);
                tx.command(
                        Arrays.asList(county.getPublicKey()),
                        new TitleContract.Commands.Repossess());
                return tx.verifies();
            });
            return null;
        });
    }
}