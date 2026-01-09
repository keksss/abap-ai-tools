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
            String result = model.chat(prompt);

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
    /**
     * Fetch available Anthropic models from API
     */
    private List<String> fetchAnthropicModels() throws CoreAiException {
        LlmConfig config = PreferenceHelper.getLlmConfig();
        String apiKey = config.getApiKey();

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new CoreAiException(LlmProvider.ANTHROPIC, "API key is required to fetch models");
        }

        List<String> models = new ArrayList<>();
        try {
            URL url = URI.create("https://api.anthropic.com/v1/models").toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("x-api-key", apiKey);
            connection.setRequestProperty("anthropic-version", "2023-06-01");
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

                if (jsonResponse.has("data")) {
                    JsonArray modelsArray = jsonResponse.getAsJsonArray("data");
                    for (int i = 0; i < modelsArray.size(); i++) {
                        JsonObject model = modelsArray.get(i).getAsJsonObject();
                        if (model.has("id")) {
                            models.add(model.get("id").getAsString());
                        }
                    }
                }
            } else {
                throw new CoreAiException(LlmProvider.ANTHROPIC, "HTTP " + responseCode);
            }
            connection.disconnect();

        } catch (CoreAiException e) {
            throw e;
        } catch (Exception e) {
            throw new CoreAiException(LlmProvider.ANTHROPIC,
                    "Failed to fetch models: " + e.getMessage(), e);
        }

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
