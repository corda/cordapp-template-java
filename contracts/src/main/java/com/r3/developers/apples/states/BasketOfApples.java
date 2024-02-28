package com.r3.developers.apples.states;

import com.r3.developers.apples.contracts.BasketOfApplesContract;
import net.corda.v5.base.annotations.ConstructorForDeserialization;
import net.corda.v5.ledger.utxo.BelongsToContract;
import net.corda.v5.ledger.utxo.ContractState;
import java.security.PublicKey;
import java.util.List;

@BelongsToContract(BasketOfApplesContract.class)
public class BasketOfApples implements ContractState {
    private String description;
    private PublicKey farm;
    private PublicKey owner;
    private int weight;
    private List<PublicKey> participants;

    @ConstructorForDeserialization
    public BasketOfApples(
            String description, PublicKey farm, PublicKey owner, int weight, List<PublicKey> participants
    ) {
        this.description = description;
        this.farm = farm;
        this.owner = owner;
        this.weight = weight;
        this.participants = participants;
    }

    public List<PublicKey> getParticipants() {
        return participants;
    }

    public String getDescription() {
        return this.description;
    }

    public PublicKey getFarm() {
        return this.farm;
    }

    public PublicKey getOwner() {
        return this.owner;
    }

    public int getWeight() {
        return this.weight;
    }

    public BasketOfApples changeOwner(PublicKey buyer) {
        List<PublicKey> participants = List.of(farm, buyer);
        return new BasketOfApples(this.description, this.farm, buyer, this.weight, participants);
    }
}