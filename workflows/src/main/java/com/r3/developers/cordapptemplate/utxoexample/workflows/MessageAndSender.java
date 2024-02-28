package com.r3.developers.cordapptemplate.utxoexample.workflows;

// A class to pair the messageFrom and message together.
public class MessageAndSender {

    private String messageFrom;
    private String message;
    public MessageAndSender() {}

    public MessageAndSender(String messageFrom, String message) {
        this.messageFrom = messageFrom;
        this.message = message;
    }

    public String getMessageFrom() {
        return messageFrom;
    }

    public String getMessage() {
        return message;
    }
}
