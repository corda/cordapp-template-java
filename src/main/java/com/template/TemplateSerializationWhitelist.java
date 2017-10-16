package com.template;

import net.corda.core.serialization.SerializationWhitelist;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

// Serialization whitelist.
public class TemplateSerializationWhitelist implements SerializationWhitelist {
    @NotNull
    @Override
    public List<Class<?>> getWhitelist() {
        return Collections.singletonList(TemplateData.class);
    }

    // This class is not annotated with @CordaSerializable, so it must be added to the serialization whitelist, above,
    // if we want to send it to other nodes within a flow.
    public static class TemplateData {
        private final String payload;

        public TemplateData(String payload) {
            this.payload = payload;
        }

        public String getPayload() { return payload; }
    }
}