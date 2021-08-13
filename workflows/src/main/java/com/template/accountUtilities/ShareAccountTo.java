package com.template.accountUtilities;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.accounts.contracts.states.AccountInfo;
import com.r3.corda.lib.accounts.workflows.flows.AccountInfoByName;
import com.r3.corda.lib.accounts.workflows.services.KeyManagementBackedAccountService;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.flows.StartableByService;
import net.corda.core.identity.Party;
import com.r3.corda.lib.accounts.workflows.flows.ShareAccountInfo;
import java.util.*;

@StartableByRPC
@StartableByService
public class ShareAccountTo extends FlowLogic<String>{

    private final Party shareTo;
    private final String acctNameShared;

    public ShareAccountTo(String acctNameShared, Party shareTo) {
        this.acctNameShared = acctNameShared;
        this.shareTo = shareTo;
    }

    @Override
    @Suspendable
    public String call() throws FlowException {
        List<StateAndRef<AccountInfo>> allmyAccounts = getServiceHub().cordaService(KeyManagementBackedAccountService.class).ourAccounts();
        StateAndRef<AccountInfo> SharedAccount = allmyAccounts.stream()
                .filter(it -> it.getState().getData().getName().equals(acctNameShared))
                .findAny().get();

        subFlow(new ShareAccountInfo(SharedAccount, Arrays.asList(shareTo)));
        return "Shared " + acctNameShared + " with " + shareTo.getName().getOrganisation();
    }
}