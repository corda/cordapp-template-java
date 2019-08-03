package com.template.states;

import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType;
import com.template.ExampleEvolvableTokenTypeContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;

import java.util.List;
import java.util.Objects;

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


    public String getImportantInformationThatMayChange() {
        return importantInformationThatMayChange;
    }

    public Party getMaintainer() {
        return maintainer;
    }

    public UniqueIdentifier getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExampleEvolvableTokenType that = (ExampleEvolvableTokenType) o;
        return getFractionDigits() == that.getFractionDigits() &&
                getImportantInformationThatMayChange().equals(that.getImportantInformationThatMayChange()) &&
                getMaintainer().equals(that.getMaintainer()) &&
                getUniqueIdentifier().equals(that.getUniqueIdentifier());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getImportantInformationThatMayChange(), getMaintainer(), getUniqueIdentifier(), getFractionDigits());
    }
}
