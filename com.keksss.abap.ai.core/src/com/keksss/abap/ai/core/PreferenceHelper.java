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
     * Validates that API key is configured
     * 
     * @return true if API key exists and is not empty
     */
    public static boolean isApiKeyConfigured() {
        String apiKey = getGoogleAiApiKey();
        return apiKey != null && !apiKey.trim().isEmpty();
    }
}
