package com.r3.developers.apples.workflows;

import com.r3.developers.apples.contracts.AppleCommands;
import com.r3.developers.apples.states.AppleStamp;
import net.corda.v5.application.flows.ClientRequestBody;
import net.corda.v5.application.flows.ClientStartableFlow;
import net.corda.v5.application.flows.CordaInject;
import net.corda.v5.application.flows.InitiatingFlow;
import net.corda.v5.application.marshalling.JsonMarshallingService;
import net.corda.v5.application.membership.MemberLookup;
import net.corda.v5.application.messaging.FlowMessaging;
import net.corda.v5.application.messaging.FlowSession;
import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.base.types.MemberX500Name;
import net.corda.v5.ledger.common.NotaryLookup;
import net.corda.v5.ledger.utxo.UtxoLedgerService;
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction;
import net.corda.v5.membership.MemberInfo;
import net.corda.v5.membership.NotaryInfo;
import org.jetbrains.annotations.NotNull;
import java.security.PublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@InitiatingFlow(protocol = "create-and-issue-apple-stamp")
public class CreateAndIssueAppleStampFlow implements ClientStartableFlow {

    @CordaInject
    public FlowMessaging flowMessaging;

    @CordaInject
    public JsonMarshallingService jsonMarshallingService;

    @CordaInject
    public MemberLookup memberLookup;

    @CordaInject
    NotaryLookup notaryLookup;

    @CordaInject
    UtxoLedgerService utxoLedgerService;

    public CreateAndIssueAppleStampFlow() {}

    @Suspendable
    @Override
    @NotNull
    public String call(@NotNull ClientRequestBody requestBody) {

        CreateAndIssueAppleStampRequest request = requestBody.getRequestBodyAs(jsonMarshallingService, CreateAndIssueAppleStampRequest.class);
        String stampDescription = request.getStampDescription();
        MemberX500Name holderName = request.getHolder();

        final NotaryInfo notaryInfo = notaryLookup.lookup(request.getNotary());
        if (notaryInfo == null) {
            throw new IllegalArgumentException("Notary " + request.getNotary() + " not found");
        }

        PublicKey issuer = memberLookup.myInfo().getLedgerKeys().get(0);

        final MemberInfo holderInfo = memberLookup.lookup(holderName);
        if (holderInfo == null) {
            throw new IllegalArgumentException(String.format("The holder %s does not exist within the network", holderName));
        }

        final PublicKey holder;
        try {
            holder = holderInfo.getLedgerKeys().get(0);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("The holder %s has no ledger key", holderName));
        }

        AppleStamp newStamp = new AppleStamp(
                UUID.randomUUID(),
                stampDescription,
                issuer,
                holder,
                List.of(issuer, holder)
        );

        UtxoSignedTransaction transaction = utxoLedgerService.createTransactionBuilder()
                .setNotary(notaryInfo.getName())
                .addOutputState(newStamp)
                .addCommand(new AppleCommands.Issue())
                .setTimeWindowUntil(Instant.now().plus(1, ChronoUnit.DAYS))
                .addSignatories(List.of(issuer, holder))
                .toSignedTransaction();

        FlowSession session = flowMessaging.initiateFlow(holderName);

        try {
            // Send the transaction and state to the counterparty and let them sign it
            // Then notarise and record the transaction in both parties' vaults.
            utxoLedgerService.finalize(transaction, List.of(session));
            return newStamp.getId().toString();
        } catch (Exception e) {
            return String.format("Flow failed, message: %s", e.getMessage());
        }
    }
}
