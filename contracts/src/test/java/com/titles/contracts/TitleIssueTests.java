package com.titles.contracts;

import com.titles.states.TitleState;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import java.util.Arrays;

import static net.corda.testing.node.NodeTestUtils.ledger;


public class TitleIssueTests {
    private final MockServices ledgerServices = new MockServices(Arrays.asList("com"));
    TestIdentity alice = new TestIdentity(new CordaX500Name("Alice",  "TestLand",  "US"));
    TestIdentity bob = new TestIdentity(new CordaX500Name("Bob",  "TestLand",  "US"));
    TestIdentity county = new TestIdentity(new CordaX500Name("County",  "Brooklyn",  "US"));

    @Test
    public void issueCommandTest() {
        TitleState state = new TitleState(alice.getParty(), county.getParty(), "123 Main St", "123456789");
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(TitleContract.ID, state);
                tx.output(TitleContract.ID, state);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Issue());
                return tx.failsWith("There must be no inputs on Issue Command");
            });
            l.transaction(tx -> {
                tx.output(TitleContract.ID, state);
                tx.output(TitleContract.ID, state);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Issue());
                return tx.failsWith("There must be one output on Issue Command");
            });
            l.transaction(tx -> {
                tx.output(TitleContract.ID, state);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), bob.getPublicKey()),
                        new TitleContract.Commands.Issue());
                return tx.failsWith("Owner and county must sign on Issue Command");
            });
            l.transaction(tx -> {
                tx.output(TitleContract.ID, state);
                tx.command(
                        Arrays.asList(alice.getPublicKey(), county.getPublicKey()),
                        new TitleContract.Commands.Issue());
                return tx.verifies();
            });
            return null;
        });
    }
}