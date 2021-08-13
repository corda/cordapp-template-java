package com.template.accountUtilities;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.flows.StartableByService;
import net.corda.core.identity.PartyAndCertificate;
import java.util.*;

@StartableByRPC
@StartableByService
public class NewKeyForAccount extends FlowLogic<PartyAndCertificate>{

    private final UUID accountID;

    public NewKeyForAccount(UUID accountID) {
        this.accountID = accountID;
    }

    @Override
    @Suspendable
    public PartyAndCertificate call() throws FlowException {
        return getServiceHub().getKeyManagementService().freshKeyAndCert(getOurIdentityAndCert(), false, accountID);
    }
}