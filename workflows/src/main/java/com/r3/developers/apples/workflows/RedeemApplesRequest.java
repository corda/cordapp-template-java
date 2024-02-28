package com.r3.developers.apples.workflows;

import net.corda.v5.base.types.MemberX500Name;

import java.util.UUID;

public class RedeemApplesRequest {

    private MemberX500Name buyer;
    private MemberX500Name notary;

    private UUID stampId;

    // The JSON Marshalling Service, which handles serialisation, needs this constructor.
    public RedeemApplesRequest() {}

    public RedeemApplesRequest(MemberX500Name buyer, MemberX500Name notary, UUID stampId) {
        this.buyer = buyer;
        this.notary = notary;
        this.stampId = stampId;
    }

    public MemberX500Name getNotary() {
        return notary;
    }

    public MemberX500Name getBuyer() {
        return buyer;
    }

    public UUID getStampId() {
        return stampId;
    }
}
