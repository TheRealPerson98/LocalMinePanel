# LocalMinePanel

A clean, modern Minecraft server management tool for developers (currently in early alpha development).

[![License: GPL-3.0](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Java Version](https://img.shields.io/badge/Java-17%2B-blue)](https://www.oracle.com/java/)

> ⚠️ **Note:** This project is in very early development and many features are not yet implemented or fully functional.

## 🚧 Current Status

- Basic UI framework implemented
- Core functionality still under development
- Limited feature set currently working

## 🔜 Upcoming Features

- 🎨 Multiple themes and customizable UI
- 📦 Easy server template importing system
- 🚀 Core functionality improvements:
  - Server creation and management
  - Console integration
  - File management
  - Performance monitoring
- 🛠️ Plugin and mod management tools
- 💾 Backup and restore functionality

## ✨ Features

- 🚀 Quick server creation with pre-configured templates
- 📁 File management system for easy access to server files
- 💻 Built-in console with command history
- 🔄 Server performance monitoring
- 🎮 Multiple server type support (Paper, Spigot, etc.)
- 🛠 Customizable startup parameters
- 📊 Resource usage monitoring
- 🔌 Plugin and mod management

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- At least 2GB of available RAM (recommended: 4GB+)
- Windows/Linux/MacOS

### Installation

1. Download the latest release from the [releases page](https://github.com/TheRealPerson98/LocalMinePanel/releases)
2. Double-click the downloaded `LocalMinePanel.jar` file to run the application

If double-clicking doesn't work, you can also run it from the command line:
```bash
java -jar LocalMinePanel.jar
```

> **Note:** Make sure you have Java 17 or higher installed on your system. You can download it from [Oracle](https://www.oracle.com/java/technologies/downloads/#java17) or [Amazon Corretto](https://aws.amazon.com/corretto/).

### Building from Source

```bash
# Clone the repository
git clone https://github.com/TheRealPerson98/LocalMinePanel.git

# Navigate to the project directory
cd LocalMinePanel

# Build with Gradle
./gradlew build

# Run the application
./gradlew run
```

## 📖 Usage

### Creating a New Server
1. Click the "+" button
2. Select server type (Paper, Spigot, etc.)
3. Configure basic settings
4. Click "Create"

### Managing Servers
- Start/Stop/Restart servers with one click
- Monitor console output in real-time
- Execute commands directly from the GUI
- Manage server files through the built-in file browser

### Configuration
- Edit server.properties through the GUI
- Customize Java arguments
- Configure memory allocation
- Set up automatic backups

## 🛠 Development

### Tech Stack
- JavaFX for the GUI
- Jackson for JSON processing
- Lombok for reducing boilerplate
- Gradle for build management

### Project Structure
```
src/main/java/com/person98/localminepanel/
├── application/    # UI-related code
│   ├── controllers/    # JavaFX controllers
│   └── views/         # Custom JavaFX views
├── core/          # Core business logic
├── services/      # Supporting services
│   ├── file/         # File management
│   ├── installer/    # Server installation
│   └── template/     # Server templates
└── utils/         # Utility classes
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## 👏 Acknowledgments

- [PaperMC](https://papermc.io/) for their amazing server software
- [JavaFX](https://openjfx.io/) for the GUI framework
- The Minecraft server community

## 💬 Support

Need help? Here's what to do:

1. Check the [FAQ](docs/FAQ.md)
2. Search existing [issues](https://github.com/TheRealPerson98/LocalMinePanel/issues)
3. Create a new issue if needed

## ⚠️ Known Limitations

Currently, many features shown in the documentation are planned but not yet implemented. The application is in active development, and breaking changes may occur frequently.

## 🗺️ Roadmap

1. **Phase 1** (Current)
   - Basic UI implementation
   - Core server management functionality

2. **Phase 2**
   - Theme system implementation
   - Template management
   - Basic server controls

3. **Phase 3**
   - Advanced features
   - Plugin management
   - Performance optimization

## 📝 TODO List

### High Priority
- 🔧 Fix memory and CPU usage monitoring
- 📂 Implement live file system updates
- 🎨 Add syntax highlighting and colors to console output
- 🚪 Fix port configuration not updating properly
- ⚙️ Allow server.properties to generate naturally instead of pre-creating

### Improvements
- 📝 Move file editor to separate popup window
  - Add syntax highlighting based on file type
  - Support different themes for different file types
  - Add basic IDE features (line numbers, search/replace)
- 💻 Console Enhancements
  - Add command history navigation (up/down arrows)
  - Add command auto-completion
  - Add clickable links in console output
  - Add timestamp toggle

### Future Features
- 📊 Enhanced Performance Monitoring
  - Graphical display of resource usage
  - Historical data tracking
  - Alert system for resource thresholds
- 🔄 Automatic Backup System
  - Scheduled backups
  - Backup rotation
  - Cloud backup integration
- 🌐 Plugin/Mod Management
  - Direct installation from popular platforms
  - Update checking
  - Dependency management

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/TheRealPerson98">Person98</a>
</p>