# Contributing to ABAP AI Tools

Thank you for your interest in contributing to ABAP AI Tools! This document provides guidelines for contributing to this project.

## How to Contribute

### Reporting Bugs

If you find a bug, please create an issue on GitHub with:
- A clear, descriptive title
- Detailed steps to reproduce the issue
- Expected behavior vs. actual behavior
- Your environment (Eclipse version, Java version, OS)
- Relevant logs or error messages

### Suggesting Enhancements

We welcome feature suggestions! Please create an issue with:
- A clear description of the proposed feature
- Use cases and benefits
- Any implementation ideas (optional)

### Pull Requests

1. **Fork the repository** and create your branch from `main`
2. **Make your changes** following the coding standards below
3. **Test your changes** thoroughly
4. **Update documentation** if needed
5. **Submit a pull request** with a clear description of your changes

## Development Setup

### Prerequisites
- Eclipse IDE 2024-09 or newer
- Java 21 JDK
- Maven 3.6+
- SAP ABAP Development Tools (ADT)

### Building the Project

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/abap-ai-tools.git
cd abap-ai-tools

# Build with Maven
mvn clean verify
```

### Project Structure
- `com.keksss.abap.ai.core/` - Core business logic
- `com.keksss.abap.ai.ui/` - Eclipse UI integration

## Coding Standards

- **Java Version**: Use Java 21 features appropriately
- **Code Style**: Follow standard Eclipse/Java formatting conventions
- **Comments**: Add JavaDoc for public APIs
- **Error Handling**: Use proper exception handling
- **Security**: Never commit API keys or sensitive data

## Testing

Before submitting a PR:
1. Ensure `mvn clean verify` passes without errors
2. Test the plugin in a running Eclipse instance
3. Verify your changes don't break existing functionality

## Questions?

Feel free to open an issue for any questions about contributing!

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
