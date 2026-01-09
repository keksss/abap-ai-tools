package com.keksss.abap.ai.ui.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import com.keksss.abap.ai.ui.Activator;

public class DumpAnalyzerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public DumpAnalyzerPreferencePage() {
        super(GRID);
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID));
        setDescription("Configure settings for ABAP Dump Analyser");
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected void createFieldEditors() {
        MultiLineStringFieldEditor promptEditor = new MultiLineStringFieldEditor(
                PreferenceConstants.P_DUMP_ANALYZER_PROMPT,
                "System Prompt:",
                getFieldEditorParent());
        addField(promptEditor);

        // Add a note about the placeholder
        Label note = new Label(getFieldEditorParent(), SWT.WRAP);
        note.setText("Note: The placeholder {dump_content} will be replaced by the actual dump content.");
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2; // Span across the grid columns
        note.setLayoutData(gd);
    }

    // Custom MultiLine String Field Editor if StringFieldEditor doesn't support
    // multiline well in older Eclipses or simple usage
    // But StringFieldEditor usually is single line. We might need a custom one or
    // check if there is one.
    // Standard StringFieldEditor does not work well for multi-line.
    // Let's implement a simple inner class or just use a custom implementation
    // logic if needed.
    // A simple way is to extend StringFieldEditor or just implement a custom
    // FieldEditor.
    // For simplicity, let's try to define a nested class here or just use a
    // specialized implementation.

    // Actually, JFace doesn't have a built-in MultiLineStringFieldEditor in older
    // versions.
    // I will implement a simple one here.

    /**
     * A simple field editor for multi-line text.
     */

    private static class MultiLineStringFieldEditor extends FieldEditor {
        private Text textField;
        private String initialValue;

        public MultiLineStringFieldEditor(String name, String labelText, Composite parent) {
            init(name, labelText);
            createControl(parent);
        }

        @Override
        protected void adjustForNumColumns(int numColumns) {
            GridData gd = (GridData) textField.getLayoutData();
            gd.horizontalSpan = numColumns - 1;
            // We only grab excess horizontal space
            gd.grabExcessHorizontalSpace = true;
        }

        @Override
        protected void doFillIntoGrid(Composite parent, int numColumns) {
            Label label = getLabelControl(parent);
            GridData gd = new GridData();
            label.setLayoutData(gd);

            textField = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
            gd = new GridData(GridData.FILL_BOTH);
            gd.heightHint = 200; // Give it some height
            gd.horizontalSpan = numColumns - 1;
            textField.setLayoutData(gd);

            if (initialValue != null) {
                textField.setText(initialValue);
            }

            textField.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    valueChanged();
                }
            });
        }

        @Override
        protected void doLoad() {
            if (textField != null) {
                String value = getPreferenceStore().getString(getPreferenceName());
                textField.setText(value);
                initialValue = value;
            }
        }

        @Override
        protected void doLoadDefault() {
            if (textField != null) {
                String value = getPreferenceStore().getDefaultString(getPreferenceName());
                textField.setText(value);
            }
            valueChanged();
        }

        @Override
        protected void doStore() {
            getPreferenceStore().setValue(getPreferenceName(), textField.getText());
        }

        @Override
        public int getNumberOfControls() {
            return 2;
        }

        protected void valueChanged() {
            setPresentsDefaultValue(false);
            String oldValue = initialValue;
            String newValue = textField.getText();
            fireValueChanged(FieldEditor.VALUE, oldValue, newValue);
        }
    }
}
