package com.r3.developers.cordapptemplate.utxoexample.workflows;

// A class to hold the deserialized arguments required to start the flow.
public class CreateNewChatFlowArgs{

    // Serialisation service requires a default constructor
    public CreateNewChatFlowArgs() {}

    private String chatName;
    private String message;
    private String otherMember;

    public CreateNewChatFlowArgs(String chatName, String message, String otherMember) {
        this.chatName = chatName;
        this.message = message;
        this.otherMember = otherMember;
    }

    public String getChatName() {
        return chatName;
    }

    public String getMessage() {
        return message;
    }

    public String getOtherMember() {
        return otherMember;
    }
}