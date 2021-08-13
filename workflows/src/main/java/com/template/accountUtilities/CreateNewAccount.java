package com.template.accountUtilities;

import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.*;
import com.r3.corda.lib.accounts.workflows.services.KeyManagementBackedAccountService;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.workflows.services.AccountService;
import net.corda.core.flows.FlowLogic;;
import net.corda.core.flows.StartableByRPC;

import java.util.UUID;

@StartableByRPC
@StartableByService
@InitiatingFlow
public class CreateNewAccount extends FlowLogic<String>{

    private String acctName;

    public CreateNewAccount(String acctName) {
        this.acctName = acctName;
    }


    @Override
    public String call() throws FlowException {
        StateAndRef<AccountInfo> newAccount = null;
        try {
            newAccount = getServiceHub().cordaService(KeyManagementBackedAccountService.class).createAccount(acctName).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        AccountInfo acct = newAccount.getState().getData();
        return "" + acct.getName() + " team's account was created. UUID is : " + acct.getIdentifier();
    }
}
