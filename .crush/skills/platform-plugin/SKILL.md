---
name: platform-plugin
description: How to develop IntelliJ Platform (JetBrains IDE) plugins. Use this skill whenever the user wants to create, modify, or understand an IntelliJ-based plugin — including plugin.xml configuration, Gradle build setup, actions, tool windows, services, listeners, extension points, testing, CI/CD, and publishing to JetBrains Marketplace. Trigger on mentions of "IntelliJ plugin", "JetBrains plugin", "IDE plugin", "Plugin DevKit", "tool window", "action group", "extension point", "plugin.xml", "runIde", or building any plugin for IntelliJ IDEA, PyCharm, WebStorm, CLion, GoLand, Rider, or any other JetBrains IDE.
---

# IntelliJ Platform Plugin Development

Build plugins for IntelliJ IDEA, PyCharm, WebStorm, CLion, GoLand, Rider, and all JetBrains IDEs using the official IntelliJ Platform Gradle Plugin (2.x).

## Project Structure

```
my-plugin/
├── .github/workflows/    CI workflows (build, release, UI tests)
├── .run/                 Run/Debug configurations
├── gradle/wrapper/       Gradle wrapper
├── src
│   ├── main
│   │   ├── kotlin/       Kotlin sources (or java/ for Java)
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   └── plugin.xml        Plugin manifest
│   │       └── messages/             Resource bundles
│   └── test
│       ├── kotlin/       Test sources
│       └── testData/     Test fixtures
├── build.gradle.kts      Build configuration
├── settings.gradle.kts   Repositories + plugin versions
├── gradle.properties     Metadata (group, version)
├── CHANGELOG.md          Release notes
└── README.md             Plugin description
```

Create `src/main/java/` alongside `src/main/kotlin/` for mixed Kotlin/Java projects.

## Build Setup

### settings.gradle.kts

```kotlin
rootProject.name = "my-plugin"

pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.jvm") version "2.1.20"
        id("org.jetbrains.changelog") version "2.5.0"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("org.jetbrains.intellij.platform") version "2.14.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        intellijPlatform {
            defaultRepositories()
        }
    }
}
```

### build.gradle.kts

```kotlin
plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog")
}

dependencies {
    testImplementation("junit:junit:4.13.2")

    intellijPlatform {
        intellijIdea("2025.2.6.1")          // target IDE (idea, pycharm, etc.)
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        description = providers.fileContents(layout.projectDirectory.file("README.md"))
            .asText.map { /* extract <!-- Plugin description --> ... <!-- Plugin description end --> */ }
    }
}

changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
}

tasks {
    publishPlugin { dependsOn(patchChangelog) }
}
```

### gradle.properties

```properties
group = com.example
version = 1.0.0
pluginRepositoryUrl = https://github.com/example/my-plugin
kotlin.stdlib.default.dependency = false
org.gradle.configuration-cache = true
org.gradle.caching = true
```

### Key Gradle Tasks

| Task | Purpose |
|------|---------|
| `runIde` | Launch sandbox IDE with plugin loaded |
| `buildPlugin` | Package plugin ZIP |
| `check` | Run tests (Kover coverage) |
| `verifyPlugin` | Verify against target IDE versions + Plugin Verifier |
| `publishPlugin` | Publish to JetBrains Marketplace |
| `patchChangelog` | Update CHANGELOG.md with release notes |
| `getChangelog` | Output changelog sections |

## Plugin Configuration (plugin.xml)

Located at `src/main/resources/META-INF/plugin.xml`. Read `references/plugin-xml.md` for the full element reference.

### Minimal plugin.xml

```xml
<idea-plugin>
    <id>com.example.myplugin</id>
    <name>My Plugin</name>
    <vendor>example</vendor>

    <depends>com.intellij.modules.platform</depends>

    <resource-bundle>messages.MyBundle</resource-bundle>

    <extensions defaultExtensionNs="com.intellij">
        <!-- Your extensions go here -->
    </extensions>

    <actions>
        <!-- Your actions go here -->
    </actions>
</idea-plugin>
```

### Critical rules
- `<id>` must be unique (use reverse domain notation), stable (cannot change after release), and use only `.`, `-`, `_`
- `<depends>` on `com.intellij.modules.platform` is the minimal dependency for any plugin
- `<idea-version since-build="...">` sets minimum IDE version (can be omitted if configured via Gradle)
- Always set `<vendor>` with `email` or `url` attributes for Marketplace listing

### Common extension points

Read `references/plugin-xml.md` for the complete list. Key ones:

- **Actions**: `<actions>` / `<action>` / `<group>` — menu items, toolbar buttons, tool windows
- **Services**: `<applicationService>`, `<projectService>` — registered services
- **Listeners**: `<applicationListeners>`, `<projectListeners>` — event listeners
- **Startup**: `<postStartupActivity>` — run code when project opens
- **Tool Windows**: `<toolWindow factoryClass="...">` — side panel tool windows
- **Extension Points**: `<extensionPoints>` — define points others can extend

## Core Development Patterns

### Services

Services hold state and logic. Two types:

**Light Services** (preferred when no subclassing needed):
```kotlin
@Service(Service.Level.PROJECT)
class MyProjectService(private val project: Project) {
    fun doSomething() { /* ... */ }
}
```
```java
@Service(Service.Level.PROJECT)
public final class MyProjectService {
    private final Project project;
    public MyProjectService(Project project) { this.project = project; }
}
```

