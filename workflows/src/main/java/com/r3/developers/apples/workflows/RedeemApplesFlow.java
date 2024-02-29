package com.r3.developers.apples.workflows;

import com.r3.developers.apples.contracts.AppleCommands;
import com.r3.developers.apples.states.AppleStamp;
import com.r3.developers.apples.states.BasketOfApples;
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
import net.corda.v5.ledger.utxo.StateAndRef;
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

@InitiatingFlow(protocol = "redeem-apples")
public class RedeemApplesFlow implements ClientStartableFlow {

    @CordaInject
    FlowMessaging flowMessaging;

    @CordaInject
    JsonMarshallingService jsonMarshallingService;

    @CordaInject
    MemberLookup memberLookup;

    @CordaInject
    NotaryLookup notaryLookup;

    @CordaInject
    UtxoLedgerService utxoLedgerService;

    public RedeemApplesFlow() {}

    @Suspendable
    @Override
    @NotNull
    public String call(@NotNull ClientRequestBody requestBody) {

        RedeemApplesRequest request = requestBody.getRequestBodyAs(jsonMarshallingService, RedeemApplesRequest.class);
        MemberX500Name buyerName = request.getBuyer();
        UUID stampId = request.getStampId();

        // Retrieve the notaries public key (this will change)
        final NotaryInfo notaryInfo = notaryLookup.lookup(request.getNotary());
        if (notaryInfo == null) {
            throw new IllegalArgumentException("Notary " + request.getNotary() + " not found");
        }

        PublicKey myKey = memberLookup.myInfo().getLedgerKeys().get(0);

        final MemberInfo buyerInfo = memberLookup.lookup(buyerName);
        if (buyerInfo == null) {
            throw new IllegalArgumentException("The buyer does not exist within the network");
        }

        final PublicKey buyer;
        try {
            buyer = buyerInfo.getLedgerKeys().get(0);
        } catch (Exception e) {
            throw new IllegalArgumentException("Buyer " + buyerName + " has no ledger key");
        }

        StateAndRef<AppleStamp> appleStampStateAndRef;
        try {
            appleStampStateAndRef = utxoLedgerService
                    .findUnconsumedStatesByExactType(AppleStamp.class, 100, Instant.now()).getResults()
                    .stream()
                    .filter(stateAndRef -> stateAndRef.getState().getContractState().getId().equals(stampId))
                    .iterator()
                    .next();
        } catch (Exception e) {
            throw new IllegalArgumentException("There are no eligible basket of apples");
        }

        StateAndRef<BasketOfApples> basketOfApplesStampStateAndRef;
        try {
            basketOfApplesStampStateAndRef = utxoLedgerService
                    .findUnconsumedStatesByExactType(BasketOfApples.class, 100, Instant.now()).getResults()
                    .stream()
                    .filter(
                            stateAndRef -> stateAndRef.getState().getContractState().getOwner().equals(
                                    appleStampStateAndRef.getState().getContractState().getIssuer()
                            )
                    )
                    .iterator()
                    .next();
        } catch (Exception e) {
            throw new IllegalArgumentException("There are no eligible baskets of apples");
        }

        BasketOfApples originalBasketOfApples = basketOfApplesStampStateAndRef.getState().getContractState();

        BasketOfApples updatedBasket = originalBasketOfApples.changeOwner(buyer);

        //Create the transaction
        UtxoSignedTransaction transaction = utxoLedgerService.createTransactionBuilder()
                .setNotary(notaryInfo.getName())
                .addInputStates(appleStampStateAndRef.getRef(), basketOfApplesStampStateAndRef.getRef())
                .addOutputState(updatedBasket)
                .addCommand(new AppleCommands.Redeem())
                .setTimeWindowUntil(Instant.now().plus(1, ChronoUnit.DAYS))
                .addSignatories(List.of(myKey, buyer))
                .toSignedTransaction();

        FlowSession session = flowMessaging.initiateFlow(buyerName);

        try {
            // Send the transaction and state to the counterparty and let them sign it
            // Then notarise and record the transaction in both parties' vaults.
            return utxoLedgerService.finalize(transaction, List.of(session)).getTransaction().getId().toString();
        } catch (Exception e) {
            return String.format("Flow failed, message: %s", e.getMessage());
        }
    }
}
