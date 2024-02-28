package com.r3.developers.apples.workflows;

import net.corda.v5.base.types.MemberX500Name;

public class CreateAndIssueAppleStampRequest {

    private String stampDescription;

    private MemberX500Name holder;
    private MemberX500Name notary;

    // The JSON Marshalling Service, which handles serialisation, needs this constructor.
    public CreateAndIssueAppleStampRequest() {}

    public CreateAndIssueAppleStampRequest(String stampDescription, MemberX500Name holder, MemberX500Name notary) {
        this.stampDescription = stampDescription;
        this.holder = holder;
        this.notary = notary;
    }

    public String getStampDescription() {
        return stampDescription;
    }

    public MemberX500Name getHolder() {
        return holder;
    }

    public MemberX500Name getNotary() {
        return notary;
    }
}
