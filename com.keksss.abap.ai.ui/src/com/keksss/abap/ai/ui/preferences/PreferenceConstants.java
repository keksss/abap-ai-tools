package com.keksss.abap.ai.ui.preferences;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

	// ========== Multi-Provider Configuration ==========
	public static final String P_LLM_PROVIDER = "llmProvider";
	public static final String P_LLM_API_KEY = "llmApiKey";
	public static final String P_LLM_MODEL = "llmModel";
	public static final String P_LLM_BASE_URL = "llmBaseUrl";
	public static final String P_LLM_TEMPERATURE = "llmTemperature";
	public static final String P_LLM_MAX_TOKENS = "llmMaxTokens";

	// ========== Legacy (Deprecated) ==========
	/**
	 * @deprecated Use {@link #P_LLM_API_KEY} instead
	 */
	@Deprecated
	public static final String P_GOOGLE_AI_API_KEY = "googleAiApiKey";

	/**
	 * @deprecated Use {@link #P_LLM_MODEL} instead
	 */
	@Deprecated
	public static final String P_GOOGLE_AI_MODEL = "googleAiModel";

	public static final String P_DUMP_ANALYZER_PROMPT = "dumpAnalyzerPrompt";

}
