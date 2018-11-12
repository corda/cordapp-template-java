package com.template;

import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;

import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
public class TemplateState implements ContractState {

    public TemplateState() {

    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList();
    }
}