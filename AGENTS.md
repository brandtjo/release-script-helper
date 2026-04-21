# Release Script Helper — IntelliJ Plugin

IntelliJ Platform plugin for generating SQL release scripts with ticket numbers, descriptions, and configurable naming conventions.

## Commands

Gradle commands should be executed through the **IntelliJ MCP** `execute_terminal_command` tool for IDE integration (progress tracking, output in IDE terminal).

```
./gradlew buildPlugin        # Build the plugin ZIP
./gradlew check              # Run tests + Kover coverage
./gradlew verifyPlugin       # Verify plugin against IntelliJ IDE versions
./gradlew qodana             # Run Qodana code inspection
./gradlew getChangelog       # Get changelog entries
./gradlew properties         # Print project properties
./gradlew clean              # Clean build artifacts
./gradlew build              # Full build
./gradlew test               # Run tests
./gradlew patchChangelog     # Patch changelog for next version
```

Run/Debug configurations are in `.run/` — discover them via MCP `get_run_configurations` tool.

## Architecture

```
src/main/
├── kotlin/.../model/          # Data models (Kotlin)
│   ├── ReleaseScript.kt       # Core model — builds file names & content
│   └── Options.kt             # Configurable options (ticket types, file endings)
├── kotlin/.../ui/
│   └── BasicAddDialog.kt      # Swing dialog for script parameters
├── java/.../action/           # IntelliJ AnAction implementations (Java)
│   ├── BasicAddScript.java    # Abstract base — shared flow for both actions
│   ├── ProjectExplorerAddScript.java  # "New → Release Script" action
│   └── GenerationMenuAddScript.java   # "Generate → Generate Release Script" action
├── java/.../settings/         # Plugin settings persistence (Java)
│   ├── ProjectLevelState.java     # Persistent state (XML-serialized via XmlSerializerUtil)
│   ├── ProjectLevelConfigurable.java # Settings UI page
│   └── ProjectLevelComponent.java   # Settings panel widgets
└── java/.../util/
    └── FileUtil.java          # VirtualFile ↔ relative path conversion
```

### Control flow (script creation)

1. User triggers action → `actionPerformed()` runs
2. `BasicAddScript.promptValuesForReleaseScript()` creates a `ReleaseScript` model, presets ticket info from VCS branch name, then shows `BasicAddDialog`
3. If dialog confirms, `ApplicationManager.getApplication().runWriteAction()` creates the file via PSI, writes binary content, and opens it

### Key gotchas

- **Mixed Kotlin/Java**: Models are Kotlin, actions/settings are Java. Both compile against JVM 21.
- **VCS branch presetting** (`BasicAddScript.presetFromVcsBranch`): Parses branch name like `OCT-1234-feature` to auto-fill ticket type and number. Only the first repo's current branch is used.
- **Dialog Swing state sync** (`updateUiComponentsByHandBecauseSwingIsStupid`): When the user opens Settings from the dialog and saves, the combo boxes must be manually repopulated — `bindItem` doesn't auto-react to model changes.
- **`ReleaseScript.ticketNumber` setter**: Side-effect — setting the ticket number also updates `ticketType` by matching against configured ticket type prefixes.
- **`ReleaseScript.options` setter**: Side-effect — setting options also initializes `ticketType` and `fileEnding` from the first element of each list.
- **Settings persistence**: `ProjectLevelState` uses `@State`/`@Storage` annotations — config is saved per-project in `ReleaseScriptHelperPlugin.xml`.
- **`isModified()` in `ProjectLevelConfigurable`** (line 39): `isUseUnixTimestamp() == settings.getOptions().getUseCustomScriptNumber()` — note the inverted comparison (unix timestamp selected ↔ custom script number NOT selected).

## Conventions

- Package: `com.github.brandtjo.releasescripthelper`
- Kotlin files: models and UI dialogs
- Java files: actions, settings, utilities
- IntelliJ Platform APIs used: `com.intellij.openapi.*`, `git4idea.*`
- External deps: Apache Commons Lang3, Commons Collections, SLF4J
- detekt config at `detekt-config.yml` for Kotlin linting
- Qodana config at `qodana.yaml` for CI inspections

## CI/CD

- **`.github/workflows/build.yml`** — build, test, Qodana, plugin verification, draft release
- **`.github/workflows/release.yml`** — publishes when a GitHub release is published
- **`.github/workflows/run-ui-tests.yml`** — UI tests via Robot Server
- Plugin signs artifacts via `CERTIFICATE_CHAIN`/`PRIVATE_KEY` env vars
