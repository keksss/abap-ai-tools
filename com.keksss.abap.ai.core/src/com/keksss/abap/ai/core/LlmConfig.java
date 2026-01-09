package com.keksss.abap.ai.core;

/**
 * Configuration object for LLM clients using Builder pattern
 */
public class LlmConfig {
    private final LlmProvider provider;
    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final double temperature;
    private final int maxTokens;

    private LlmConfig(Builder builder) {
        this.provider = builder.provider;
        this.apiKey = builder.apiKey;
        this.model = builder.model;
        this.baseUrl = builder.baseUrl;
        this.temperature = builder.temperature;
        this.maxTokens = builder.maxTokens;
    }

    public LlmProvider getProvider() {
        return provider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    /**
     * Builder for LlmConfig
     */
    public static class Builder {
        private LlmProvider provider = LlmProvider.GOOGLE_AI;
        private String apiKey = "";
        private String model = "gemini-1.5-flash";
        private String baseUrl = null;
        private double temperature = 0.7;
        private int maxTokens = 2048;

        public Builder provider(LlmProvider provider) {
            this.provider = provider;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public LlmConfig build() {
            return new LlmConfig(this);
        }
    }

    @Override
    public String toString() {
        return "LlmConfig{" +
                "provider=" + provider +
                ", model='" + model + '\'' +
                ", baseUrl='" + baseUrl + '\'' +
                ", temperature=" + temperature +
                ", maxTokens=" + maxTokens +
                '}';
    }
}
