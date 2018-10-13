package com.art;

import net.corda.core.contracts.CommandData;

public interface ArtCommands extends CommandData {
    class Issue implements ArtCommands {}
    class Transfer implements ArtCommands {}
}
