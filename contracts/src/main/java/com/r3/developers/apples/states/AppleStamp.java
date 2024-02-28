package com.r3.developers.apples.states;

import com.r3.developers.apples.contracts.AppleStampContract;
import net.corda.v5.base.annotations.ConstructorForDeserialization;
import net.corda.v5.ledger.utxo.BelongsToContract;
import net.corda.v5.ledger.utxo.ContractState;
import java.security.PublicKey;
import java.util.*;

@BelongsToContract(AppleStampContract.class)
public class AppleStamp implements ContractState {
    private UUID id;
    private String stampDesc;
    private PublicKey issuer;
    private PublicKey holder;
    private List<PublicKey> participants;

    @ConstructorForDeserialization
    public AppleStamp(UUID id, String stampDesc, PublicKey issuer, PublicKey holder, List<PublicKey> participants) {
        this.id = id;
        this.stampDesc = stampDesc;
        this.issuer = issuer;
        this.holder = holder;
        this.participants = participants;
    }

    @Override
    public List<PublicKey> getParticipants() {
        return participants;
    }

    public UUID getId() {
        return this.id;
    }

    public String getStampDesc() {
        return this.stampDesc;
    }

    public PublicKey getIssuer() {
        return this.issuer;
    }

    public PublicKey getHolder() {
        return this.holder;
    }
}