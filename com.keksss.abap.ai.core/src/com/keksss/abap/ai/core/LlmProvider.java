package com.keksss.abap.ai.core;

/**
 * Enumeration of supported LLM providers
 */
public enum LlmProvider {
    GOOGLE_AI("Google AI (Gemini)", true, false),
    OPENAI("OpenAI", true, false),
    ANTHROPIC("Anthropic (Claude)", true, false),
    OLLAMA("Ollama (Local)", false, true);

    private final String displayName;
    private final boolean requiresApiKey;
    private final boolean supportsBaseUrl;

    LlmProvider(String displayName, boolean requiresApiKey, boolean supportsBaseUrl) {
        this.displayName = displayName;
        this.requiresApiKey = requiresApiKey;
        this.supportsBaseUrl = supportsBaseUrl;
    }

    /**
     * Get the display name for UI
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if this provider requires an API key
     */
    public boolean requiresApiKey() {
        return requiresApiKey;
    }

    /**
     * Check if this provider supports custom base URL
     */
    public boolean supportsBaseUrl() {
        return supportsBaseUrl;
    }

    /**
     * Convert string to LlmProvider enum
     * 
     * @param value string representation of provider
     * @return LlmProvider enum or GOOGLE_AI as default
     */
    public static LlmProvider fromString(String value) {
        if (value == null || value.isEmpty()) {
            return GOOGLE_AI;
        }
        for (LlmProvider provider : values()) {
            if (provider.name().equals(value)) {
                return provider;
            }
        }
        return GOOGLE_AI; // Default
    }
}
