package com.template.plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.template.api.TemplateApi;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.CordaPluginRegistry;
import net.corda.core.serialization.SerializationCustomization;
import net.corda.webserver.services.WebServerPluginRegistry;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TemplatePlugin extends CordaPluginRegistry {
    /**
     * Whitelisting the required types for serialisation by the Corda node.
     */
    @Override
    public boolean customizeSerialization(SerializationCustomization custom) {
        return true;
    }
}