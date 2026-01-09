package com.keksss.abap.ai.core;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;

/**
 * Helper class to retrieve configuration from UI plugin preferences
 */
public class PreferenceHelper {

    private static final String UI_PLUGIN_ID = "com.keksss.abap.ai.ui";
    private static final String API_KEY_PREFERENCE = "googleAiApiKey";
    private static final String MODEL_PREFERENCE = "googleAiModel";
    public static final String DEFAULT_MODEL = "gemini-1.5-flash";

    /**
     * Retrieves the Google AI API key from preferences
     * 
     * @return API key or null if not set
     */
    public static String getGoogleAiApiKey() {
        try {
            Preferences preferences = InstanceScope.INSTANCE.getNode(UI_PLUGIN_ID);
            String apiKey = preferences.get(API_KEY_PREFERENCE, null);
            return apiKey;
        } catch (Exception e) {
            System.err.println("Error retrieving API key from preferences: " + e.getMessage());
            return null;
        }
    }

    private static final String DUMP_ANALYZER_PROMPT_PREFERENCE = "dumpAnalyzerPrompt";

    /**
     * Retrieves the Google AI Model from preferences
     * 
     * @return Model name or default if not set
     */
    public static String getGoogleAiModel() {
        try {
            Preferences preferences = InstanceScope.INSTANCE.getNode(UI_PLUGIN_ID);
            String model = preferences.get(MODEL_PREFERENCE, DEFAULT_MODEL);
            return (model != null && !model.isEmpty()) ? model : DEFAULT_MODEL;
        } catch (Exception e) {
            System.err.println("Error retrieving Model from preferences: " + e.getMessage());
            return DEFAULT_MODEL;
        }
    }

    /**
     * Retrieves the Dump Analyzer System Prompt from preferences
     * 
     * @return Custom prompt or null if not set (should rely on default in UI, but
     *         here we can return null to fallback)
     */
    public static String getDumpAnalyzerPrompt() {
        try {
            Preferences preferences = InstanceScope.INSTANCE.getNode(UI_PLUGIN_ID);
            return preferences.get(DUMP_ANALYZER_PROMPT_PREFERENCE, null);
        } catch (Exception e) {
            System.err.println("Error retrieving Dump Analyzer Prompt from preferences: " + e.getMessage());
            return null;
        }
    }

    /**
     * Validates that API key is configured
     * 
     * @return true if API key exists and is not empty
     */
    public static boolean isApiKeyConfigured() {
        String apiKey = getGoogleAiApiKey();
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    // ========== New Multi-Provider Configuration ==========

    private static final String LLM_PROVIDER_PREFERENCE = "llmProvider";
    private static final String LLM_API_KEY_PREFERENCE = "llmApiKey";
    private static final String LLM_MODEL_PREFERENCE = "llmModel";
    private static final String LLM_BASE_URL_PREFERENCE = "llmBaseUrl";
    private static final String LLM_TEMPERATURE_PREFERENCE = "llmTemperature";
    private static final String LLM_MAX_TOKENS_PREFERENCE = "llmMaxTokens";

    /**
     * Gets the complete LLM configuration from preferences
     * 
     * @return LlmConfig object with all settings
     */
    public static LlmConfig getLlmConfig() {
        try {
            Preferences preferences = InstanceScope.INSTANCE.getNode(UI_PLUGIN_ID);

            // Get provider (with backward compatibility)
            String providerStr = preferences.get(LLM_PROVIDER_PREFERENCE, null);
            LlmProvider provider;

            if (providerStr == null || providerStr.isEmpty()) {
                // Migration: check if old Google AI settings exist
                String oldApiKey = preferences.get(API_KEY_PREFERENCE, null);
                if (oldApiKey != null && !oldApiKey.isEmpty()) {
                    provider = LlmProvider.GOOGLE_AI;
                } else {
                    provider = LlmProvider.GOOGLE_AI; // Default
                }
            } else {
                provider = LlmProvider.fromString(providerStr);
            }

            // Get API key (with backward compatibility)
            String apiKey = preferences.get(LLM_API_KEY_PREFERENCE, null);
            if (apiKey == null || apiKey.isEmpty()) {
                // Try old preference for migration
                apiKey = preferences.get(API_KEY_PREFERENCE, "");
            }

            // Get model (with backward compatibility)
            String model = preferences.get(LLM_MODEL_PREFERENCE, null);
            if (model == null || model.isEmpty()) {
                // Try old preference for migration
                model = preferences.get(MODEL_PREFERENCE, null);
                if (model == null || model.isEmpty()) {
                    model = getDefaultModel(provider);
                }
            }

            String baseUrl = preferences.get(LLM_BASE_URL_PREFERENCE, null);
            double temperature = preferences.getDouble(LLM_TEMPERATURE_PREFERENCE, 0.7);
            int maxTokens = preferences.getInt(LLM_MAX_TOKENS_PREFERENCE, 2048);

            return new LlmConfig.Builder()
                    .provider(provider)
                    .apiKey(apiKey != null ? apiKey : "")
                    .model(model)
                    .baseUrl(baseUrl)
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .build();

        } catch (Exception e) {
            System.err.println("Error retrieving LLM config from preferences: " + e.getMessage());
            e.printStackTrace();
            // Return default config
            return new LlmConfig.Builder().build();
        }
    }

    /**
     * Get default model name for a given provider
     * 
     * @param provider LLM provider
     * @return default model name
     */
    private static String getDefaultModel(LlmProvider provider) {
        switch (provider) {
            case GOOGLE_AI:
                return "gemini-1.5-flash";
            case OPENAI:
                return "gpt-4o-mini";
            case ANTHROPIC:
                return "claude-3-5-sonnet-20241022";
            case OLLAMA:
                return "llama2";
            default:
                return "gemini-1.5-flash";
        }
    }
}
