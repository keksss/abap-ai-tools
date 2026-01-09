package com.keksss.abap.ai.core;

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
 * Universal client for interacting with LLM providers using LangChain4j
 */
public class LlmClient {

    /**
     * Analyzes text using configured LLM provider via LangChain4j
     * 
     * @param prompt The prompt/text to analyze
     * @return Analysis result from AI
     */
    public AnalysisResult analyzeText(String prompt) {
        LlmConfig config = PreferenceHelper.getLlmConfig();

        if (config.getProvider().requiresApiKey() &&
                (config.getApiKey() == null || config.getApiKey().trim().isEmpty())) {
            return AnalysisResult.failure(
                    "API key is not configured for " + config.getProvider().getDisplayName() +
                            ". Please set it in Preferences -> ABAP AI Tools.");
        }

        if (prompt == null || prompt.trim().isEmpty()) {
            return AnalysisResult.failure("No content to analyze.");
        }

        try {
            // Create model through factory
            ChatLanguageModel model = LlmClientFactory.createChatModel(config);

            // Generate response using LangChain4j
            String result = model.generate(prompt);

            if (result == null || result.trim().isEmpty()) {
                return AnalysisResult.failure("AI returned empty response.");
            }

            return AnalysisResult.success(result);

        } catch (CoreAiException e) {
            String errorMsg = e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            return AnalysisResult.failure(errorMsg);
        } catch (Exception e) {
            String errorMsg = "Error calling LLM API: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            return AnalysisResult.failure(errorMsg);
        }
    }

    /**
     * Fetches available models from the configured provider
     * 
     * @return List of model names
     * @throws CoreAiException if API call fails
     */
    public List<String> fetchAvailableModels() throws CoreAiException {
        LlmConfig config = PreferenceHelper.getLlmConfig();

        switch (config.getProvider()) {
            case GOOGLE_AI:
                return fetchGoogleAiModels(config.getApiKey());
            case OPENAI:
                return fetchOpenAiModels(config.getApiKey());
            case ANTHROPIC:
                return fetchAnthropicModels();
            case OLLAMA:
                return fetchOllamaModels(config.getBaseUrl());
            default:
                throw new CoreAiException(config.getProvider(),
                        "Fetching models not supported for: " + config.getProvider());
        }
    }

    /**
     * Fetch available Google AI models
     */
    private List<String> fetchGoogleAiModels(String apiKey) throws CoreAiException {
        List<String> models = new ArrayList<>();
        try {
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
                throw new CoreAiException(LlmProvider.GOOGLE_AI, "HTTP " + responseCode);
            }
            connection.disconnect();

        } catch (CoreAiException e) {
            throw e;
        } catch (Exception e) {
            throw new CoreAiException(LlmProvider.GOOGLE_AI,
                    "Failed to fetch models: " + e.getMessage(), e);
        }

        return models;
    }

    /**
     * Fetch available OpenAI models
     */
    private List<String> fetchOpenAiModels(String apiKey) throws CoreAiException {
        List<String> models = new ArrayList<>();
        try {
            URL url = URI.create("https://api.openai.com/v1/models").toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);

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

                Gson gson = new Gson();
                JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);

                if (jsonResponse.has("data")) {
                    JsonArray modelsArray = jsonResponse.getAsJsonArray("data");
                    for (int i = 0; i < modelsArray.size(); i++) {
                        JsonObject model = modelsArray.get(i).getAsJsonObject();
                        if (model.has("id")) {
                            String id = model.get("id").getAsString();
                            // Filter to show only GPT models
                            if (id.startsWith("gpt-")) {
                                models.add(id);
                            }
                        }
                    }
                }
            } else {
                throw new CoreAiException(LlmProvider.OPENAI, "HTTP " + responseCode);
            }
            connection.disconnect();

        } catch (CoreAiException e) {
            throw e;
        } catch (Exception e) {
            throw new CoreAiException(LlmProvider.OPENAI,
                    "Failed to fetch models: " + e.getMessage(), e);
        }

        return models;
    }

    /**
     * Return predefined Anthropic models (API doesn't provide list endpoint)
     */
    private List<String> fetchAnthropicModels() {
        List<String> models = new ArrayList<>();
        models.add("claude-3-5-sonnet-20241022");
        models.add("claude-3-5-haiku-20241022");
        models.add("claude-3-opus-20240229");
        models.add("claude-3-sonnet-20240229");
        models.add("claude-3-haiku-20240307");
        return models;
    }

    /**
     * Fetch available Ollama models from local instance
     */
    private List<String> fetchOllamaModels(String baseUrl) throws CoreAiException {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = "http://localhost:11434";
        }

        List<String> models = new ArrayList<>();
        try {
            URL url = URI.create(baseUrl + "/api/tags").toURL();
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

                Gson gson = new Gson();
                JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);

                if (jsonResponse.has("models")) {
                    JsonArray modelsArray = jsonResponse.getAsJsonArray("models");
                    for (int i = 0; i < modelsArray.size(); i++) {
                        JsonObject model = modelsArray.get(i).getAsJsonObject();
                        if (model.has("name")) {
                            models.add(model.get("name").getAsString());
                        }
                    }
                }
            } else {
                throw new CoreAiException(LlmProvider.OLLAMA,
                        "HTTP " + responseCode + " - Is Ollama running?");
            }
            connection.disconnect();

        } catch (CoreAiException e) {
            throw e;
        } catch (Exception e) {
            throw new CoreAiException(LlmProvider.OLLAMA,
                    "Failed to fetch models: " + e.getMessage() + " - Is Ollama running?", e);
        }

        return models;
    }
}
