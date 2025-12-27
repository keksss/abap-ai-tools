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

import com.keksss.abap.ai.core.GoogleAiClient;
import com.keksss.abap.ai.ui.Activator;

/**
 * Validates the API key and allows selecting a model from the available ones.
 */
public class AbapAiPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Text apiKeyText;
	private Combo modelCombo;
	private Button refreshButton;

	public AbapAiPreferencePage() {
		setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID));
		setDescription("Configure Google AI API settings for ABAP AI Tools");
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		container.setLayout(layout);

		// API Key
		Label apiKeyLabel = new Label(container, SWT.NONE);
		apiKeyLabel.setText("Google AI API Key:");

		apiKeyText = new Text(container, SWT.BORDER | SWT.PASSWORD);
		GridData apiKeyData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		apiKeyText.setLayoutData(apiKeyData);

		String currentApiKey = getPreferenceStore().getString(PreferenceConstants.P_GOOGLE_AI_API_KEY);
		apiKeyText.setText(currentApiKey);

		// Model
		Label modelLabel = new Label(container, SWT.NONE);
		modelLabel.setText("Model:");

		modelCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData modelData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		modelCombo.setLayoutData(modelData);

		String currentModel = getPreferenceStore().getString(PreferenceConstants.P_GOOGLE_AI_MODEL);
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

		return container;
	}

	private void refreshModels() {
		String apiKey = apiKeyText.getText().trim();
		if (apiKey.isEmpty()) {
			MessageDialog.openError(getShell(), "Error", "Please enter an API Key first.");
			return;
		}

		refreshButton.setEnabled(false);

		// Capture shell and display references on UI thread before starting background
		// job
		final var shell = getShell();
		final var display = shell.getDisplay();

		Job job = new Job("Fetching Google AI Models") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					GoogleAiClient client = new GoogleAiClient();
					List<String> models = client.fetchAvailableModels(apiKey);

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
						MessageDialog.openError(shell, "Error",
								"Failed to fetch models: " + ex.getMessage());
					});
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to fetch models", ex);
				}
			}
		};
		job.schedule();
	}

	@Override
	public boolean performOk() {
		getPreferenceStore().setValue(PreferenceConstants.P_GOOGLE_AI_API_KEY, apiKeyText.getText().trim());
		getPreferenceStore().setValue(PreferenceConstants.P_GOOGLE_AI_MODEL, modelCombo.getText());
		return super.performOk();
	}
}