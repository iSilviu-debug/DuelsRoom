# DuelsRoom

**DuelsRoom** is a Minecraft plugin that allows players to engage in duels within custom-shaped regions.

## Features

- Duel a player when entering a custom region.
- Customizable duel areas: rectangle, triangle, cuboid, etc.
- Block border traps players inside until one dies or leaves.
- Integrated combat system to prevent escape:
  - Choose whether combat starts on room entry or first hit.
- Automatic room closure when a player leaves.
- Detects and stops duels if a player glitches into blocks.
- Win/loss announcements in chat.

## Requirements

- **WorldGuard** and **WorldEdit** plugins must be installed.
- Java 21
- Minecraft server running Spigot or Paper.

## Installation

1. Download the latest version of the plugin.
2. Place the `.jar` file into your server's `plugins` folder.
3. Start your server to generate the configuration files.
4. Configure your duel regions and settings using the provided configuration options.

## Building from Source

To build DuelsRoom from the source code, you'll need to use **Gradle**. Follow the instructions below to set up and compile the plugin.

### Prerequisites

- **Java Development Kit (JDK) 21+**
- **Gradle** (or use the included `gradlew` wrapper)

### Steps to Build

1. Clone the repository:
   ```bash
   git clone https://github.com/iSilviu-debug/DuelsRoom.git
2. Navigate to the project directory:
   ```bash
   cd DuelsRoom
3. Run the Gradle build command:
   ```bash
   ./gradlew build
4. After the build completes, the .jar file can be found in the build/libs/ directory. This file is the plugin you can install on your Minecraft server.

[LICENSE](LICENSE)
