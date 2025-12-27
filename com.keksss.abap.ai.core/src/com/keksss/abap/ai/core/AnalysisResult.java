package com.keksss.abap.ai.core;

/**
 * Data class to hold analysis results from AI processing
 */
public class AnalysisResult {

    private boolean success;
    private String analysisText;
    private String errorMessage;

    public AnalysisResult(boolean success, String analysisText, String errorMessage) {
        this.success = success;
        this.analysisText = analysisText;
        this.errorMessage = errorMessage;
    }

    /**
     * Creates a successful result
     */
    public static AnalysisResult success(String analysisText) {
        return new AnalysisResult(true, analysisText, null);
    }

    /**
     * Creates a failed result
     */
    public static AnalysisResult failure(String errorMessage) {
        return new AnalysisResult(false, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getAnalysisText() {
        return analysisText;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
