# ClearLava

A client-side Fabric mod for Minecraft 1.21.1 that makes lava translucent and lets you swap its fluid texture while you play.

## Features

- Translucent lava rendering, with an option to restore the vanilla opaque render layer.
- Press **F9** to switch between the lava and water fluid textures.
- Press **F4** to toggle opaque lava on or off.
- Saves both choices in `config/clearlava.txt`.

The controls can be rebound in Minecraft's Controls menu. ClearLava runs on the client and does not need to be installed on a server.

## Requirements

- Minecraft Java Edition **1.21.1**
- Java **21**
- Fabric Loader **0.15.0 or newer**
- Fabric API
- Python 3 and Pillow, only when building from source

## Build

The mod replaces Minecraft's lava texture resources with translucent variants. Those variants are generated locally from the Minecraft client JAR you provide; the game textures are not stored in this repository.

On Windows, from PowerShell:

```powershell
.\gradlew.bat build -PclientJar="C:\path\to\minecraft-client-1.21.1.jar"
```

On macOS or Linux:

```sh
./gradlew build -PclientJar=/path/to/minecraft-client-1.21.1.jar
```

The client JAR must be for Minecraft 1.21.1 and contain `assets/minecraft/textures/block/lava_still.png` and `lava_flow.png`. Install Python 3 and the pinned image dependency first (`python -m pip install -r requirements-build.txt`). The build uses Fabric Loom to fetch the modding dependencies.

The output is written to `build/libs/`. Copy the ClearLava JAR into the `mods` folder of a Fabric 1.21.1 profile that also has Fabric API installed.

## Notes

- ClearLava is an unofficial community project. It is not affiliated with or endorsed by Mojang Studios or Microsoft.
- No open-source license is granted by this repository. All rights are reserved unless the author gives permission. Third-party tools and game assets remain subject to their own terms; see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
- Compatibility with other rendering mods depends on their versions and configuration.
