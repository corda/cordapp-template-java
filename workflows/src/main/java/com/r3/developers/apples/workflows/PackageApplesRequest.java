package com.r3.developers.apples.workflows;

import net.corda.v5.base.types.MemberX500Name;

public class PackageApplesRequest {

    private String appleDescription;
    private MemberX500Name notary;

    private int weight;

    // The JSON Marshalling Service, which handles serialisation, needs this constructor.
    public PackageApplesRequest() {}

    public PackageApplesRequest(String appleDescription, int weight, MemberX500Name notary) {
        this.appleDescription = appleDescription;
        this.weight = weight;
        this.notary = notary;
    }

    public MemberX500Name getNotary() {
        return notary;
    }

    public String getAppleDescription() {
        return appleDescription;
    }

    public int getWeight() {
        return weight;
    }
}
