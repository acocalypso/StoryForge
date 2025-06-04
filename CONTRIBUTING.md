# Contributing to StoryForge 🤝

Thank you for your interest in contributing to StoryForge! We welcome contributions from writers, developers, designers, and anyone passionate about improving tools for creative writing.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Submitting Changes](#submitting-changes)
- [Issue Guidelines](#issue-guidelines)
- [Documentation](#documentation)
- [Community](#community)

## 📜 Code of Conduct

By participating in this project, you agree to abide by our Code of Conduct:

- **Be respectful**: Treat all contributors with respect and kindness
- **Be inclusive**: Welcome newcomers and help them get started
- **Be constructive**: Provide helpful feedback and suggestions
- **Be patient**: Remember that everyone has different skill levels and backgrounds
- **Be collaborative**: Work together to improve the project

## 🚀 Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:

- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: OpenJDK 11 or later
- **Android SDK**: API level 24 or higher
- **Git**: For version control
- **Kotlin**: 1.9.0+ (bundled with Android Studio)

### Fork and Clone

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```powershell
   git clone https://github.com/acocalypso/StoryForge.git
   cd StoryForge
   ```
3. **Add the upstream remote**:
   ```powershell
   git remote add upstream https://github.com/acocalypso/StoryForge.git
   ```

## 🛠️ Development Setup

### 1. Open in Android Studio

1. Launch Android Studio
2. Select "Open an existing project"
3. Navigate to your cloned StoryForge folder
4. Wait for Gradle sync to complete

### 2. Build the Project

```powershell
# Clean build
./gradlew clean

# Build debug version
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run Android tests
./gradlew connectedAndroidTest
```

### 3. Run the App

You can run the app using:
- Android Studio's Run button (▶️)
- VS Code task: "Run StoryForge App"
- Command line: `./gradlew installDebug`

### 4. Database Schema

When making changes to database entities, you may need to create a migration:

1. Update the database version in `StoryForgeDatabase.kt`
2. Create a migration in the `migrations` package
3. Add the migration to the database builder
4. Test the migration thoroughly

## 🎯 How to Contribute

### Types of Contributions

We welcome various types of contributions:

- **🐛 Bug Fixes**: Fix issues and improve stability
- **✨ New Features**: Add new functionality for writers
- **📚 Documentation**: Improve guides, comments, and README
- **🎨 UI/UX**: Enhance the user interface and experience
- **🧪 Tests**: Add or improve test coverage
- **🔧 Refactoring**: Improve code quality and architecture
- **🌐 Translations**: Add support for new languages
- **📖 Content**: Help with example content or tutorials

### Areas We Need Help With

#### High Priority
- **Export Enhancements**: Improve PDF generation and DOCX formatting
- **UI Polish**: Enhance Material Design 3 implementation
- **Performance**: Optimize large document handling
- **Accessibility**: Improve app accessibility features

#### Medium Priority
- **Character Relationships**: Visual relationship mapping
- **Timeline Visualization**: Interactive timeline interface
- **Writing Analytics**: Advanced progress tracking
- **Import Formats**: Support for more file formats

#### Low Priority
- **Cloud Sync**: Integration with cloud storage
- **Collaboration**: Multi-user editing features
- **Plugins**: Extension system for custom features

## 📝 Coding Standards

### Kotlin Style Guide

Follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html):

```kotlin
// Good
class BookRepository @Inject constructor(
    private val bookDao: BookDao,
    private val exportService: ExportService
) {
    suspend fun saveBook(book: Book): Long {
        return bookDao.insertBook(book)
    }
}

// Bad
class BookRepository @Inject constructor(private val bookDao:BookDao,private val exportService:ExportService){
suspend fun saveBook(book:Book):Long{
return bookDao.insertBook(book)
}
}
```

### Architecture Guidelines

1. **MVVM Pattern**: Use ViewModel for UI logic, Repository for data access
2. **Dependency Injection**: Use Hilt for dependency management
3. **Compose**: Use Jetpack Compose for UI components
4. **Single Responsibility**: Each class should have one reason to change

### File Organization

```
app/src/main/java/de/astronarren/storyforge/
├── data/
│   ├── database/          # Room entities, DAOs, database
│   ├── repository/        # Data repositories
│   └── service/          # Business services (export, import)
├── domain/               # Business models and use cases
├── ui/
│   ├── components/       # Reusable Compose components
│   ├── screens/         # Screen-specific composables
│   └── theme/           # App theming
└── util/                # Utility classes and extensions
```

### Naming Conventions

- **Classes**: PascalCase (`BookDetailViewModel`)
- **Functions**: camelCase (`saveBookToDatabase`)
- **Variables**: camelCase (`currentBook`)
- **Constants**: SCREAMING_SNAKE_CASE (`MAX_CHAPTER_LENGTH`)
- **Resources**: snake_case (`book_detail_screen`)

### Code Documentation

```kotlin
/**
 * Exports a book to the specified format.
 * 
 * @param book The book to export
 * @param format The export format (TXT, DOCX, PDF)
 * @param outputPath The destination file path
 * @return Result indicating success or failure
 */
suspend fun exportBook(
    book: Book,
    format: ExportFormat,
    outputPath: String
): Result<String>
```

## 🧪 Testing Guidelines

### Unit Tests

Write unit tests for:
- ViewModels
- Repositories
- Business logic
- Utility functions

```kotlin
@Test
fun `saveBook should return book ID on success`() = runTest {
    // Given
    val book = Book(title = "Test Book", author = "Test Author")
    coEvery { bookDao.insertBook(book) } returns 1L
    
    // When
    val result = repository.saveBook(book)
    
    // Then
    assertEquals(1L, result)
}
```

### Integration Tests

Test database operations and complex workflows:

```kotlin
@Test
fun `database migration from version 4 to 5 preserves data`() {
    // Test database migrations
}
```

### UI Tests

Test critical user flows:

```kotlin
@Test
fun `creating new book navigates to book detail screen`() {
    // Test UI navigation and state changes
}
```

### Running Tests

```powershell
# Run all unit tests
./gradlew test

# Run tests for specific module
./gradlew :app:testDebugUnitTest

# Run instrumented tests
./gradlew connectedAndroidTest

# Generate test coverage report
./gradlew jacocoTestReport
```

## 📤 Submitting Changes

### Branch Strategy

1. **Create a feature branch** from `main`:
   ```powershell
   git checkout main
   git pull upstream main
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes** in small, logical commits

3. **Write descriptive commit messages**:
   ```
   feat: add PDF page numbering enhancement
   
   - Implement proper page numbering for PDF exports
   - Add header/footer customization options
   - Update export dialog to include page number settings
   
   Fixes #123
   ```

### Commit Message Format

Use the [Conventional Commits](https://www.conventionalcommits.org/) format:

- `feat:` New features
- `fix:` Bug fixes
- `docs:` Documentation changes
- `style:` Code style changes (formatting, etc.)
- `refactor:` Code refactoring
- `test:` Adding or updating tests
- `chore:` Maintenance tasks

### Pull Request Process

1. **Update your branch** with the latest changes:
   ```powershell
   git fetch upstream
   git rebase upstream/main
   ```

2. **Push your changes**:
   ```powershell
   git push origin feature/your-feature-name
   ```

3. **Create a Pull Request** on GitHub with:
   - Clear title and description
   - Screenshots for UI changes
   - Test results
   - Breaking changes (if any)

4. **Address review feedback** promptly

### Pull Request Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing completed

## Screenshots (if applicable)
[Add screenshots here]

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] Tests added/updated
```

## 🐛 Issue Guidelines

### Reporting Bugs

Use the bug report template:

```markdown
## Bug Description
Clear description of the bug

## Steps to Reproduce
1. Go to '...'
2. Click on '....'
3. Scroll down to '....'
4. See error

## Expected Behavior
What should happen

## Actual Behavior
What actually happens

## Environment
- Device: [e.g. Pixel 6]
- Android Version: [e.g. Android 12]
- App Version: [e.g. 1.0.0]

## Screenshots
[Add screenshots if applicable]
```

### Feature Requests

Use the feature request template:

```markdown
## Feature Description
Clear description of the proposed feature

## Problem Statement
What problem does this solve?

## Proposed Solution
How should this feature work?

## Alternatives Considered
Other solutions you've considered

## Additional Context
Any other relevant information
```

## 📚 Documentation

### Code Comments

- Document complex algorithms
- Explain business logic
- Add TODO comments for future improvements
- Document public APIs

### README Updates

When adding new features:
1. Update the features section
2. Add usage examples
3. Update screenshots if needed
4. Update the roadmap

### Wiki Contributions

Help improve our documentation:
- User guides
- Developer tutorials
- Architecture documentation
- Troubleshooting guides

## 🌟 Recognition

### Contributors

All contributors will be recognized in:
- README.md acknowledgments
- Release notes
- GitHub contributors list

### Types of Recognition

- **First-time contributors**: Welcome badge
- **Regular contributors**: Special mention in releases
- **Major contributors**: Collaborator status
- **Exceptional contributions**: Hall of fame

## 💬 Community

### Getting Help

- **GitHub Discussions**: For questions and general discussion
- **GitHub Issues**: For bug reports and feature requests
- **Discord**: [Coming soon] Real-time chat with other contributors

### Mentorship

New contributors can get help from experienced maintainers:
- Code review guidance
- Architecture discussions
- Best practices
- Career advice

## 🔄 Development Workflow

### Typical Workflow

1. **Check Issues**: Look for good first issues or areas needing help
2. **Discuss**: Comment on issues to discuss approach
3. **Code**: Implement changes following guidelines
4. **Test**: Ensure all tests pass
5. **Document**: Update relevant documentation
6. **Submit**: Create pull request with clear description
7. **Iterate**: Address review feedback
8. **Merge**: Celebrate when merged! 🎉

### Release Cycle

- **Major releases**: Every 3-4 months
- **Minor releases**: Monthly
- **Patch releases**: As needed for critical fixes

## 🙏 Thank You

Every contribution, no matter how small, makes StoryForge better for writers everywhere. Whether you're fixing a typo, adding a feature, or helping other contributors, your efforts are valued and appreciated.

Happy coding! 📝✨

---

**Questions?** Feel free to reach out via GitHub Issues or Discussions.

**First time contributing to open source?** Check out [First Contributions](https://github.com/firstcontributions/first-contributions) for guidance.
