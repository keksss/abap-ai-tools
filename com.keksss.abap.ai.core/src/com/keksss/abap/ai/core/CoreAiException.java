package com.keksss.abap.ai.core;

/**
 * Unified exception wrapper for all LLM provider-specific exceptions.
 * This ensures client code doesn't need to handle provider-specific exceptions.
 */
public class CoreAiException extends Exception {
    private static final long serialVersionUID = 1L;

    private final LlmProvider provider;

    /**
     * Create exception with provider and message
     * 
     * @param provider the LLM provider that caused the exception
     * @param message  error message
     */
    public CoreAiException(LlmProvider provider, String message) {
        super(message);
        this.provider = provider;
    }

    /**
     * Create exception with provider, message, and cause
     * 
     * @param provider the LLM provider that caused the exception
     * @param message  error message
     * @param cause    underlying exception
     */
    public CoreAiException(LlmProvider provider, String message, Throwable cause) {
        super(message, cause);
        this.provider = provider;
    }

    /**
     * Get the provider that caused this exception
     * 
     * @return LlmProvider
     */
    public LlmProvider getProvider() {
        return provider;
    }

    @Override
    public String getMessage() {
        return String.format("[%s] %s", provider.getDisplayName(), super.getMessage());
    }
}
