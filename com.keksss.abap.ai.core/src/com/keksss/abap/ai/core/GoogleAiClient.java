package com.keksss.abap.ai.core;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Client for interacting with Google AI Gemini API using the new Java SDK
 */
public class GoogleAiClient {

    /**
     * Analyzes text using Google AI Gemini model
     * 
     * @param apiKey Google AI API key
     * @param prompt The prompt/text to analyze
     * @return Analysis result from AI
     */
    public AnalysisResult analyzeText(String apiKey, String prompt) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return AnalysisResult.failure("API key is not configured. Please set it in Preferences -> ABAP AI Tools.");
        }

        if (prompt == null || prompt.trim().isEmpty()) {
            return AnalysisResult.failure("No content to analyze.");
        }

        String modelName = PreferenceHelper.getGoogleAiModel();

        try {
            // Initialize Client
            Client client = Client.builder()
                    .apiKey(apiKey)
                    .build();

            // Configure generation
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .temperature(0.7f)
                    .topP(0.95f)
                    .topK(40.0f)
                    .maxOutputTokens(2048)
                    .build();

            // Generate response
            GenerateContentResponse response = client.models.generateContent(
                    modelName,
                    prompt,
                    config);

            String result = response.text();

            if (result == null || result.trim().isEmpty()) {
                return AnalysisResult.failure("AI returned empty response.");
            }

            return AnalysisResult.success(result);

        } catch (Exception e) {
            String errorMsg = "Error calling Google AI API: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            return AnalysisResult.failure(errorMsg);
        }
    }

    /**
     * Fetches available models from Google AI API
     * 
     * @param apiKey Google AI API key
     * @return List of model names
     * @throws Exception if API call fails
     */
    public List<String> fetchAvailableModels(String apiKey) throws Exception {
        List<String> models = new ArrayList<>();

        try {
            // Using direct REST API for listing models as SDK might not expose it simply
            // yet or to match example logic
            // The example GenAiTester used direct HTTP, let's stick to that for reliability
            // or use SDK if possible.
            // Documentation says client.models.list() might exist but let's follow the
            // robust example pattern for now
            // since we don't have full javadoc for the new SDK handy, relying on the
            // working example is safer.

            URL url = URI.create("https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.has("models")) {
                    JSONArray modelsArray = jsonResponse.getJSONArray("models");
                    for (int i = 0; i < modelsArray.length(); i++) {
                        JSONObject model = modelsArray.getJSONObject(i);
                        if (model.has("name")) {
                            String name = model.getString("name");
                            if (name.startsWith("models/")) {
                                name = name.substring(7);
                            }
                            models.add(name);
                        }
                    }
                }
            } else {
                throw new Exception("HTTP " + responseCode);
            }
            connection.disconnect();

        } catch (Exception e) {
            throw new Exception("Failed to fetch models: " + e.getMessage(), e);
        }

        return models;
    }
}
