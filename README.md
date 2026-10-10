# Liquid Info

Liquid Info is a small collection of independent client-side Fabric mods for Minecraft 1.21.1. Install either mod on its own, or install both for translucent lava and quick in-world fluid information.

## Mods

### ClearLava — 1.0.1+mc1.21.1

- Renders lava translucently.
- Press **F9** to switch between the lava and water fluid textures.
- Press **F4** to toggle the opaque lava render layer.
- Saves preferences in config/clearlava.txt.

### Fluid Levels — 1.1.3+mc1.21.1-prod

- Shows compact labels for water and lava fluid states around the player; source blocks use S.
- Press **F7** to toggle the display.
- Press **F5** to hide flowing-fluid labels in text mode. This does not change wool-marker mode.
- Press **F6** to replace text labels with source markers: red wool for lava and blue wool for water.
- Saves preferences in config/fluidlevels.json.

All keybinds can be changed in Minecraft's Controls menu. F5 is also Minecraft's default perspective key, so rebind either action if needed. Both mods are client-side and do not need to be installed on a server. They are packaged as separate JARs and keep their original mod IDs and configuration paths.

## Requirements

- Minecraft Java Edition 1.21.1
- Java 21
- Fabric Loader **0.18.3 or newer** and Fabric API
- Python 3 and Pillow to generate ClearLava's textures when building from source

## Build

ClearLava's translucent textures are generated locally from a Minecraft 1.21.1 client JAR that you provide. The game textures and client JAR are not stored in this repository. Fluid Levels uses Mojang's official mappings; ClearLava keeps its intermediary mappings. The root Gradle build task builds both mods; Fluid Levels can also be built independently.

Install Python 3 and the pinned image dependency first:

~~~powershell
python -m pip install -r requirements-build.txt
~~~

On Windows, build both mods from PowerShell with a local Minecraft 1.21.1 client JAR:

~~~powershell
.\gradlew.bat clean build -PclientJar="C:\path\to\minecraft-client-1.21.1.jar"
~~~

On macOS or Linux:

~~~sh
python3 -m pip install -r requirements-build.txt
./gradlew clean build -Ppython=python3 -PclientJar=/path/to/minecraft-client-1.21.1.jar
~~~

To build only Fluid Levels, run :fluid-levels:build without the clientJar property. The root build task explicitly includes both mods. Gradle resolves Fabric Loom, Minecraft mappings, Loader, and API dependencies during the build.

The two mod JARs are written to build/libs/ and fluid-levels/build/libs/. Install the JAR or JARs you want in the mods folder of a Fabric 1.21.1 client profile with Fabric API installed.

## Licensing and compatibility

The original ClearLava component has no open-source license grant in this repository. The Fluid Levels component's original Fabric metadata declares MIT and names FluidLevels as its author; its license is scoped to that component in the [fluid-levels/LICENSE](fluid-levels/LICENSE) file. See [third-party notices](THIRD_PARTY_NOTICES.md) for dependencies and Minecraft assets.

Liquid Info is an unofficial community project and is not affiliated with or endorsed by Mojang Studios or Microsoft. Compatibility with other rendering mods depends on their versions and configuration.
