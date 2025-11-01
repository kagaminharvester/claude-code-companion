# Claude Code Companion

An Android companion app for Claude Code that enhances your development workflow with a beautiful, intuitive GUI.

## Features

### Task Management
- View all tasks from your Claude Code sessions
- Track task status (Pending, In Progress, Completed)
- Add, update, and delete tasks
- Filter tasks by status
- Real-time task synchronization

### Agent Quick Access
- Quick launch for specialized Claude Code agents:
  - **Explore Agent** - Fast codebase exploration and searching
  - **Plan Agent** - Create implementation plans for complex tasks
  - **General Purpose Agent** - Multi-step autonomous task handling
  - **Code Reviewer** - Review code for issues and improvements
- Detailed agent descriptions and usage tips
- Custom task prompts for each agent

### Command Palette
- Pre-configured common commands (Git, Build, Test, etc.)
- Search and filter commands by category
- Favorite commands for quick access
- Execute commands remotely via SSH
- View command output in real-time
- Add custom commands

### Workflow Templates
- Pre-built workflow sequences:
  - **Commit & Push** - Stage, commit, and push changes
  - **Build & Test** - Install dependencies, build, and test
  - **Pull & Install** - Pull latest changes and install dependencies
- Step-by-step workflow visualization
- One-click workflow execution
- Create custom workflows

### SSH Connection
- Secure SSH connection to your development machine
- Connection status monitoring
- Persistent connection management
- Execute commands remotely
- Real-time command output

## Screenshots

[Dashboard] [Tasks] [Agents] [Commands] [Workflows]

## Installation

### Prerequisites
- Android device running Android 7.0 (API level 24) or higher
- SSH access to your development machine
- Claude Code installed on your development machine

### Building from Source

1. Clone the repository:
```bash
git clone https://github.com/yourusername/claude-code-companion.git
cd claude-code-companion
```

2. Open the project in Android Studio

3. Build and run the app on your device or emulator

### Installing the APK

1. Download the latest APK from the [Releases](https://github.com/yourusername/claude-code-companion/releases) page
2. Enable "Install from Unknown Sources" on your Android device
3. Install the APK

## Configuration

1. Open the app and navigate to Settings
2. Enter your SSH connection details:
   - **Host**: IP address or hostname of your development machine
   - **Port**: SSH port (default: 22)
   - **Username**: Your SSH username
   - **Password**: Your SSH password
3. Tap "Save & Connect"

## Usage

### Connecting to Claude Code

1. Ensure Claude Code is running on your development machine
2. Configure SSH connection in Settings
3. Tap "Connect" on the Dashboard
4. Once connected, you can execute commands, launch agents, and manage tasks

### Managing Tasks

1. Navigate to the Tasks tab
2. Tap the + button to add a new task
3. Enter task description and active form
4. Update task status by tapping on a task and selecting an action
5. Filter tasks using the status chips

### Using Agents

1. Navigate to the Agents tab
2. Tap on an agent card to see details
3. Enter a task description for the agent
4. Tap "Execute" to launch the agent

### Executing Commands

1. Navigate to the Commands tab
2. Browse or search for a command
3. Tap the "Run" button to execute
4. View output in the dialog
5. Star commands to add them to favorites

### Running Workflows

1. Navigate to the Workflows tab
2. Expand a workflow to see its steps
3. Tap "Execute Workflow" to run all steps in sequence

## Architecture

### Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **SSH Library**: JSch
- **Async Operations**: Kotlin Coroutines & Flow
- **Navigation**: Jetpack Navigation Compose

### Project Structure
```
app/
├── src/main/
│   ├── java/com/claude/codecompanion/
│   │   ├── data/           # Data models and entities
│   │   ├── ssh/            # SSH connection management
│   │   ├── viewmodel/      # ViewModels for UI state
│   │   ├── ui/
│   │   │   ├── screens/    # Composable screens
│   │   │   └── theme/      # App theming
│   │   └── MainActivity.kt
│   └── res/                # Resources (strings, themes, etc.)
└── build.gradle.kts
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Security Notes

- SSH passwords are stored in memory only and are not persisted
- Consider using SSH key authentication for better security
- Always use secure connections when connecting to remote machines
- The app requires INTERNET and NETWORK_STATE permissions

## Future Enhancements

- [ ] SSH key-based authentication
- [ ] WebSocket support for real-time updates
- [ ] File browser for remote file access
- [ ] Code editor integration
- [ ] Dark/Light theme toggle
- [ ] Custom agent creation
- [ ] Workflow builder
- [ ] Task import/export
- [ ] Multi-device sync
- [ ] Notifications for task updates

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Built for [Claude Code](https://docs.claude.com/claude-code) by Anthropic
- Uses Material Design 3 components
- SSH connectivity via JSch library

## Support

For issues, questions, or suggestions, please open an issue on GitHub.

---

Made with ❤️ for the Claude Code community
