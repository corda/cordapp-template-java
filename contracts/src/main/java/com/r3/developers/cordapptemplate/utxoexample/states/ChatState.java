package com.r3.developers.cordapptemplate.utxoexample.states;

import com.r3.developers.cordapptemplate.utxoexample.contracts.ChatContract;
import net.corda.v5.base.annotations.ConstructorForDeserialization;
import net.corda.v5.base.types.MemberX500Name;
import net.corda.v5.ledger.utxo.BelongsToContract;
import net.corda.v5.ledger.utxo.ContractState;

import java.security.PublicKey;
import java.util.*;

@BelongsToContract(ChatContract.class)
public class ChatState implements ContractState {

    private UUID id;
    private String chatName;
    private MemberX500Name messageFrom;
    private String message;
    public List<PublicKey> participants;

    // Allows serialisation and to use a specified UUID.
    @ConstructorForDeserialization
    public ChatState(UUID id,
                     String chatName,
                     MemberX500Name messageFrom,
                     String message,
                     List<PublicKey> participants) {
        this.id = id;
        this.chatName = chatName;
        this.messageFrom = messageFrom;
        this.message = message;
        this.participants = participants;
    }

    public UUID getId() {
        return id;
    }
    public String getChatName() {
        return chatName;
    }
    public MemberX500Name getMessageFrom() {
        return messageFrom;
    }
    public String getMessage() {
        return message;
    }

    public List<PublicKey> getParticipants() {
        return participants;
    }

    public ChatState updateMessage(MemberX500Name name, String message) {
        return new ChatState(id, chatName, name, message, participants);
    }
}