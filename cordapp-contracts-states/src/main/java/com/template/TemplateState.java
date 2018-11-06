package com.template;

import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;

import java.util.List;

// *********
// * State *
// *********
public class TemplateState implements ContractState {
    private List<AbstractParty> participants;

    public TemplateState(List<AbstractParty> participants) {
        this.participants = participants;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return participants;
    }
}