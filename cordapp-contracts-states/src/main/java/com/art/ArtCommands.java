package com.art;

import net.corda.core.contracts.CommandData;

public interface ArtCommands {
    class Issue implements CommandData {}
    class Transfer implements CommandData {}
}
