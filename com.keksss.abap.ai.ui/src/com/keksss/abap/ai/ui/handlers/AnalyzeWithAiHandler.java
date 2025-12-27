package com.keksss.abap.ai.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class AnalyzeWithAiHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        ISelection selection = HandlerUtil.getCurrentSelection(event);

        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            Object firstElement = structuredSelection.getFirstElement();

            // Check if the selected element is an AbapFeedEntry by class name
            if (firstElement != null &&
                    firstElement.getClass().getName().equals("com.sap.adt.feedreader.internal.feed.AbapFeedEntry")) {

                // Get title using reflection to avoid direct dependency
                String title = "Unknown";
                try {
                    java.lang.reflect.Method getTitleMethod = firstElement.getClass().getMethod("getTitle");
                    title = (String) getTitleMethod.invoke(firstElement);
                } catch (Exception e) {
                    title = firstElement.toString();
                }

                // Implement AI analysis logic
                String apiKey = com.keksss.abap.ai.core.PreferenceHelper.getGoogleAiApiKey();

                if (apiKey == null || apiKey.isEmpty()) {
                    MessageDialog.openError(window.getShell(), "Configuration Error",
                            "Google AI API Key is not configured.");
                    return null;
                }

                try {
                    // Open and show the view
                    com.keksss.abap.ai.ui.views.AbapAiResultView view = (com.keksss.abap.ai.ui.views.AbapAiResultView) window
                            .getActivePage()
                            .showView(com.keksss.abap.ai.ui.views.AbapAiResultView.ID);

                    view.setContent("<html><body><h3>Analyzing...</h3></body></html>");

                    // Run analysis in a job or thread to avoid freezing UI (for now simple
                    // thread/job)
                    // For simplicity in this handler, we might run it sync or use a simple job.
                    // Given the usually short response time, a Job is better but let's stick to
                    // simple logic for this step
                    // or better, use a Job to not block UI.

                    final String prompt = "Analyze this ABAP feed entry: " + title;
                    final String finalTitle = title;

                    new org.eclipse.core.runtime.jobs.Job("AI Analysis") {
                        @Override
                        protected org.eclipse.core.runtime.IStatus run(
                                org.eclipse.core.runtime.IProgressMonitor monitor) {
                            com.keksss.abap.ai.core.GoogleAiClient client = new com.keksss.abap.ai.core.GoogleAiClient();
                            com.keksss.abap.ai.core.AnalysisResult result = client.analyzeText(apiKey, prompt);

                            window.getShell().getDisplay().asyncExec(() -> {
                                if (result.isSuccess()) {
                                    java.time.LocalDateTime now = java.time.LocalDateTime.now();
                                    String dateStr = now
                                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                    String timeStr = now
                                            .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

                                    String html = "<html><style>" +
                                            "body { font-family: sans-serif; padding: 10px; }" +
                                            ".header { color: #555; font-size: 0.9em; margin-bottom: 15px; }" +
                                            ".title { font-size: 1.2em; font-weight: bold; margin-bottom: 20px; }" +
                                            ".content { border: 1px solid #ccc; padding: 15px; background-color: #f9f9f9; border-radius: 5px; }"
                                            +
                                            "</style><body>" +
                                            "<div class='header'>Data: " + dateStr + ", Time: " + timeStr + "</div>" +
                                            "<div class='title'>" + finalTitle + "</div>" +
                                            "<div class='content'>" + result.getAnalysisText().replace("\n", "<br/>")
                                            + "</div>"
                                            +
                                            "</body></html>";

                                    view.setContent(html);
                                } else {
                                    view.setContent("<html><body><h3>Error</h3><p>" + result.getErrorMessage()
                                            + "</p></body></html>");
                                }
                            });
                            return org.eclipse.core.runtime.Status.OK_STATUS;
                        }
                    }.schedule();

                } catch (Exception e) {
                    MessageDialog.openError(window.getShell(), "Error",
                            "Failed to open results view: " + e.getMessage());
                }
            } else {
                MessageDialog.openWarning(
                        window.getShell(),
                        "Analyze with AI",
                        "Please select a valid ABAP feed entry.");
            }
        }

        return null;
    }
}
