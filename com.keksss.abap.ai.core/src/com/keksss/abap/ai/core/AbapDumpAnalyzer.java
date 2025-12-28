package com.keksss.abap.ai.core;

/**
 * Service for analyzing ABAP dumps using Google AI
 */
public class AbapDumpAnalyzer {

    private final GoogleAiClient aiClient;

    public AbapDumpAnalyzer() {
        this.aiClient = new GoogleAiClient();
    }

    /**
     * Analyzes an ABAP dump and provides insights
     * 
     * @param dumpContent The ABAP dump content to analyze
     * @return Analysis result containing AI insights
     */
    public AnalysisResult analyzeDump(String dumpContent) {
        // Get API key from preferences
        String apiKey = PreferenceHelper.getGoogleAiApiKey();

        if (apiKey == null || apiKey.trim().isEmpty()) {
            return AnalysisResult.failure(
                    "Google AI API key is not configured.\n\n" +
                            "Please configure it in:\n" +
                            "Window -> Preferences -> ABAP AI Tools");
        }

        // Construct a specialized prompt for ABAP dump analysis
        String prompt = constructAnalysisPrompt(dumpContent);

        // Call Google AI
        AnalysisResult result = aiClient.analyzeText(apiKey, prompt);

        if (result.isSuccess()) {
            return AnalysisResult.success(result.getAnalysisText() + "\n\nOriginal Dump Content:\n" + dumpContent);
        }

        return result;
    }

    /**
     * Constructs a detailed prompt for ABAP dump analysis
     */
    private String constructAnalysisPrompt(String dumpContent) {
        String customPrompt = PreferenceHelper.getDumpAnalyzerPrompt();

        if (customPrompt != null && !customPrompt.trim().isEmpty()) {
            // Use custom prompt from preferences, replacing placeholder
            return customPrompt.replace("{dump_content}", dumpContent);
        }

        // Fallback to hardcoded default if preference is missing (though Initializer
        // should set it)
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are an expert ABAP developer analyzing a runtime dump/error.\n\n");
        prompt.append("Please analyze the following ABAP dump and provide:\n");
        prompt.append("1. Root cause analysis\n");
        prompt.append("2. Possible solutions or fixes\n");
        prompt.append("3. Best practices to prevent this error\n");
        prompt.append("4. Any relevant SAP notes or documentation references if applicable\n\n");
        prompt.append("ABAP Dump Content:\n");
        prompt.append("---\n");
        prompt.append(dumpContent);
        prompt.append("\n---\n\n");
        prompt.append("Provide a clear, concise analysis that would help a developer resolve this issue.");

        return prompt.toString();
    }
}
