package com.keksss.abap.ai.core;

import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

/**
 * Factory for creating ChatLanguageModel instances based on provider
 * configuration.
 * Implements the Factory Pattern to decouple client code from specific
 * implementations.
 */
public class LlmClientFactory {

    /**
     * Creates a ChatLanguageModel based on the provided configuration
     * 
     * @param config LLM configuration
     * @return ChatLanguageModel interface implementation
     * @throws CoreAiException if model creation fails
     */
    public static ChatLanguageModel createChatModel(LlmConfig config) throws CoreAiException {
        try {
            switch (config.getProvider()) {
                case GOOGLE_AI:
                    return createGoogleAiModel(config);
                case OPENAI:
                    return createOpenAiModel(config);
                case ANTHROPIC:
                    return createAnthropicModel(config);
                case OLLAMA:
                    return createOllamaModel(config);
                default:
                    throw new CoreAiException(config.getProvider(),
                            "Unsupported provider: " + config.getProvider());
            }
        } catch (CoreAiException e) {
            throw e;
        } catch (Exception e) {
            throw new CoreAiException(config.getProvider(),
                    "Failed to create LLM client: " + e.getMessage(), e);
        }
    }

    /**
     * Creates Google AI Gemini chat model
     */
    private static ChatLanguageModel createGoogleAiModel(LlmConfig config) throws CoreAiException {
        try {
            return GoogleAiGeminiChatModel.builder()
                    .apiKey(config.getApiKey())
                    .modelName(config.getModel())
                    .temperature(config.getTemperature())
                    .topP(0.95)
                    .topK(40)
                    .maxOutputTokens(config.getMaxTokens())
                    .build();
        } catch (Exception e) {
            throw new CoreAiException(LlmProvider.GOOGLE_AI,
                    "Failed to create Google AI model: " + e.getMessage(), e);
        }
    }

    /**
     * Creates OpenAI chat model
     */
    private static ChatLanguageModel createOpenAiModel(LlmConfig config) throws CoreAiException {
        try {
            OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                    .apiKey(config.getApiKey())
                    .modelName(config.getModel())
                    .maxCompletionTokens(config.getMaxTokens());

            // o1 models do not support custom temperature
            // Check for "o1-" case-insensitive
            String modelNameLower = config.getModel().toLowerCase();
            if (!modelNameLower.contains("o1-") && !modelNameLower.equals("o1")) {
                builder.temperature(config.getTemperature());
            }

            return builder.build();
        } catch (Exception e) {
            throw new CoreAiException(LlmProvider.OPENAI,
                    "Failed to create OpenAI model: " + e.getMessage(), e);
        }
    }

    /**
     * Creates Anthropic Claude chat model
     */
    private static ChatLanguageModel createAnthropicModel(LlmConfig config) throws CoreAiException {
        try {
            return AnthropicChatModel.builder()
                    .apiKey(config.getApiKey())
                    .modelName(config.getModel())
                    .temperature(config.getTemperature())
                    .maxTokens(config.getMaxTokens())
                    .build();
        } catch (Exception e) {
            throw new CoreAiException(LlmProvider.ANTHROPIC,
                    "Failed to create Anthropic model: " + e.getMessage(), e);
        }
    }

    /**
     * Creates Ollama local chat model
     */
    private static ChatLanguageModel createOllamaModel(LlmConfig config) throws CoreAiException {
        try {
            String baseUrl = config.getBaseUrl();
            if (baseUrl == null || baseUrl.trim().isEmpty()) {
                baseUrl = "http://localhost:11434";
            }

            return OllamaChatModel.builder()
                    .httpClientBuilder(new JdkHttpClientBuilder())
                    .baseUrl(baseUrl)
                    .modelName(config.getModel())
                    .temperature(config.getTemperature())
                    .build();
        } catch (Exception e) {
            throw new CoreAiException(LlmProvider.OLLAMA,
                    "Failed to create Ollama model: " + e.getMessage(), e);
        }
    }
}
