---
trigger: model_decision
description: when working ai.core class
---

1. Code Against Interfaces, Not Implementations
Your module must expose only the generic interface (e.g., ChatLanguageModel or StreamingChatLanguageModel).
Do: Return ChatLanguageModel.
Don't: Return concrete classes like OpenAiChatModel or OllamaChatModel. This ensures the client code remains decoupled from specific providers.

2. Implement the Factory Pattern
Create a central "Factory" or "Builder" within your core module. This logic should accept a configuration object and instantiate the correct implementation internally.
Logic: If the input is "DeepSeek" → instantiate OpenAiChatModel with DeepSeek's baseUrl. If "Vertex" → instantiate VertexAiGeminiChatModel.

3. Abstract and Wrap Exceptions
Different providers throw different exceptions (e.g., OpenAiHttpException vs. GoogleJsonResponseException).
Action: Catch all provider-specific exceptions within your core module.
Result: Wrap them and re-throw a single custom exception (e.g., CoreAiException). This prevents the client application from crashing due to unknown dependency errors.