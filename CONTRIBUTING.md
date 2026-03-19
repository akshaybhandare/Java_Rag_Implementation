# Contributing to Simple RAG Application

Thank you for your interest in contributing to the Simple RAG Application! This document provides guidelines and instructions for contributing.

## Code of Conduct

- Be respectful and inclusive
- Welcome diverse perspectives
- Focus on constructive feedback
- Report issues through proper channels

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+
- Git
- IDE (IntelliJ, Eclipse, VS Code)

### Setup Development Environment

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd org.simple.rag
   ```

2. **Build the project:**
   ```bash
   mvn clean install
   ```

3. **Import into IDE:**
   - IntelliJ: Open pom.xml
   - Eclipse: Import as Maven project
   - VS Code: Install Java extensions

4. **Create a feature branch:**
   ```bash
   git checkout -b feature/your-feature-name
   ```

## Development Guidelines

### Code Style

- **Language**: Java 17
- **Formatting**: Use standard Java conventions
- **Indentation**: 4 spaces (no tabs)
- **Line Length**: 120 characters maximum
- **Naming**: camelCase for variables/methods, PascalCase for classes

### Code Quality

- **Comments**: Use Javadoc for public APIs
- **Documentation**: Document complex logic inline
- **Constants**: Use `final static` for constants
- **Error Handling**: Use specific exceptions, not generic ones
- **Null Safety**: Use `Objects.requireNonNull()` for defensive checks

### Example Class Structure

```java
/**
 * Description of what this class does.
 * Provide context and usage examples if needed.
 */
public class MyFeatureClass {
    
    private static final Logger logger = Logger.getInstance();
    private static final int MAX_ATTEMPTS = 3;
    
    private final String requiredField;
    private String optionalField;
    
    /**
     * Create a new instance of MyFeatureClass.
     * 
     * @param requiredField A required parameter
     * @throws NullPointerException if requiredField is null
     */
    public MyFeatureClass(String requiredField) {
        this.requiredField = Objects.requireNonNull(requiredField);
        logger.info("Instance created");
    }
    
    /**
     * Description of what this method does.
     * 
     * @param input The input parameter
     * @return The return value
     * @throws IOException if IO fails
     */
    public String processInput(String input) throws IOException {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input cannot be null or empty");
        }
        
        // Implementation
        return input;
    }
}
```

### Import Organization

```java
// 1. Java standard library
import java.util.*;
import java.io.*;

// 2. Third-party libraries
import org.json.JSONObject;

// 3. Project classes
import org.simple.rag.*;
import org.simple.rag.rag.*;
```

## Testing

### Writing Tests

- Create test files in `src/test/java/` mirror structure
- Use JUnit 5 for tests
- Follow naming: `ClassName` → `ClassNameTest`
- Test both success and failure cases

### Test Example

```java
public class MyFeatureClassTest {
    
    private MyFeatureClass feature;
    
    @BeforeEach
    public void setUp() {
        feature = new MyFeatureClass("test");
    }
    
    @Test
    public void testProcessInputSuccess() {
        String result = feature.processInput("input");
        assertEquals("expected", result);
    }
    
    @Test
    public void testProcessInputWithNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            feature.processInput(null);
        });
    }
}
```

### Run Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=MyFeatureClassTest

# Run with coverage
mvn clean test jacoco:report

# Skip tests during build
mvn clean install -DskipTests
```

## Commit Guidelines

### Commit Message Format

```
<type>: <subject>

<body>

<footer>
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `refactor`: Code refactoring
- `perf`: Performance improvement
- `docs`: Documentation
- `test`: Add/update tests
- `chore`: Build, CI, dependencies
- `style`: Formatting, missing semicolons, etc.

### Example Commits

```
feat: Add batch embedding generation with async support

- Implement generateEmbeddingsAsync for parallel processing
- Add retry logic with exponential backoff
- Include comprehensive error handling

Closes #123

---

fix: Handle empty query vectors in similarity search

- Add validation for null/empty vectors
- Return empty results instead of throwing exception

Fixes #456
```

### Commit Best Practices

- Keep commits logically independent
- Write clear, descriptive messages
- Reference related issues
- One feature per commit when possible

## Pull Request Process

### Before Submitting PR

1. **Update from main:**
   ```bash
   git fetch origin
   git rebase origin/main
   ```

2. **Run tests locally:**
   ```bash
   mvn clean verify
   ```

3. **Check code style:**
   ```bash
   mvn checkstyle:check
   ```

4. **Build distribution:**
   ```bash
   mvn clean package
   ```

### PR Description Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] New feature
- [ ] Bug fix
- [ ] Performance improvement
- [ ] Documentation update

## Related Issues
Closes #<issue_number>

## Testing
- [ ] Added new tests
- [ ] All tests passing
- [ ] Manual testing completed

## Checklist
- [ ] Code follows style guidelines
- [ ] Javadoc added for public methods
- [ ] Documentation updated
- [ ] No breaking changes
- [ ] Backwards compatible
```

### PR Review Process

1. Code review by maintainers
2. Automated tests must pass
3. Documentation must be complete
4. Feedback addressed
5. Squash and merge (maintainers)

## Architecture Decisions

### When to Propose an ADR (Architecture Decision Record)

- **Large Features**: Submit design doc first
- **API Changes**: Document before implementation
- **Major Refactoring**: Discuss approach in issue
- **Performance Changes**: Include benchmarks

### ADR Template

```markdown
# ADR-001: Title of Decision

## Context
Why are we making this decision?

## Alternatives
What other approaches were considered?

## Decision
What is our decision?

## Consequences
What are the implications?
```

## Documentation

### Areas to Document

- **New Classes/Methods**: Add Javadoc
- **Complex Logic**: Add inline comments
- **Configuration**: Update README if applicable
- **API**: Add to API reference if public
- **Deployment**: Update DEPLOYMENT.md for operational changes

### Documentation Standards

- Keep it clear and concise
- Include examples where helpful
- Link to related documentation
- Update table of contents

## Submission Process

1. **Fork** the repository (if external contributor)
2. **Create** a feature branch: `git checkout -b feature/xyz`
3. **Make** your changes with atomic commits
4. **Test** thoroughly: `mvn clean verify`
5. **Push** to your fork: `git push origin feature/xyz`
6. **Create** a Pull Request to `main` branch
7. **Respond** to code review feedback
8. **Celebrate** when your PR is merged!

## Release Process

These steps are for maintainers:

1. **Update version** in pom.xml
2. **Update CHANGELOG** with new features/fixes
3. **Create release branch**
4. **Build distributions**: `mvn clean package`
5. **Tag release**: `git tag v1.0.0`
6. **Push changes and tags**
7. **Create release notes** on GitHub

## Questions or Need Help?

- Check existing issues and documentation
- Ask in GitHub discussions
- Email the maintainers
- Check community forums

## Areas We Need Help With

- [ ] Performance optimization
- [ ] Docker/Kubernetes implementations
- [ ] Documentation improvements
- [ ] Additional storage backends
- [ ] Web UI development
- [ ] API implementation
- [ ] Model evaluation metrics
- [ ] Example notebooks
- [ ] Integration tests
- [ ] CI/CD improvements

## Recognition

Contributors will be recognized:
- In CONTRIBUTORS.md
- In release notes
- In project documentation

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

## Additional Resources

- [Architecture Overview](./docs/architecture.md) (when available)
- [Performance Tuning](./DEPLOYMENT.md#performance-tuning)
- [API Reference](./README.md#api-reference)
- [Troubleshooting](./DEPLOYMENT.md#troubleshooting)

---

Thank you for contributing! 🎉

**Last Updated**: March 19, 2026
