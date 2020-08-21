package com.template.states;

import com.template.contracts.TemplateContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(TemplateContract.class)
public class TemplateState implements ContractState {

    //private variables
    private String msg;
    private Party sender;
    private Party receiver;

    /* Constructor of your Corda state */
    public TemplateState(String msg, Party sender, Party receiver) {
        this.msg = msg;
        this.sender = sender;
        this.receiver = receiver;
    }

    //getters
    public String getMsg() { return msg; }
    public Party getSender() { return sender; }
    public Party getReceiver() { return receiver; }

    /* This method will indicate who are the participants and required signers when
     * this state is used in a transaction. */
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(sender,receiver);
    }
}