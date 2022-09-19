package com.titles.states;

import com.titles.contracts.TitleContract;
import net.corda.core.contracts.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;

import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

// *********
// * State *
// *********
@BelongsToContract(TitleContract.class)
public class TitleState implements LinearState {
    private Party owner;
    private Party county;
    private String address;
    private String parcelId;
    private final UniqueIdentifier linearId;

    /* Constructor of your Corda state */
    public TitleState(Party owner, Party county, String address, String parcelId) {
        this(owner, county, address, parcelId, new UniqueIdentifier());
    }
    @ConstructorForDeserialization
    public TitleState(Party owner, Party county, String address, String parcelId, UniqueIdentifier linearId) {
        this.owner = owner;
        this.county = county;
        this.address = address;
        this.parcelId = parcelId;
        this.linearId = linearId;
    }
    //getters
    public Party getOwner() { return owner; }
    public Party getCounty() { return county; }
    public String getAddress() { return address; }
    public String getParcelId() { return parcelId; }
//    public String getMsg() { return msg; }
//    public Party getSender() { return sender; }
//    public Party getReceiver() { return receiver; }

    /* This method will indicate who are the participants and required signers when
     * this state is used in a transaction. */
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(owner, county);
    }
    @Override
    public UniqueIdentifier getLinearId() { return linearId;}
    public TitleState withNewOwner(Party newOwner) {
        return new TitleState(newOwner, county, address, parcelId, linearId);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TitleState titleState = (TitleState) o;
        return Objects.equals(owner, titleState.owner) &&
                Objects.equals(county, titleState.county) &&
                Objects.equals(address, titleState.address) &&
                Objects.equals(parcelId, titleState.parcelId) &&
                Objects.equals(linearId, titleState.linearId);
    }
}