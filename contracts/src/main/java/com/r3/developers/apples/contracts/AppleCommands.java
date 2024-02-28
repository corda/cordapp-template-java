package com.r3.developers.apples.contracts;

import net.corda.v5.ledger.utxo.Command;

public interface AppleCommands extends Command {
    public class Issue implements AppleCommands { };
    public class Redeem implements AppleCommands { };
    public class PackBasket implements AppleCommands { };
}