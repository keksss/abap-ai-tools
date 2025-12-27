# ABAP AI Tools for Eclipse

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![Eclipse](https://img.shields.io/badge/Eclipse-2024--09-purple.svg)](https://www.eclipse.org/)

This Eclipse plugin leverages the power of Google's AI (Gemini) to analyze ABAP runtime errors (dumps) directly within the Eclipse IDE. It helps ABAP developers quickly understand the root cause of issues and provides actionable solutions.

## Overview

The project is divided into two main OSGi bundles:
*   **`com.keksss.abap.ai.core`**: Contains the core business logic, including the Google AI client and data processing algorithms.
*   **`com.keksss.abap.ai.ui`**: Handles the user interface integration, including context menus in the ABAP Feeds view, commands, and preference pages.

## Features

*   **AI-Powered Analysis**: Automatically sends ABAP dump content to Google AI for deep analysis.
*   **Comprehensive Insights**: Generates a report including:
    *   Root Cause Analysis
    *   Possible Solutions & Fixes
    *   Best Practices to prevent recurrence
    *   Relevant SAP Notes (if applicable)
*   **Context Menu Integration**: distinct "Analyze with AI" command available directly in the ABAP Feeds View (ADT).
*   **Secure Configuration**: Securely store your Google AI API key in Eclipse Preferences.
*   **Results View**: Displays AI analysis results in a dedicated view with Markdown formatting support.

## Prerequisites

*   **Eclipse IDE**: 2024-09 or newer features.
*   **Java Runtime**: Java 21 is required for building and running the plugin.
*   **SAP ADT**: SAP ABAP Development Tools must be installed in Eclipse (as the plugin extends the Feeds View).
*   **Google AI API Key**: You need a valid API key for Google Gemini.

## Installation & Build

This project uses **Maven** and **Tycho** for dependency management and building.

### Building from Source

1.  Clone the repository:
    ```bash
    git clone https://github.com/YOUR_USERNAME/abap-ai-tools.git
    cd abap-ai-tools
    ```
2.  Build with Maven:
    ```bash
    mvn clean verify
    ```
    *(Note: Ensure you have Maven installed and configured to use Java 21)*

### Installing in Eclipse

After building, you can install the plugin by copying the generated JAR files from the `target` directories to your Eclipse plugins folder, or by creating an update site.

## Configuration

1.  Open Eclipse Preferences: `Window` -> `Preferences`.
2.  Navigate to `ABAP AI Tools`.
3.  Enter your **Google AI API Key**.
4.  Click `Apply and Close`.

## Usage

1.  Open the **ABAP Feeds View** in Eclipse (part of SAP ADT).
2.  Locate an ABAP Feed entry (e.g., a short dump or runtime error).
3.  Right-click on the entry.
4.  Select **"Analyze with AI"** from the context menu.
5.  Wait for the analysis to complete; the results will be displayed in the "AI Results" view.

## Project Structure

```
abap-ai-tools/
├── com.keksss.abap.ai.core/    # Core logic bundle
├── com.keksss.abap.ai.ui/      # UI bundle (Preferences, Menus, Views)
├── pom.xml                      # Root Maven configuration (Tycho)
├── LICENSE                      # MIT License
└── README.md                    # This file
```

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

*   Built with [Google Generative AI](https://ai.google.dev/)
*   Powered by Eclipse RCP and SAP ADT APIs

## Support

If you encounter any issues or have questions, please [open an issue](https://github.com/YOUR_USERNAME/abap-ai-tools/issues) on GitHub.

