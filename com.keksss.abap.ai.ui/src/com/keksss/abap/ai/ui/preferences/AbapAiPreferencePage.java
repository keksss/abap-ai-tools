package com.keksss.abap.ai.ui.preferences;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.keksss.abap.ai.core.LlmClient;
import com.keksss.abap.ai.core.LlmProvider;
import com.keksss.abap.ai.ui.Activator;

/**
 * Multi-provider LLM configuration page
 */
public class AbapAiPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Combo providerCombo;
	private Text apiKeyText;
	private Combo modelCombo;
	private Text baseUrlText;
	private Label baseUrlLabel;
	private Button refreshButton;

	// Advanced Settings
	private org.eclipse.swt.widgets.Scale temperatureScale;
	private Label temperatureValueLabel;
	private org.eclipse.swt.widgets.Spinner maxTokensSpinner;

	// Status
	private Label statusLabel;

	public AbapAiPreferencePage() {
		setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID));
		setDescription("Configure LLM Provider settings for ABAP AI Tools");
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		container.setLayout(layout);

		// Provider Selection
		Label providerLabel = new Label(container, SWT.NONE);
		providerLabel.setText("LLM Provider:");

		providerCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData providerData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		providerCombo.setLayoutData(providerData);

		// Populate providers
		for (LlmProvider provider : LlmProvider.values()) {
			providerCombo.add(provider.getDisplayName());
		}

		// Load current provider
		String currentProvider = getPreferenceStore().getString(PreferenceConstants.P_LLM_PROVIDER);
		if (currentProvider.isEmpty()) {
			currentProvider = "GOOGLE_AI";
		}
		LlmProvider selectedProvider = LlmProvider.fromString(currentProvider);
		providerCombo.select(getProviderIndex(selectedProvider));

		// Provider selection listener
		providerCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				updateUIForProvider();
			}
		});

		// API Key
		Label apiKeyLabel = new Label(container, SWT.NONE);
		apiKeyLabel.setText("API Key:");
		apiKeyLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		apiKeyText = new Text(container, SWT.BORDER | SWT.PASSWORD);
		GridData apiKeyData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		apiKeyData.widthHint = 300; // Fixed width to prevent stretching with long API keys
		apiKeyText.setLayoutData(apiKeyData);

		String currentApiKey = getPreferenceStore().getString(PreferenceConstants.P_LLM_API_KEY);
		apiKeyText.setText(currentApiKey);

		// Base URL (for Ollama, DeepSeek, etc.)
		baseUrlLabel = new Label(container, SWT.NONE);
		baseUrlLabel.setText("Base URL:");
		baseUrlLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		baseUrlText = new Text(container, SWT.BORDER);
		GridData baseUrlData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		baseUrlText.setLayoutData(baseUrlData);

		String currentBaseUrl = getPreferenceStore().getString(PreferenceConstants.P_LLM_BASE_URL);
		baseUrlText.setText(currentBaseUrl != null ? currentBaseUrl : "");

		// Model
		Label modelLabel = new Label(container, SWT.NONE);
		modelLabel.setText("Model:");

		modelCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData modelData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		modelCombo.setLayoutData(modelData);

		String currentModel = getPreferenceStore().getString(PreferenceConstants.P_LLM_MODEL);
		if (currentModel.isEmpty()) {
			currentModel = "gemini-1.5-flash"; // Default
		}
		modelCombo.add(currentModel);
		modelCombo.select(0);

		refreshButton = new Button(container, SWT.PUSH);
		refreshButton.setText("Refresh Models");
		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshModels();
			}
		});

		// ========== Advanced Settings Section ==========
		// Separator
		Label separator1 = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData sepData1 = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		sepData1.verticalIndent = 10;
		separator1.setLayoutData(sepData1);

		// Advanced Settings Group
		org.eclipse.swt.widgets.Group advancedGroup = new org.eclipse.swt.widgets.Group(container, SWT.NONE);
		advancedGroup.setText("Advanced Settings");
		GridData advancedGroupData = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		advancedGroup.setLayoutData(advancedGroupData);
		advancedGroup.setLayout(new GridLayout(3, false));

		// Temperature
		Label tempLabel = new Label(advancedGroup, SWT.NONE);
		tempLabel.setText("Temperature:");
		tempLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		temperatureScale = new org.eclipse.swt.widgets.Scale(advancedGroup, SWT.HORIZONTAL);
		temperatureScale.setMinimum(0);
		temperatureScale.setMaximum(100); // 0.0 to 1.0, scaled by 100
		temperatureScale.setIncrement(1);
		temperatureScale.setPageIncrement(10);
		GridData tempScaleData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		tempScaleData.widthHint = 200;
		temperatureScale.setLayoutData(tempScaleData);

		// Temperature value label
		temperatureValueLabel = new Label(advancedGroup, SWT.NONE);
		temperatureValueLabel.setText("0.0");
		GridData tempValueData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		tempValueData.widthHint = 35;
		temperatureValueLabel.setLayoutData(tempValueData);

		// Load current temperature
		double currentTemp = getPreferenceStore().getDouble(PreferenceConstants.P_LLM_TEMPERATURE);
		if (currentTemp == 0.0) {
			currentTemp = 0.7; // Default
		}
		temperatureScale.setSelection((int) (currentTemp * 100));
		temperatureValueLabel.setText(String.format("%.1f", currentTemp));

		// Add listener to update label when scale changes
		temperatureScale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				double value = temperatureScale.getSelection() / 100.0;
				temperatureValueLabel.setText(String.format("%.1f", value));
			}
		});

		// Max Tokens
		Label maxTokensLabel = new Label(advancedGroup, SWT.NONE);
		maxTokensLabel.setText("Max Tokens:");
		maxTokensLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		maxTokensSpinner = new org.eclipse.swt.widgets.Spinner(advancedGroup, SWT.BORDER);
		maxTokensSpinner.setMinimum(100);
		maxTokensSpinner.setMaximum(100000);
		maxTokensSpinner.setIncrement(100);
		maxTokensSpinner.setPageIncrement(1000);
		GridData maxTokensData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		maxTokensSpinner.setLayoutData(maxTokensData);

		// Load current max tokens
		int currentMaxTokens = getPreferenceStore().getInt(PreferenceConstants.P_LLM_MAX_TOKENS);
		if (currentMaxTokens == 0) {
			currentMaxTokens = 2048; // Default
		}
		maxTokensSpinner.setSelection(currentMaxTokens);

		// ========== Status Section ==========
		Label separator2 = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData sepData2 = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		sepData2.verticalIndent = 10;
		separator2.setLayoutData(sepData2);

		Label statusHeaderLabel = new Label(container, SWT.NONE);
		statusHeaderLabel.setText("Status:");
		statusHeaderLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		statusLabel = new Label(container, SWT.NONE);
		statusLabel.setText("Ready");
		GridData statusData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		statusLabel.setLayoutData(statusData);
		statusLabel.setForeground(container.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));

		// Update UI based on selected provider
		updateUIForProvider();

		return container;
	}

	/**
	 * Update UI elements based on selected provider
	 */
	private void updateUIForProvider() {
		int index = providerCombo.getSelectionIndex();
		if (index < 0)
			return;

		LlmProvider provider = LlmProvider.values()[index];

		// Show/hide API key based on provider
		apiKeyText.setEnabled(provider.requiresApiKey());

		// Show/hide base URL based on provider
		boolean showBaseUrl = provider.supportsBaseUrl();
		baseUrlLabel.setVisible(showBaseUrl);
		baseUrlText.setVisible(showBaseUrl);
		((GridData) baseUrlText.getLayoutData()).exclude = !showBaseUrl;
		((GridData) baseUrlLabel.getLayoutData()).exclude = !showBaseUrl;

		// Update placeholder text for base URL
		if (provider == LlmProvider.OLLAMA) {
			baseUrlText.setMessage("http://localhost:11434");
		}

		// Re-layout
		baseUrlLabel.getParent().layout();
	}

	/**
	 * Get provider index in combo
	 */
	private int getProviderIndex(LlmProvider provider) {
		for (int i = 0; i < LlmProvider.values().length; i++) {
			if (LlmProvider.values()[i] == provider) {
				return i;
			}
		}
		return 0;
	}

	private void refreshModels() {
		LlmProvider provider = LlmProvider.values()[providerCombo.getSelectionIndex()];

		// Validate API key if required
		if (provider.requiresApiKey()) {
			String apiKey = apiKeyText.getText().trim();
			if (apiKey.isEmpty()) {
				MessageDialog.openError(getShell(), "Error",
						"Please enter an API Key first for " + provider.getDisplayName() + ".");
				return;
			}
		}

		refreshButton.setEnabled(false);

		// Capture shell, display and text field values on UI thread before starting
		// background
		// job
		final var shell = getShell();
		final var display = shell.getDisplay();
		final String apiKey = apiKeyText.getText().trim();
		final String baseUrl = baseUrlText.getText().trim();

		Job job = new Job("Fetching Models from " + provider.getDisplayName()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					// Temporarily save settings to preferences for LlmClient to use
					getPreferenceStore().setValue(PreferenceConstants.P_LLM_PROVIDER, provider.name());
					getPreferenceStore().setValue(PreferenceConstants.P_LLM_API_KEY, apiKey);
					getPreferenceStore().setValue(PreferenceConstants.P_LLM_BASE_URL, baseUrl);

					LlmClient client = new LlmClient();
					List<String> models = client.fetchAvailableModels();

					display.asyncExec(() -> {
						if (!modelCombo.isDisposed()) {
							String currentSelection = modelCombo.getText();
							modelCombo.removeAll();
							for (String model : models) {
								modelCombo.add(model);
							}

							// Restore selection if possible, or select first
							int index = -1;
							for (int i = 0; i < modelCombo.getItemCount(); i++) {
								if (modelCombo.getItem(i).equals(currentSelection)) {
									index = i;
									break;
								}
							}
							if (index >= 0) {
								modelCombo.select(index);
							} else if (modelCombo.getItemCount() > 0) {
								modelCombo.select(0);
							}

							refreshButton.setEnabled(true);

							// Update status
							if (!statusLabel.isDisposed()) {
								statusLabel.setText("✓ Connected successfully");
								statusLabel.setForeground(display.getSystemColor(SWT.COLOR_DARK_GREEN));
							}

							MessageDialog.openInformation(shell, "Success",
									"Models refreshed successfully. Found: " + models.size());
						}
					});

					return Status.OK_STATUS;
				} catch (Exception ex) {
					// Handle error on UI thread
					display.asyncExec(() -> {
						if (!refreshButton.isDisposed()) {
							refreshButton.setEnabled(true);
						}

						// Update status with error
						if (!statusLabel.isDisposed()) {
							statusLabel.setText("✗ Connection failed: " + ex.getMessage());
							statusLabel.setForeground(display.getSystemColor(SWT.COLOR_RED));
						}

						MessageDialog.openError(shell, "Error", "Failed to fetch models: " + ex.getMessage());
					});
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to fetch models", ex);
				}
			}
		};
		job.schedule();
	}

	@Override
	public boolean performOk() {
		LlmProvider provider = LlmProvider.values()[providerCombo.getSelectionIndex()];

		getPreferenceStore().setValue(PreferenceConstants.P_LLM_PROVIDER, provider.name());
		getPreferenceStore().setValue(PreferenceConstants.P_LLM_API_KEY, apiKeyText.getText().trim());
		getPreferenceStore().setValue(PreferenceConstants.P_LLM_MODEL, modelCombo.getText());
		getPreferenceStore().setValue(PreferenceConstants.P_LLM_BASE_URL, baseUrlText.getText().trim());

		// Save Advanced Settings
		double temperature = temperatureScale.getSelection() / 100.0;
		getPreferenceStore().setValue(PreferenceConstants.P_LLM_TEMPERATURE, temperature);
		getPreferenceStore().setValue(PreferenceConstants.P_LLM_MAX_TOKENS, maxTokensSpinner.getSelection());

		return super.performOk();
	}
}