package com.keksss.abap.ai.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.keksss.abap.ai.ui.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID);
		store.setDefault(PreferenceConstants.P_GOOGLE_AI_API_KEY, "");

		StringBuilder defaultPrompt = new StringBuilder();
		defaultPrompt.append("You are an expert ABAP developer analyzing a runtime dump/error.\n\n");
		defaultPrompt.append("Please analyze the following ABAP dump and provide:\n");
		defaultPrompt.append("1. Root cause analysis\n");
		defaultPrompt.append("2. Possible solutions or fixes\n");
		defaultPrompt.append("3. Best practices to prevent this error\n");
		defaultPrompt.append("4. Any relevant SAP notes or documentation references if applicable\n\n");
		defaultPrompt.append("ABAP Dump Content:\n");
		defaultPrompt.append("---\n");
		defaultPrompt.append("{dump_content}");
		defaultPrompt.append("\n---\n\n");
		defaultPrompt.append("Provide a clear, concise analysis that would help a developer resolve this issue.");

		store.setDefault(PreferenceConstants.P_DUMP_ANALYZER_PROMPT, defaultPrompt.toString());
	}

}
