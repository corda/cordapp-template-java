package com.titles.states;

import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertTrue;

public class TitleStateTests {

    //Mock State test check for if the state has correct parameters type
    @Test
    public void hasFieldsOfCorrectType() throws NoSuchFieldException {
        assert (TitleState.class.getDeclaredField("owner").getType().equals(Party.class));
        assert (TitleState.class.getDeclaredField("county").getType().equals(Party.class));
        assert (TitleState.class.getDeclaredField("address").getType().equals(String.class));
        assert (TitleState.class.getDeclaredField("parcelId").getType().equals(String.class));
    }
    @Test
    public void isLinearState() throws NoSuchFieldException{
        assert(LinearState.class.isAssignableFrom(TitleState.class));
        Field linearIdField = TitleState.class.getDeclaredField("linearId");
        assertTrue(linearIdField.getType().isAssignableFrom(UniqueIdentifier.class));
    }
}