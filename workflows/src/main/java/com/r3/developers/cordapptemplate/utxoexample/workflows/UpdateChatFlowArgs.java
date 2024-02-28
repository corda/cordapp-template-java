package com.r3.developers.cordapptemplate.utxoexample.workflows;

import java.util.UUID;

// A class to hold the deserialized arguments required to start the flow.
public class UpdateChatFlowArgs {
    public UpdateChatFlowArgs() {}

    private UUID id;
    private String message;

    public UpdateChatFlowArgs(UUID id, String message) {
        this.id = id;
        this.message = message;
    }

    public UUID getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }
}