**Registered Services** (when subclassing or interface exposure needed):
```xml
<extensions defaultExtensionNs="com.intellij">
    <projectService serviceInterface="com.example.MyService"
                    serviceImplementation="com.example.MyServiceImpl"/>
</extensions>
```

**Rules**:
- Never do heavy initialization in constructors
- Never cache service instances in fields — always call `getService()` on demand
- Use `project.service<MyService>()` (Kotlin) or `project.getService(MyService.class)` (Java)
- For coroutines, inject `CoroutineScope` as a constructor parameter

### Actions

Actions appear in menus, toolbars, and the popup.

```kotlin
class MyAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        // ...
    }
}
```

Register in `plugin.xml`:
```xml
<actions>
    <action id="com.example.MyAction" class="com.example.MyAction"
            text="My Action" description="Does something">
        <add-to-group group-id="ToolsMenu" anchor="last"/>
        <keyboard-shortcut keymap="$default"
            first-keystroke="control alt M"/>
    </action>
</actions>
```

### Tool Windows

```kotlin
class MyToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val content = ContentFactory.getInstance()
            .createContent(MyToolWindow(project).getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }
}
```

Register:
```xml
<extensions defaultExtensionNs="com.intellij">
    <toolWindow factoryClass="com.example.MyToolWindowFactory"
                id="MyToolWindow" icon="AllIcons.Toolwindows.WebToolWindow"/>
</extensions>
```

### Startup Activities

```kotlin
class MyActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        // Runs when project opens (Kotlin coroutine)
    }
}
```

Register:
```xml
<extensions defaultExtensionNs="com.intellij">
    <postStartupActivity implementation="com.example.MyActivity"/>
</extensions>
```

### Listeners

```kotlin
class MyDocumentListener : DocumentListener {
    override fun documentChanged(event: DocumentEvent) {
        // ...
    }
}
```

Register:
```xml
<projectListeners>
    <listener topic="com.intellij.openapi.editor.DocumentListener"
              class="com.example.MyDocumentListener"/>
</projectListeners>
```

## Resource Bundles

For localization, create a properties file and a Kotlin bundle class:

`src/main/resources/messages/MyBundle.properties`:
```properties
my.action.text=My Action
my.dialog.title=My Dialog
```

`src/main/kotlin/com/example/MyBundle.kt`:
```kotlin
object MyBundle : DynamicBundle("messages.MyBundle") {
    @JvmStatic
    fun message(key: String, vararg params: Any) = getMessage(key, *params)
}
```

## Threading Model

- **EDT (Event Dispatch Thread)**: All UI updates must run on EDT
- **Background threads**: Use `ApplicationManager.getApplication().executeOnPooledThread {}` for non-UI work
- **DumbMode**: Index-dependent code must wrap accesses in `DumbService.runReadActionInSmartMode {}`
- **Kotlin coroutines**: Use `CoroutinesApi.dispatchToUi()` and `CoroutinesApi.dispatchToBackground()` for cross-thread calls

## Testing

### Functional Tests

```kotlin
class MyPluginTest : BasePlatformTestCase() {
    fun testSomething() {
        val service = project.service<MyService>()
        assertEquals(expected, service.doSomething())
    }

    override fun getTestDataPath() = "src/test/testData/myplugin"
}
```

Run with `./gradlew check`. Test data lives in `src/test/testData/`.

### UI Tests

Use [IntelliJ UI Test Robot](https://github.com/JetBrains/intellij-ui-test-robot) for UI testing. Not wired into the template by default — add your own source set and Gradle tasks.

## CI/CD (GitHub Actions)

### Build workflow (`.github/workflows/build.yml`)
- Runs on push to `main` and pull requests
- Jobs: `build` (buildPlugin + upload artifact), `test` (check), `verify` (verifyPlugin), `releaseDraft` (draft GitHub release)

### Release workflow (`.github/workflows/release.yml`)
- Runs on GitHub release publish
- Steps: patch changelog → publishPlugin → upload release asset → create changelog PR

### Required secrets
| Secret | Purpose |
|--------|---------|
| `PUBLISH_TOKEN` | JetBrains Marketplace publishing token |
| `PRIVATE_KEY` | RSA private key for plugin signing |
| `PRIVATE_KEY_PASSWORD` | Key password |
| `CERTIFICATE_CHAIN` | Certificate chain for signing |

Generate signing certs per [Plugin Signing docs](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html).

## Changelog Management

Use [Keep a Changelog](https://keepachangelog.com/) format. The Gradle Changelog Plugin auto-handles versioning:

```markdown
# MyPlugin Changelog

## [Unreleased]
### Added
- New feature description

### Fixed
- Bug fix description
```

On release, CI swaps `[Unreleased]` to `[1.0.0]` with a date and creates a fresh `[Unreleased]` section.

## Useful Links

- [IntelliJ Platform SDK Docs](https://plugins.jetbrains.com/docs/intellij)
- [IntelliJ Platform Gradle Plugin](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html)
- [IntelliJ SDK Code Samples](https://github.com/JetBrains/intellij-sdk-code-samples)
- [IntelliJ Platform Explorer](https://jb.gg/ipe) — browse extension points in open-source plugins
- [Kotlin UI DSL v2](https://plugins.jetbrains.com/docs/intellij/kotlin-ui-dsl-version-2.html)
- [JetBrains Marketplace Quality Guidelines](https://plugins.jetbrains.com/docs/marketplace/quality-guidelines.html)
- [Plugin DevKit](https://plugins.jetbrains.com/plugin/22851-plugin-devkit) — must be installed in your IDE
