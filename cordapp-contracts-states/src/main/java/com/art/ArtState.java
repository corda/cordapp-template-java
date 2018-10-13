package com.art;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.util.List;

public class ArtState implements ContractState {
    private final Party appraiser;
    private final Party owner;
    private final String artist;
    private final String title;
    private final String type;

    private List<AbstractParty> participants;

    public ArtState(Party appraiser, Party owner, String artist, String title, String type) {
        this.appraiser = appraiser;
        this.owner = owner;
        this.artist = artist;
        this.title = title;
        this.type = type;
        this.participants = ImmutableList.of(this.appraiser, this.owner);
    }

    @Override public List<AbstractParty> getParticipants() {
        return participants;
    }

    public Party getAppraiser() {
        return appraiser;
    }

    public Party getOwner() {
        return owner;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }
}