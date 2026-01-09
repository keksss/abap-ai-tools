package com.keksss.abap.ai.core;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.chat.ChatLanguageModel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

/**
 * Client for interacting with Google AI Gemini API using LangChain4j
 */
public class GoogleAiClient {

    /**
     * Analyzes text using Google AI Gemini model via LangChain4j
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
            // Initialize LangChain4j GoogleAiGeminiChatModel
            ChatLanguageModel model = GoogleAiGeminiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .temperature(0.7)
                    .topP(0.95)
                    .topK(40)
                    .maxOutputTokens(2048)
                    .build();

            // Generate response using LangChain4j
            String result = model.generate(prompt);

            if (result == null || result.trim().isEmpty()) {
                return AnalysisResult.failure("AI returned empty response.");
            }

            return AnalysisResult.success(result);

        } catch (Exception e) {
            String errorMsg = "Error calling Google AI API via LangChain4j: " + e.getMessage();
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
            // Using direct REST API for listing models
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

                // Use Gson for JSON parsing
                Gson gson = new Gson();
                JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);

                if (jsonResponse.has("models")) {
                    JsonArray modelsArray = jsonResponse.getAsJsonArray("models");
                    for (int i = 0; i < modelsArray.size(); i++) {
                        JsonObject model = modelsArray.get(i).getAsJsonObject();
                        if (model.has("name")) {
                            String name = model.get("name").getAsString();
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
