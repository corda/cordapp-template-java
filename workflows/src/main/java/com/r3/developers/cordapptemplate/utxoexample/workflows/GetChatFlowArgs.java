package com.r3.developers.cordapptemplate.utxoexample.workflows;

import java.util.UUID;

// A class to hold the deserialized arguments required to start the flow.
public class GetChatFlowArgs {

    private UUID id;
    private int numberOfRecords;
    public GetChatFlowArgs() {}

    public GetChatFlowArgs(UUID id, int numberOfRecords ) {
        this.id = id;
        this.numberOfRecords = numberOfRecords;
    }

    public UUID getId() {
        return id;
    }

    public int getNumberOfRecords() {
        return numberOfRecords;
    }
}
