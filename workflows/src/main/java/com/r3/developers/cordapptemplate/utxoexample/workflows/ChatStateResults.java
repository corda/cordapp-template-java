package com.r3.developers.cordapptemplate.utxoexample.workflows;

import java.util.UUID;

// Class to hold the ListChatFlow results.
// The ChatState(s) cannot be returned directly as the JsonMarshallingService can only serialize simple classes
// that the underlying Jackson serializer recognises, hence creating a DTO style object which consists only of Strings
// and a UUID. It is possible to create custom serializers for the JsonMarshallingService, but this beyond the scope
// of this simple example.
public class ChatStateResults {

    private UUID id;
    private String chatName;
    private String messageFromName;
    private String message;

    public ChatStateResults() {}

    public ChatStateResults(UUID id, String chatName, String messageFromName, String message) {
        this.id = id;
        this.chatName = chatName;
        this.messageFromName = messageFromName;
        this.message = message;
    }

    public UUID getId() {
        return id;
    }

    public String getChatName() {
        return chatName;
    }

    public String getMessageFromName() {
        return messageFromName;
    }

    public String getMessage() {
        return message;
    }
}
