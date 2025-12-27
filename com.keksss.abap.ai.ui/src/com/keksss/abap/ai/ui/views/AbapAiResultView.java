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
                "<html><style>body { font-family: sans-serif; padding: 10px; } h1 { font-size: 18px; margin: 0 0 5px 0; } h2 { font-size: 16px; margin: 15px 0 5px 0; } p { margin: 5px 0; } ul { margin: 5px 0; padding-left: 20px; } li { margin-bottom: 5px; } .note { margin-top: 15px; font-style: italic; color: #666; border-top: 1px solid #eee; padding-top: 10px; }</style><body>"
                        +
                        "<h1>ABAP AI Tools</h1>" +
                        "<p><b>✨ Your Intelligent ABAP Assistant</b></p>" +
                        "<p>Welcome! This view is designed to help you analyze code and debug runtime errors using Google Gemini AI.</p>"
                        +
                        "<h2>🚀 How to use</h2>" +
                        "<p>To see results here, follow these simple steps:</p>" +
                        "<p><b>For Dump Analysis:</b></p>" +
                        "<ul>" +
                        "<li>Open the <b>Feed Reader</b> view.</li>" +
                        "<li>Right-click on a <b>Runtime Error</b>.</li>" +
                        "<li>Select <b>Explain Dump (AI based)</b>.</li>" +
                        "</ul>" +
                        "<p class=\"note\">Note: Make sure you have provided your Google AI API Key in Window &gt; Preferences &gt; ABAP AI Tools.</p>"
                        +
                        "</body></html>");
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
