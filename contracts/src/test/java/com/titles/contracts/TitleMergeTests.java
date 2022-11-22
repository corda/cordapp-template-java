package com.titles.contracts;

import com.titles.states.TitleState;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import java.util.Arrays;

import static net.corda.testing.node.NodeTestUtils.ledger;


public class TitleMergeTests {
    private final MockServices ledgerServices = new MockServices(Arrays.asList("com"));
    TestIdentity alice = new TestIdentity(new CordaX500Name("Alice",  "TestLand",  "US"));
    TestIdentity bob = new TestIdentity(new CordaX500Name("Bob",  "TestLand",  "US"));
    TestIdentity county = new TestIdentity(new CordaX500Name("County",  "Brooklyn",  "US"));
    TestIdentity countyB = new TestIdentity(new CordaX500Name("CountyB",  "Brooklyn",  "US"));

    @Test
    public void mergeCommandTest() {
        TitleState inputA = new TitleState(alice.getParty(), county.getParty(), "123 Main St", "123456788");
        TitleState inputB = new TitleState(alice.getParty(), county.getParty(), "123 Main St", "123456789");
        TitleState inputDifferentOwner = new TitleState(bob.getParty(), county.getParty(), "123 Main St", "123456789");
        TitleState inputDifferentCounty = new TitleState(alice.getParty(), countyB.getParty(), "123 Main St", "123456789");
        TitleState output = new TitleState(alice.getParty(), county.getParty(), "123 Main St", "123456789");
        TitleState outputDifferentOwner = new TitleState(bob.getParty(), county.getParty(), "123 Main St", "123456789");
        TitleState outputDifferentCounty = new TitleState(alice.getParty(), countyB.getParty(), "123 Main St", "123456789");
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(TitleContract.ID, inputA);
                tx.output(TitleContract.ID, output);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Merge());
                return tx.failsWith("There must be two or more inputs on Merge Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, inputA);
                tx.input(TitleContract.ID, inputB);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Merge());
                return tx.failsWith("There must be one output state on Merge Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, inputA);
                tx.input(TitleContract.ID, inputA);
                tx.output(TitleContract.ID, output);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Merge());
                return tx.failsWith("Parcel IDs must be unique for all inputs on Merge Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, inputA);
                tx.input(TitleContract.ID, inputDifferentOwner);
                tx.output(TitleContract.ID, output);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Merge());
                return tx.failsWith("All input states must have same Owner on Merge Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, inputA);
                tx.input(TitleContract.ID, inputDifferentCounty);
                tx.output(TitleContract.ID, output);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Merge());
                return tx.failsWith("All input states must have same County on Merge Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, inputA);
                tx.input(TitleContract.ID, inputB);
                tx.output(TitleContract.ID, outputDifferentOwner);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Merge());
                return tx.failsWith("Owner must remain the same on Merge Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, inputA);
                tx.input(TitleContract.ID, inputB);
                tx.output(TitleContract.ID, outputDifferentCounty);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Merge());
                return tx.failsWith("County must remain the same on Merge Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, inputA);
                tx.input(TitleContract.ID, inputB);
                tx.output(TitleContract.ID, output);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), bob.getPublicKey()),
                        new TitleContract.Commands.Merge());
                return tx.failsWith("Owner and County must sign Merge Command");
            });
            l.transaction(tx -> {
                tx.input(TitleContract.ID, inputA);
                tx.input(TitleContract.ID, inputB);
                tx.output(TitleContract.ID, output);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Merge());
                return tx.verifies();
            });
            return null;
        });
    }
}