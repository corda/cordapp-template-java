package com.template.states;

import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType;
import com.template.ExampleEvolvableTokenTypeContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;

import java.util.List;

@BelongsToContract(ExampleEvolvableTokenTypeContract.class)
public class ExampleEvolvableTokenType extends EvolvableTokenType {

    private final String importantInformationThatMayChange;
    private final Party maintainer;
    private final UniqueIdentifier uniqueIdentifier;
    private final int fractionDigits;

    public ExampleEvolvableTokenType(String importantInformationThatMayChange, Party maintainer,
                                     UniqueIdentifier uniqueIdentifier, int fractionDigits) {
        this.importantInformationThatMayChange = importantInformationThatMayChange;
        this.maintainer = maintainer;
        this.uniqueIdentifier = uniqueIdentifier;
        this.fractionDigits = fractionDigits;
    }

    @Override
    public List<Party> getMaintainers() {
        return ImmutableList.of(maintainer);
    }

    @Override
    public int getFractionDigits() {
        return this.fractionDigits;
    }

    @Override
    public UniqueIdentifier getLinearId() {
        return this.uniqueIdentifier;
    }
}
