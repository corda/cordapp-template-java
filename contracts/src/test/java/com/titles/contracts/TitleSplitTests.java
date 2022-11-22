package com.titles.contracts;

import com.titles.states.TitleState;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import java.util.Arrays;

import static net.corda.testing.node.NodeTestUtils.ledger;


public class TitleSplitTests {
    private final MockServices ledgerServices = new MockServices(Arrays.asList("com"));
    TestIdentity alice = new TestIdentity(new CordaX500Name("Alice",  "TestLand",  "US"));
    TestIdentity bob = new TestIdentity(new CordaX500Name("Bob",  "TestLand",  "US"));
    TestIdentity county = new TestIdentity(new CordaX500Name("County",  "Brooklyn",  "US"));
    TestIdentity countyB = new TestIdentity(new CordaX500Name("CountyB",  "Brooklyn",  "US"));

    @Test
    public void splitCommandTest() {
        TitleState input = new TitleState(alice.getParty(), county.getParty(), "123 Main St", "123456789");
        TitleState outputA = new TitleState(alice.getParty(), county.getParty(), "123 Main St A", "123456788");
        TitleState outputB = new TitleState(alice.getParty(), county.getParty(), "123 Main St B", "123456789");
        TitleState outputDifferentOwner = new TitleState(bob.getParty(), county.getParty(), "123 Main St B", "123456787");
        TitleState inputDifferentOwner = new TitleState(bob.getParty(), county.getParty(), "123 Main St", "123456789");
        TitleState inputDifferentCounty = new TitleState(alice.getParty(), countyB.getParty(), "123 Main St", "123456789");
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(TitleContract.ID, outputA);
                tx.output(TitleContract.ID, outputB);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Split());
                return tx.failsWith("There must be one input on Split Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, input);
                tx.output(TitleContract.ID, outputB);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Split());
                return tx.failsWith("There must be two or more outputs on Split Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, input);
                tx.output(TitleContract.ID, outputA);
                tx.output(TitleContract.ID, outputB);
                tx.output(TitleContract.ID, outputB);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Split());
                return tx.failsWith("Parcel IDs must be unique for all outputs on Split Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, input);
                tx.output(TitleContract.ID, outputA);
                tx.output(TitleContract.ID, outputDifferentOwner);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Split());
                return tx.failsWith("All output states must have same owner on Split Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, inputDifferentOwner);
                tx.output(TitleContract.ID, outputA);
                tx.output(TitleContract.ID, outputB);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Split());
                return tx.failsWith("Owner must remain the same on Split Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, inputDifferentCounty);
                tx.output(TitleContract.ID, outputA);
                tx.output(TitleContract.ID, outputB);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Split());
                return tx.failsWith("County must remain the same on Split Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, input);
                tx.output(TitleContract.ID, outputA);
                tx.output(TitleContract.ID, outputB);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), bob.getPublicKey()),
                        new TitleContract.Commands.Split());
                return tx.failsWith("Owner and County must sign Split Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, input);
                tx.output(TitleContract.ID, outputA);
                tx.output(TitleContract.ID, outputB);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Split());
                return tx.verifies();
            });
            return null;
        });
    }
}