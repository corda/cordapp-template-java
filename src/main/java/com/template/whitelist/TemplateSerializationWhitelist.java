package com.template.whitelist;

import net.corda.core.serialization.SerializationWhitelist;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

// Serialization whitelist (only needed for 3rd party classes, but we use a local example here).
public class TemplateSerializationWhitelist implements SerializationWhitelist {
    @NotNull
    @Override
    public List<Class<?>> getWhitelist() {
        return Collections.singletonList(TemplateData.class);
    }

    // Not annotated with @CordaSerializable just for use with manual whitelisting above.
    public static class TemplateData {
        private final String payload;

        public TemplateData(String payload) {
            this.payload = payload;
        }

        public String getPayload() { return payload; }
    }
}
