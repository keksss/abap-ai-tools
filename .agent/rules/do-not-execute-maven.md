---
trigger: model_decision
description: DO NOT suggest or execute Maven commands via the CLI
---

Whenever a build, test, or dependency update is needed:
1. Ignore standard CLI approaches (like `mvn clean install`).
2. Instead, provide step-by-step manual instructions to achieve the result using the **Eclipse IDE GUI**.
   - Examples: Use "Run As -> Eclipse Application", "Project -> Clean", "Maven -> Update Project", or the "Export" wizard.