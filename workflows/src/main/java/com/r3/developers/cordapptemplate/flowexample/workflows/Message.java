package com.r3.developers.cordapptemplate.flowexample.workflows;

import net.corda.v5.base.annotations.CordaSerializable;
import net.corda.v5.base.types.MemberX500Name;

// Where a class contains a message, mark it with @CordaSerializable to enable Corda to 
// send it from one virtual node to another.
@CordaSerializable
public class Message {

    private MemberX500Name sender;
    private String message;

    public Message(MemberX500Name sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public MemberX500Name getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }


}
