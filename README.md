# ABAP AI Tools

<p align="center">
  <strong>✨ Intelligent ABAP Development Assistant for Eclipse ADT</strong>
</p>

<p align="center">
  An Eclipse plugin that leverages AI to help ABAP developers analyze runtime dumps and debug errors faster.
</p>

<p align="center">
  <a href="#features">Features</a> •
  <a href="#supported-llm-providers">LLM Providers</a> •
  <a href="#installation">Installation</a> •
  <a href="#usage">Usage</a> •
  <a href="#configuration">Configuration</a> •
  <a href="#license">License</a>
</p>

---

## Features

- **🔍 AI-Powered Dump Analysis** — Right-click on any ABAP runtime error in the Feed Reader and get instant AI-driven insights
- **🎯 Root Cause Analysis** — Automatically identifies the likely cause of the dump
- **💡 Solution Suggestions** — Provides actionable fixes and best practices
- **🎨 Rich HTML Results View** — Displays analysis results in a clean, styled browser-based view
- **⚙️ Customizable Prompts** — Tailor the AI analysis prompt to your specific needs

## Supported LLM Providers

ABAP AI Tools supports multiple LLM providers through [LangChain4j](https://docs.langchain4j.dev/):

| Provider | Description | API Key Required |
|----------|-------------|------------------|
| **Google AI (Gemini)** | Google's Gemini family of models | ✅ Yes |
| **OpenAI** | GPT-4, GPT-4o, and other OpenAI models | ✅ Yes |
| **Anthropic (Claude)** | Claude 3 and other Anthropic models | ✅ Yes |
| **Ollama (Local)** | Run models locally with Ollama | ❌ No |

## Requirements

- **Eclipse IDE** — 2025-12 or later
- **SAP ADT** — ABAP Development Tools installed
- **Java** — JDK 21 or higher
- **LLM API Key** — From your chosen provider (except for Ollama local)

## Installation

### Option 1: Eclipse Marketplace (Coming Soon)

1. Open Eclipse
2. Go to **Help → Eclipse Marketplace**
3. Search for **"ABAP AI Tools"**
4. Click **Install**

### Option 2: Manual Installation

1. Download the latest release from the [Releases](https://github.com/keksss/abap-ai-tools/releases) page
2. In Eclipse, go to **Help → Install New Software...**
3. Click **Add → Archive** and select the downloaded zip file
4. Select **ABAP AI Tools** and complete the installation
5. Restart Eclipse

## Usage

### Analyzing ABAP Dumps

1. **Open the Feed Reader view** in Eclipse ADT
2. **Right-click** on any Runtime Error entry
3. Select **"Explain Dump (AI based)"** from the context menu
4. Wait for the AI analysis to complete
5. View the results in the **AI Results** view

The analysis will provide:
- **Root cause analysis** of the error
- **Possible solutions** and fixes
- **Best practices** to prevent similar errors
- **Relevant SAP Notes** and documentation references

### Opening the AI Results View

If the AI Results view is not visible:

1. Go to **Window → Show View → Other...**
2. Navigate to **ABAP AI → AI Results**
3. Click **Open**

## Configuration

### Basic Configuration

1. Go to **Window → Preferences**
2. Navigate to **ABAP AI Tools**
3. Configure the following settings:

| Setting | Description |
|---------|-------------|
| **Provider** | Select your LLM provider |
| **API Key** | Your API key (for cloud providers) |
| **Model** | The specific model to use |
| **Base URL** | Custom endpoint URL (for Ollama) |
| **Temperature** | Controls response creativity (0.0-1.0) |
| **Max Tokens** | Maximum response length |

### Customizing the Analysis Prompt

1. Go to **Window → Preferences → ABAP AI Tools → Dump Analyser**
2. Modify the **System Prompt** to customize how the AI analyzes dumps
3. Use `{title}` and `{dump_content}` placeholders in your prompt

**Default prompt template:**
```
You are an expert ABAP developer analyzing a runtime dump/error.

Dump Title: {title}
Please analyze the following ABAP dump and provide:
1. Root cause analysis
2. Possible solutions or fixes
3. Best practices to prevent this error
4. Any relevant SAP notes or documentation references if applicable

ABAP Dump Content:
---
{dump_content}
---

Provide a clear, concise analysis that would help a developer resolve this issue.
```

## Project Structure

```
abap-ai-tools/
├── com.keksss.abap.ai.core/       # Core plugin - LLM integration & analysis logic
│   ├── src/
│   │   └── com/keksss/abap/ai/core/
│   │       ├── AbapDumpAnalyzer.java    # Main dump analysis service
│   │       ├── LlmClient.java           # Universal LLM client
│   │       ├── LlmClientFactory.java    # Factory for provider-specific models
│   │       ├── LlmConfig.java           # Configuration data class
│   │       └── LlmProvider.java         # Supported providers enum
│   └── lib/                             # LangChain4j dependencies
│
├── com.keksss.abap.ai.ui/         # UI plugin - Eclipse integration
│   ├── src/
│   │   └── com/keksss/abap/ai/ui/
│   │       ├── handlers/                # Command handlers
│   │       ├── preferences/             # Preference pages
│   │       └── views/                   # Result view
│   └── plugin.xml                       # Eclipse extension points
│
├── com.keksss.abap.ai.feature/    # Eclipse feature definition
├── com.keksss.abap.ai.repository/ # P2 update site
└── pom.xml                        # Parent Maven/Tycho build
```

## Building from Source

This project uses Maven with Tycho for building Eclipse plugins.

```bash
# Clone the repository
git clone https://github.com/keksss/abap-ai-tools.git
cd abap-ai-tools

# Build with Maven
mvn clean verify
```

The built P2 repository will be available in `com.keksss.abap.ai.repository/target/repository/`.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Roadmap

- [ ] Code explanation and documentation generation
- [ ] ABAP code optimization suggestions
- [ ] Integration with more ADT views (Code Editor, etc.)
- [ ] Support for additional LLM providers
- [ ] Conversation history and follow-up questions


---

<p align="center">
  Made with ❤️ for the ABAP community
</p>
