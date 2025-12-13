# Documentation Index

This directory contains detailed documentation for the Simple Home Assistant Android app.

---

## 📚 Main Documentation

### Configuration & Setup

- **[CONFIG_FILE_SETUP.md](CONFIG_FILE_SETUP.md)** - Complete guide to secure configuration
  management
    - How to use `default_config.json`
    - Git safety and security
    - Setup for team members

- **[QUICK_START_CONFIG.md](QUICK_START_CONFIG.md)** - Quick reference for configuration setup

- **[GIT_SAFE_FILES.md](GIT_SAFE_FILES.md)** - What to commit and what to exclude

### Testing

- **[TESTING_GUIDE.md](TESTING_GUIDE.md)** - Comprehensive testing documentation
    - Types of tests (unit, instrumented, UI)
    - How to write tests
    - Running tests

- **[COMPREHENSIVE_TEST_SUITE.md](COMPREHENSIVE_TEST_SUITE.md)** - Overview of the test suite (35+
  tests)

- **[TESTING_SETUP_SUMMARY.md](TESTING_SETUP_SUMMARY.md)** - Quick setup for testing

- **[RUN_TESTS_NOW.md](RUN_TESTS_NOW.md)** - Quick start guide for running tests

### Tablet Support

- **[CREATE_TABLET_EMULATOR.md](CREATE_TABLET_EMULATOR.md)** - Step-by-step tablet emulator setup
    - Creating Pixel Tablet AVD
    - Testing tablet-specific features

- **[TABLET_SUPPORT.md](TABLET_SUPPORT.md)** - Tablet optimization details
    - Responsive dimensions
    - Layout differences
    - Testing guidelines

### Features & Guides

- **[TABS_COMPLETE_GUIDE.md](TABS_COMPLETE_GUIDE.md)** - Complete guide to custom tabs feature
    - Creating tabs
    - Assigning entities
    - Managing tabs

- **[TABS_FEATURE.md](TABS_FEATURE.md)** - Tab feature overview

- **[GETTING_STARTED.md](GETTING_STARTED.md)** - Initial setup guide

- **[QUICK_START_GUIDE.md](QUICK_START_GUIDE.md)** - Quick reference

- **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** - Command cheat sheet

### Architecture & Implementation

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Detailed architecture documentation
    - MVVM pattern
    - Repository pattern
    - Database schema
    - Data flow diagrams

- **[CUSTOM_TABS_IMPLEMENTATION_PLAN.md](CUSTOM_TABS_IMPLEMENTATION_PLAN.md)** - Implementation plan
  for tabs feature

### Network & Deployment

- **[EMULATOR_NETWORKING_GUIDE.md](EMULATOR_NETWORKING_GUIDE.md)** - Setting up emulator networking
    - Accessing local Home Assistant
    - Network configuration

- **[INSTALL_ADB.md](INSTALL_ADB.md)** - ADB installation and usage

---

## 📦 Archive

Historical documentation about bug fixes, feature implementations, and development progress is
stored in **[archive/](archive/)**.

### What's in the Archive

The archive contains chronological documentation of:

- Bug fixes and their solutions
- Feature implementation steps
- Development progress updates
- Status reports
- Troubleshooting guides for specific issues

**Files in archive** (45+ documents):

- ANR fixes
- Entity selection fixes
- Tab switching improvements
- Flicker-free update implementation
- Network fixes
- State refresh optimizations
- Configuration restoration
- And many more...

These are kept for historical reference and to understand how the app evolved.

---

## 🖼️ Images

Screenshots and diagrams are stored in **[images/](images/)**:

- `phone-dashboard.png` - Phone dashboard view
- `tablet-dashboard.png` - Tablet dashboard view
- Future: Architecture diagrams, feature screenshots

---

## 📖 Quick Links

### Getting Started

1. Read the main [README.md](../README.md) in the project root
2. Set up your configuration: [CONFIG_FILE_SETUP.md](CONFIG_FILE_SETUP.md)
3. Create a tablet emulator: [CREATE_TABLET_EMULATOR.md](CREATE_TABLET_EMULATOR.md)
4. Learn about tabs: [TABS_COMPLETE_GUIDE.md](TABS_COMPLETE_GUIDE.md)

### Development

1. Understand the architecture: [ARCHITECTURE.md](ARCHITECTURE.md)
2. Set up testing: [TESTING_GUIDE.md](TESTING_GUIDE.md)
3. Configure networking: [EMULATOR_NETWORKING_GUIDE.md](EMULATOR_NETWORKING_GUIDE.md)

### Reference

- [QUICK_REFERENCE.md](QUICK_REFERENCE.md) - Command cheat sheet
- [GIT_SAFE_FILES.md](GIT_SAFE_FILES.md) - Git safety checklist

---

## 📝 Documentation Guidelines

When adding new documentation:

### Where to Put It

**Main docs/** folder:

- Feature guides (how to use)
- Setup instructions
- Architecture explanations
- Reference materials

**docs/archive/** folder:

- Bug fix reports
- Status updates
- Implementation progress
- Troubleshooting for specific issues
- Historical development notes

### Naming Conventions

- Use UPPERCASE with underscores: `FEATURE_NAME.md`
- Be descriptive: `CUSTOM_TABS_IMPLEMENTATION.md` not `TABS.md`
- Include purpose: `TESTING_GUIDE.md` not just `TESTS.md`

### Content Guidelines

- Start with status and date
- Use clear headings
- Include code examples
- Add step-by-step instructions
- Link to related docs
- Keep it concise but complete

---

## 🔍 Search Tips

**Looking for specific topics?**

- **Configuration**: CONFIG_FILE_SETUP.md, QUICK_START_CONFIG.md
- **Testing**: TESTING_GUIDE.md, COMPREHENSIVE_TEST_SUITE.md
- **Tablets**: CREATE_TABLET_EMULATOR.md, TABLET_SUPPORT.md
- **Tabs Feature**: TABS_COMPLETE_GUIDE.md, TABS_FEATURE.md
- **Architecture**: ARCHITECTURE.md
- **Networking**: EMULATOR_NETWORKING_GUIDE.md
- **Git Safety**: GIT_SAFE_FILES.md
- **Quick Commands**: QUICK_REFERENCE.md

**Looking for historical info?**

- Check `archive/` folder for bug fixes and implementation details

---

## 🆘 Need Help?

1. Check the main [README.md](../README.md) - comprehensive overview
2. Look through relevant guides in this folder
3. Search the archive for specific issues
4. Check Home Assistant community forums

---

<div align="center">

**For the latest information, always refer to the main [README.md](../README.md)**

[⬆ Back to Project Root](../)

</div>
