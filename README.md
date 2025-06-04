# StoryForge 📚✨

**A comprehensive creative writing companion for Android**

StoryForge is a powerful Android application designed for writers who want to organize, create, and manage their literary works with professional-grade tools. Whether you're writing novels, short stories, or screenplays, StoryForge provides everything you need to bring your stories to life.

## 🌟 Features

### 📖 Book & Chapter Management
- **Multi-Book Support**: Organize multiple writing projects simultaneously
- **Chapter Organization**: Structure your books with ordered chapters
- **Rich Text Editor**: Advanced text editing with formatting support (bold, italic, headers, lists, quotes)
- **Auto-Save**: Never lose your work with automatic saving and backup management
- **Chapter Reordering**: Easily reorganize your story structure

### 📤 Export Capabilities
- **Multiple Formats**: Export to TXT, DOCX, and PDF formats
- **Professional DOCX**: Rich formatting with table of contents, page numbering, and proper styling
- **High-Quality PDF**: Professional PDF generation with proper typography and layout
- **Single Chapter or Full Book**: Export individual chapters or complete manuscripts
- **Custom File Naming**: Choose your own export file names

### 👥 Character Management
- **Character Profiles**: Create detailed character information and relationships
- **Character Relationships**: Map connections between characters
- **Character Development**: Track character arcs throughout your story

### ⏰ Timeline & Scene Management
- **Story Timeline**: Organize events chronologically
- **Scene Planning**: Plan and structure individual scenes
- **Story Analytics**: Track your writing progress and productivity

### 💾 Data Management
- **Import/Export System**: Backup and restore your entire library
- **Data Portability**: Move your work between devices
- **Database Migrations**: Seamless updates that preserve your data
- **Search & Filter**: Advanced search capabilities across all content

### 🎨 User Experience
- **Material Design 3**: Modern, beautiful interface following Google's design guidelines
- **Dark/Light Themes**: Comfortable writing in any lighting condition
- **Responsive Layout**: Optimized for tablets and phones
- **Intuitive Navigation**: Easy access to all features

## 🏗️ Technical Architecture

### Built With
- **Kotlin**: 100% Kotlin codebase
- **Jetpack Compose**: Modern declarative UI framework
- **Material Design 3**: Latest Material Design components
- **Android Architecture Components**: MVVM pattern with ViewModel and LiveData

### Dependencies & Libraries
- **Hilt**: Dependency injection for clean architecture
- **Room Database**: Local data persistence with automatic migrations
- **Apache POI**: Professional DOCX document generation
- **Kotlinx Serialization**: JSON serialization for rich text content
- **Navigation Component**: Type-safe navigation between screens

### Architecture Pattern
```
UI Layer (Compose) → ViewModel → Repository → Database (Room)
                                      ↓
                              File System (Exports)
```

## 📱 Screenshots

*Coming soon - Screenshots of the main features will be added here*

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 24+ (Android 7.0)
- Kotlin 1.9.0+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/acocalypso/StoryForge.git
   cd StoryForge
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned repository folder

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run the app**
   ```bash
   ./gradlew installDebug
   ```

### Building from Source

The project uses Gradle build system with Kotlin DSL:

```bash
# Clean build
./gradlew clean

# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test
```

## 📖 Usage Guide

### Creating Your First Book
1. Launch StoryForge
2. Tap the "+" button to create a new book
3. Fill in book details (title, author, description)
4. Start writing your first chapter

### Rich Text Editing
- **Bold**: Use `**text**` or toolbar button
- **Italic**: Use `*text*` or toolbar button
- **Headers**: Use `#`, `##`, `###` for different header levels
- **Lists**: Use `-` for bullet points, `1.` for numbered lists
- **Quotes**: Use `>` for block quotes

### Exporting Your Work
1. Navigate to your book or chapter
2. Tap the export button (📤)
3. Choose your preferred format (TXT, DOCX, PDF)
4. Select export location
5. Your file will be saved to the chosen location

### Character Management
1. Go to the Characters section
2. Add new characters with detailed profiles
3. Define relationships between characters
4. Reference characters while writing

## 🛠️ Development Setup

### Project Structure
```
app/
├── src/main/java/de/astronarren/storyforge/
│   ├── data/                    # Data layer
│   │   ├── database/           # Room database
│   │   ├── repository/         # Data repositories
│   │   └── service/            # Export services
│   ├── domain/                 # Business logic
│   ├── ui/                     # UI layer
│   │   ├── components/         # Reusable UI components
│   │   ├── screens/           # Screen composables
│   │   └── theme/             # App theming
│   └── util/                  # Utility classes
└── schemas/                   # Database schema versions
```

### Key Components

#### Export System
The export system supports three formats:
- **TextExport**: Plain text with basic formatting
- **DocxExport**: Rich formatting using Apache POI
- **PdfExport**: Professional PDF generation with Android's PdfDocument API

#### Database Architecture
- **Room Database**: Local SQLite database with automatic migrations
- **Entities**: Book, Chapter, Character, Timeline, Scene
- **DAOs**: Type-safe database access
- **Migrations**: Automatic schema updates

#### Rich Text System
- **RichTextDocument**: JSON-based rich text format
- **RichTextEditor**: Compose-based rich text editor
- **RichTextFormatter**: Conversion between formats

### Contributing

We welcome contributions! Please follow these guidelines:

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Make your changes**
4. **Add tests** for new functionality
5. **Commit your changes**
   ```bash
   git commit -m "Add amazing feature"
   ```
6. **Push to your branch**
   ```bash
   git push origin feature/amazing-feature
   ```
7. **Open a Pull Request**

### Code Style
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add documentation for public APIs
- Write unit tests for business logic

## 🐛 Known Issues & Roadmap

### Current Limitations
- PDF page numbering is basic (enhancement in progress)
- Export progress indicators could be improved
- Character relationship visualization needs enhancement

### Upcoming Features
- [ ] Cloud synchronization
- [ ] Collaborative writing
- [ ] Writing statistics and goals
- [ ] Theme customization
- [ ] Voice-to-text integration
- [ ] Grammar and spell check integration

## 📋 Requirements

### Minimum Requirements
- **Android Version**: Android 7.0 (API level 24)
- **RAM**: 2GB minimum, 4GB recommended
- **Storage**: 100MB for app, additional space for your books and exports
- **Permissions**: Storage access for file exports

### Recommended Specifications
- **Android Version**: Android 10+ (API level 29)
- **RAM**: 4GB or more
- **Storage**: 1GB+ free space
- **Screen Size**: 5.5" or larger for optimal experience

## 🤝 Support & Community

### Getting Help
- **Issues**: Report bugs or request features via [GitHub Issues](https://github.com/acocalypso/StoryForge/issues)
- **Discussions**: Join our [GitHub Discussions](https://github.com/acocalypso/StoryForge/discussions)
- **Email**: Contact us at storyforge.support@email.com

### Contributing
We welcome contributions from writers, developers, and designers! Check our [Contributing Guidelines](CONTRIBUTING.md) for details.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Apache POI**: For excellent DOCX document generation
- **Material Design**: For beautiful UI components
- **Android Jetpack**: For modern Android development tools
- **Kotlin Team**: For the amazing programming language
- **Our Beta Testers**: For valuable feedback and bug reports

## 📊 Project Status

- **Version**: 1.0.0
- **Status**: Active Development
- **Stability**: Beta
- **Last Updated**: June 2025

---

**Made with ❤️ for writers by writers**

*StoryForge - Where stories come to life*
