# Vortex Client — Anarchy edition (migration in progress)

This repository now contains the extracted Vortex source, a visible **For Anarchy Servers** title-screen notice, and GitHub Actions build/config/graphical-menu smoke tests. Corz migration is **not complete**: see [the full module inventory](docs/CORZ-MIGRATION.md). The graphical smoke test renders menus in a real Fabric client but does not enter a world or validate multiplayer behavior. Check Actions for actual run results; configuration alone is not a passing test.

Build with JDK 21 and Gradle 9.2.1: `gradle build`. Test menu rendering with `gradle runClient -PvortexCiSmoke` (requires a graphical display; exits automatically after rendering). Normal launches never activate that test path. CI uploads JARs and logs as artifacts. The totem counter includes the offhand.

## Initial utility additions

Two opt-in modules have independent implementations (not copied Corz code):
- **Auto Tool** selects a strictly faster hotbar item while holding attack over a breakable block. Ties retain the selected slot. It does not move inventory items, account for all enchantment/harvest rules, or restore the previous slot.
- **Weather Notifier** writes a local chat message when rain/thunder changes. It suppresses the initial state on joining a world and sends no server chat.

These are not claims of full Corz parity. CI tests tool-selection logic and menu startup; world interaction remains unverified.

The original source-package notes below describe the baseline and are retained for context; their untested status predates the Actions workflow.

---

# Vortex Client: source phase 0.3

**Status: unfinished and unverified.** This is an early source-code phase, not the finished client in the full request. The Minecraft-dependent code has NOT been compiled, launched, or tested in-game. There is no compiled JAR in this ZIP. The GUI was adapted from the supplied Spectre screenshots, not from the original Spectre source, and has not been visually checked in Minecraft.

## What's implemented in source

- Right Shift opens the dark-purple/cyan ClickGUI while in a world. The reference-inspired layout has a top header, left category list, central module cards and search, and a right-side module details panel. Click or right-click a card to select it; click its switch or the right-side button to toggle it. Use the footer arrows for pages. Only categories containing implemented modules are shown. The original phase-0.1 screen source remains in the project as unused reference code.
- Fourteen informational HUD toggles: FPS Counter, Ping Display, Coordinates, Direction HUD, Clock, Speed Display, World Time, Biome Display, Sprint Indicator, Session Timer, Memory Usage, Server Information, Health Display and Armor Status, plus two Inventory readouts: Inventory Slots and Totem Counter. Speed is an estimate based on horizontal displacement per client tick. Ping may show `--` when unavailable. Session time resets when the client restarts. The HUD attaches to the vanilla chat layer and is hidden with the normal HUD.
- Sprint: attempts to sprint while holding forward, subject to hunger, sneaking, collision and Minecraft/server rules.
- Auto Respawn: requests respawn from the death screen. This acts immediately when enabled, so use carefully.
- Config: module on/off states automatically load and save to `config/vortexclient.properties`. If the file cannot be parsed, it is protected from overwrite for that session. Restore or fix the file, then restart. This is a single config, not a profiles system.

Only the modules described above are registered, with Sprint and Auto Respawn remaining opt-in. The requested combat automation, exploit/packet manipulation, server-specific market automation, HUD editor, profiles, draggable panels, main-menu branding, animations and other large design items are **not implemented**. There are no fake module toggles for them. This project focuses on presentation and informational readouts, not manipulating multiplayer interactions.

## Build on your PC

You need internet access, **JDK 21**, and **Gradle 9.2.1** installed. This source ZIP intentionally does not include an unverified Gradle wrapper. Use a terminal in this folder:

1. `java -version` and `gradle --version` should both report Java 21 and Gradle 9.2.1 respectively. If Gradle uses another Java, set `JAVA_HOME` to your JDK 21 folder.
2. Run `gradle build` (Windows: `gradle.bat build` if that's how your Gradle install is set up).
3. If the build succeeds, find `build/libs/vortexclient-0.3.0.jar`. Do **not** install the `-sources.jar` file.
4. Install Fabric Loader 0.18.4 or newer for Minecraft Java 1.21.11, and place a matching Fabric API JAR plus your built Vortex JAR in the Minecraft `mods` folder. Use a separate test profile and back up worlds first.
5. Start Minecraft with Java 21, enter a test world, press Right Shift. Toggle modules and check that they persist after restarting. Change the keybind in Minecraft's Controls if needed.
6. If the build fails or Minecraft crashes, send the full terminal output or `logs/latest.log` and the crash report. Don't assume the code has been tested.

The first Gradle run must download Loom, Minecraft, mappings, Loader and Fabric API. Dependencies and APIs can change, and a build or load may require fixes. This package has not been compiled or tested in-game; verify it on a separate test profile before use. The source uses Yarn 1.21.11+build.3 and the Fabric 1.21.11 APIs.

## Local checks included

`tests/ConfigSmokeTest.java` is an unrun standalone test of config defaults, persistence and corrupt-file protection. The creation environment had a Java runtime but no Java compiler, so this test could not be run. It does not test Minecraft, Fabric, the GUI or a JAR. You can run it with JDK 21 from the project folder:

```
mkdir test-classes
javac -d test-classes src/main/java/dev/vortex/client/Modules.java src/main/java/dev/vortex/client/VortexConfig.java tests/ConfigSmokeTest.java
java -cp test-classes ConfigSmokeTest
```
