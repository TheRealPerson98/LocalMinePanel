# LocalMinePanel

A clean, modern Minecraft server management tool for developers. Easily manage multiple local Minecraft servers with a simple GUI interface.

<p align="center">
  <img src="screenshot.png" alt="LocalMinePanel Screenshot" width="800">
</p>

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-17%2B-blue)](https://www.oracle.com/java/)

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

1. Download the latest release from the [releases page](https://github.com/yourusername/LocalMinePanel/releases)
2. Extract the archive to your preferred location
3. Run the application:
   ```bash
   java -jar LocalMinePanel.jar
   ```

### Building from Source

```bash
# Clone the repository
git clone https://github.com/yourusername/LocalMinePanel.git

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

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👏 Acknowledgments

- [PaperMC](https://papermc.io/) for their amazing server software
- [JavaFX](https://openjfx.io/) for the GUI framework
- The Minecraft server community

## 💬 Support

Need help? Here's what to do:

1. Check the [FAQ](docs/FAQ.md)
2. Search existing [issues](https://github.com/TheRealPerson98/LocalMinePanel/issues)
3. Create a new issue if needed

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/TheRealPerson98">Person98</a>
</p>