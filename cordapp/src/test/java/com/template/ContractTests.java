package com.template;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static net.corda.testing.CoreTestUtils.setCordappPackages;
import static net.corda.testing.CoreTestUtils.unsetCordappPackages;

public class ContractTests {

    @Before
    public void setup() {
        setCordappPackages("com.template");
    }

    @After
    public void tearDown() {
        unsetCordappPackages();
    }

    @Test
    public void dummyTest() {

    }
}