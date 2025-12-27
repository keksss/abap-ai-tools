package com.keksss.abap.ai.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class AbapAiResultView extends ViewPart {

    public static final String ID = "com.keksss.abap.ai.ui.views.AbapAiResultView";

    private Browser browser;

    @Override
    public void createPartControl(Composite parent) {
        browser = new Browser(parent, SWT.NONE);
        browser.setText(
                "<html><style>body { font-family: sans-serif; } code { background-color: #f0f0f0; padding: 2px; }</style><body><h1>AI Analysis Result</h1><p>Here is the <b>bold</b> text and <i>italic</i> text.</p><ul><li>Item 1</li><li>Item 2</li></ul><p><code>Code block</code></p></body></html>");
    }

    @Override
    public void setFocus() {
        if (browser != null && !browser.isDisposed()) {
            browser.setFocus();
        }
    }

    public void setContent(String htmlContent) {
        if (browser != null && !browser.isDisposed()) {
            browser.setText(htmlContent);
        }
    }
}
