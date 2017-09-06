package com.template.plugin;

import net.corda.core.node.CordaPluginRegistry;
import net.corda.core.serialization.SerializationCustomization;

public class TemplatePlugin extends CordaPluginRegistry {
    /**
     * Whitelisting the required types for serialisation by the Corda node.
     */
    @Override
    public boolean customizeSerialization(SerializationCustomization custom) {
        return true;
    }
}