# Deployment Guide

## Deploying to GitHub

Since the app is complete and committed locally, here's how to push it to GitHub:

### Option 1: Using GitHub CLI (gh)

```bash
# Install GitHub CLI if not already installed
# On Raspberry Pi:
curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null
sudo apt update
sudo apt install gh

# Authenticate
gh auth login

# Create repo and push
cd /home/pi/claude-code-companion
gh repo create claude-code-companion --public --source=. --remote=origin --push
```

### Option 2: Manual GitHub Setup

1. **Create Repository on GitHub:**
   - Go to https://github.com/new
   - Repository name: `claude-code-companion`
   - Description: `Android companion app for Claude Code - Enhance your workflow with task management, agent quick access, and command execution`
   - Make it Public
   - Don't initialize with README (we already have one)
   - Click "Create repository"

2. **Push Local Code:**
```bash
cd /home/pi/claude-code-companion
git remote add origin https://github.com/YOUR_USERNAME/claude-code-companion.git
git branch -M main
git push -u origin main
```

### Option 3: Using GitHub Personal Access Token

```bash
cd /home/pi/claude-code-companion

# Create remote with token embedded (replace YOUR_TOKEN and YOUR_USERNAME)
git remote add origin https://YOUR_TOKEN@github.com/YOUR_USERNAME/claude-code-companion.git

# Push
git branch -M main
git push -u origin main
```

## Building the APK

Once pushed to GitHub, you can build the APK:

### Using Android Studio

1. Clone the repository
2. Open in Android Studio
3. Let Gradle sync complete
4. Build → Generate Signed Bundle / APK
5. Choose APK
6. Create a new keystore or use existing
7. Build release APK

### Using Command Line (requires Android SDK)

```bash
# Clone the repo
git clone https://github.com/YOUR_USERNAME/claude-code-companion.git
cd claude-code-companion

# Build debug APK (for testing)
./gradlew assembleDebug

# Build release APK (for distribution)
./gradlew assembleRelease

# APK will be in: app/build/outputs/apk/
```

## Creating a Release

After building the APK:

1. Go to your GitHub repository
2. Click "Releases" → "Create a new release"
3. Tag version: `v1.0.0`
4. Release title: `Claude Code Companion v1.0.0`
5. Upload the APK file
6. Write release notes
7. Publish release

## Current Status

The complete Android app has been created with:
- ✅ Full Jetpack Compose UI
- ✅ Task management system
- ✅ Agent quick access panel
- ✅ Command palette with favorites
- ✅ Workflow automation
- ✅ SSH connection management
- ✅ Material Design 3 theming
- ✅ MVVM architecture
- ✅ Complete documentation (README.md)
- ✅ Git repository initialized and committed

**Ready to push to GitHub!**

## Next Steps

1. Choose one of the deployment options above
2. Push the code to GitHub
3. Build the APK using Android Studio or Gradle
4. Create a GitHub release with the APK
5. Share with the community!

## Repository Information

- **Local Path:** `/home/pi/claude-code-companion`
- **Git Status:** Committed (ready to push)
- **Files:** 23 files, 2456 insertions
- **Commit:** Initial commit: Claude Code Companion Android app
