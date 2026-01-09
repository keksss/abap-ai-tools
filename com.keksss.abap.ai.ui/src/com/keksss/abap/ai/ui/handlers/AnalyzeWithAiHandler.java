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

                // Get content using reflection
                String content = "";

                // Try Adaptation to IFile or IStorage
                try {
                    // Try adapting to IFile
                    org.eclipse.core.resources.IFile file = org.eclipse.core.runtime.Platform.getAdapterManager()
                            .getAdapter(firstElement, org.eclipse.core.resources.IFile.class);
                    if (file == null && firstElement instanceof org.eclipse.core.runtime.IAdaptable) {
                        file = ((org.eclipse.core.runtime.IAdaptable) firstElement)
                                .getAdapter(org.eclipse.core.resources.IFile.class);
                    }

                    if (file != null) {
                        try (java.io.InputStream is = file.getContents()) {
                            byte[] bytes = is.readAllBytes();
                            content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                        }
                    } else {
                        // Try adapting to IStorage
                        org.eclipse.core.resources.IStorage storage = org.eclipse.core.runtime.Platform
                                .getAdapterManager()
                                .getAdapter(firstElement, org.eclipse.core.resources.IStorage.class);
                        if (storage == null && firstElement instanceof org.eclipse.core.runtime.IAdaptable) {
                            storage = ((org.eclipse.core.runtime.IAdaptable) firstElement)
                                    .getAdapter(org.eclipse.core.resources.IStorage.class);
                        }

                        if (storage != null) {
                            try (java.io.InputStream is = storage.getContents()) {
                                byte[] bytes = is.readAllBytes();
                                content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                            }
                        }
                    }

                    // Fallback: Check for "getPreview()" or similar method on the entry directly if
                    // adaptation fails
                    if (content.isEmpty()) {
                        // Check for getSummary() again but parse it better if it's HTML
                        java.lang.reflect.Method getSummaryMethod = firstElement.getClass().getMethod("getSummary");
                        Object summaryObj = getSummaryMethod.invoke(firstElement);
                        if (summaryObj != null) {
                            // If it returns Content object
                            try {
                                java.lang.reflect.Method getValueMethod = summaryObj.getClass().getMethod("getValue");
                                String val = (String) getValueMethod.invoke(summaryObj);
                                if (val != null)
                                    content = val;
                            } catch (Exception ex) {
                                content = summaryObj.toString();
                            }
                        }
                    }

                } catch (Exception e) {
                    // Ignore errors
                }

                // If content is still empty, fallback to description (just in case)
                if (content == null || content.isEmpty()) {
                    try {
                        java.lang.reflect.Method getDescriptionMethod = firstElement.getClass()
                                .getMethod("getDescription");
                        Object descObj = getDescriptionMethod.invoke(firstElement);
                        if (descObj != null) {
                            // ... existing fallback ...
                            try {
                                java.lang.reflect.Method getValueMethod = descObj.getClass().getMethod("getValue");
                                content = (String) getValueMethod.invoke(descObj);
                            } catch (Exception ex) {
                                content = descObj.toString();
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }

                // Check LLM configuration
                com.keksss.abap.ai.core.LlmConfig config = com.keksss.abap.ai.core.PreferenceHelper.getLlmConfig();

                if (config.getProvider().requiresApiKey() &&
                        (config.getApiKey() == null || config.getApiKey().isEmpty())) {
                    MessageDialog.openError(window.getShell(), "Configuration Error",
                            "LLM API Key is not configured for " + config.getProvider().getDisplayName() + ".");
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

                    final String finalTitle = title;
                    final String finalContent = content;

                    new org.eclipse.core.runtime.jobs.Job("AI Analysis") {
                        @Override
                        protected org.eclipse.core.runtime.IStatus run(
                                org.eclipse.core.runtime.IProgressMonitor monitor) {
                            com.keksss.abap.ai.core.AbapDumpAnalyzer analyzer = new com.keksss.abap.ai.core.AbapDumpAnalyzer();
                            com.keksss.abap.ai.core.AnalysisResult result = analyzer.analyzeDump(finalTitle,
                                    finalContent);

                            window.getShell().getDisplay().asyncExec(() -> {
                                if (result.isSuccess()) {
                                    java.time.LocalDateTime now = java.time.LocalDateTime.now();
                                    String dateStr = now
                                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                    String timeStr = now
                                            .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

                                    String analysisText = result.getAnalysisText();

                                    // Check if the content is predominantly HTML (full document)
                                    boolean isHtml = false;
                                    if (analysisText != null) {
                                        String trimmed = analysisText.trim().toLowerCase();
                                        isHtml = trimmed.startsWith("<!doctype") || trimmed.startsWith("<html");
                                    }

                                    if (isHtml) {
                                        // It is a full HTML document.
                                        // We inject the timestamp header for consistency but preserve the HTML
                                        // structure.
                                        String timestampHtml = "<div style='font-family: sans-serif; color: #555; font-size: 0.9em; padding: 10px; border-bottom: 1px solid #eee; background-color: #fcfcfc;'>Analysis Date: "
                                                + dateStr + ", Time: " + timeStr + "</div>";

                                        // Inject after <body> tag
                                        String finalHtml = analysisText.replaceFirst("(?i)<body>",
                                                "<body>" + timestampHtml);
                                        // If replacement didn't happen (no body tag), just use original
                                        if (finalHtml.equals(analysisText)) {
                                            finalHtml = analysisText;
                                        }
                                        view.setContent(finalHtml);
                                    } else {
                                        // Treat as plain text
                                        String html = "<html><style>" +
                                                "body { font-family: sans-serif; padding: 10px; }" +
                                                ".header { color: #555; font-size: 0.9em; margin-bottom: 15px; }" +
                                                ".title { font-size: 1.2em; font-weight: bold; margin-bottom: 20px; }" +
                                                ".content { border: 1px solid #ccc; padding: 15px; background-color: #f9f9f9; border-radius: 5px; }"
                                                +
                                                "</style><body>" +
                                                "<div class='header'>Date: " + dateStr + ", Time: " + timeStr + "</div>"
                                                +
                                                "<div class='content'>"
                                                + (analysisText != null ? analysisText.replace("\n", "<br/>") : "")
                                                + "</div>"
                                                +
                                                "</body></html>";
                                        view.setContent(html);
                                    }
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
