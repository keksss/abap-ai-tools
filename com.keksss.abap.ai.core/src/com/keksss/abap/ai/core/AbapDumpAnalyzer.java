package com.keksss.abap.ai.core;

/**
 * Service for analyzing ABAP dumps using configured LLM
 */
public class AbapDumpAnalyzer {

    private final LlmClient aiClient;

    public AbapDumpAnalyzer() {
        this.aiClient = new LlmClient();
    }

    /**
     * Analyzes an ABAP dump and provides insights
     * 
     * @param dumpContent The ABAP dump content to analyze
     * @return Analysis result containing AI insights
     */
    public AnalysisResult analyzeDump(String title, String dumpContent) {
        // Get LLM configuration
        LlmConfig config = PreferenceHelper.getLlmConfig();

        // Check if API key is configured (if required by provider)
        if (config.getProvider().requiresApiKey() &&
                (config.getApiKey() == null || config.getApiKey().trim().isEmpty())) {
            return AnalysisResult.failure(
                    "LLM API key is not configured for " + config.getProvider().getDisplayName() + ".\n\n" +
                            "Please configure it in:\n" +
                            "Window -> Preferences -> ABAP AI Tools");
        }

        // Construct a specialized prompt for ABAP dump analysis
        String prompt = constructAnalysisPrompt(title, dumpContent);

        // Call LLM through client
        AnalysisResult result = aiClient.analyzeText(prompt);

        if (result.isSuccess()) {
            return AnalysisResult.success(result.getAnalysisText() );
        }

        return result;
    }

    /**
     * Constructs a detailed prompt for ABAP dump analysis
     */
    private String constructAnalysisPrompt(String title, String dumpContent) {
        String customPrompt = PreferenceHelper.getDumpAnalyzerPrompt();

        if (customPrompt != null && !customPrompt.trim().isEmpty()) {
            // Use custom prompt from preferences, replacing placeholders
            return customPrompt
                    .replace("{title}", title != null ? title : "")
                    .replace("{dump_content}", dumpContent);
        }

        // Fallback to hardcoded default if preference is missing (though Initializer
        // should set it)
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are an expert ABAP developer analyzing a runtime dump/error.\n\n");
        if (title != null && !title.isEmpty()) {
            prompt.append("Dump Title: ").append(title).append("\n");
        }
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
