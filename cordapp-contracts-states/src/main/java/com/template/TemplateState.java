package com.template;

import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;

import java.util.Collections;
import java.util.List;

/**
 * Define your state object here.
 */
public class TemplateState implements ContractState {
    private List<AbstractParty> participants;

    public TemplateState(List<AbstractParty> participants) {
        this.participants = participants;
    }

    /** The public keys of the involved parties. */
    @Override public List<AbstractParty> getParticipants() {
        return participants;
    }
}