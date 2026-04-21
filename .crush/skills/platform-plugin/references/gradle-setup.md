# Gradle Build Setup Reference

## IntelliJ Platform Gradle Plugin (2.x)

Official plugin at [tools-intellij-platform-gradle-plugin](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html).

### Version Selection

| IDE Version Range | Plugin Version |
|-------------------|---------------|
| 2024.2+ | 2.x (current) |
| 2022.3+ - 2024.1 | 2.x (recommended) |
| 2022.3 and earlier | 1.x (obsolete) |

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

The `foojay-resolver-convention` plugin auto-detects Java toolchains.

### build.gradle.kts — Dependencies

```kotlin
dependencies {
    // Test framework
    testImplementation("junit:junit:4.13.2")

    // IntelliJ Platform IDE (choose one)
    intellijPlatform {
        intellijIdea("2025.2.6.1")           // IntelliJ IDEA
        // intellijIdea("2025.2.6.1", "IU")  // Ultimate
        // intellijIdea("2025.2.6.1", "IC")  // Community
        // intellijPyCharm("2025.2.6.1")     // PyCharm
        // intellijPhpStorm("2025.2.6.1")    // PhpStorm
        // intellijWebStorm("2025.2.6.1")    // WebStorm
        // intellijClion("2025.2.6.1")       // CLion
        // intellijGoLand("2025.2.6.1")      // GoLand
        // intellijRider("2025.2.6.1")       // Rider

        testFramework(TestFrameworkType.Platform)

        // Plugin dependencies (IDE modules)
        pluginModule("com.intellij.java")
        pluginModule("org.jetbrains.kotlin")
    }

    // Third-party libraries
    implementation("org.apache.commons:commons-lang3:3.14.0")
}
```

### build.gradle.kts — Plugin Configuration

```kotlin
intellijPlatform {
    pluginConfiguration {
        id = "com.example.myplugin"
        name = "My Plugin"
        vendor = "example"

        // Extract description from README.md
        description = providers.fileContents(layout.projectDirectory.file("README.md"))
            .asText.map { extractDescription(it) }

        // Extract changelog from CHANGELOG.md
        val changelog = project.changelog
        changeNotes = version.map { pluginVersion ->
            changelog.renderItem(
                changelog.getOrNull(pluginVersion) ?: changelog.getUnreleased(),
                Changelog.OutputType.HTML
            )
        }

        // IDEA compatibility (alternative to <idea-version> in plugin.xml)
        ideaVersion {
            sinceBuild = "231.9011.34"
            // untilBuild = "242.*"  // optional upper bound
        }
    }

    // Signing configuration (alternative to env vars)
    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    // Publishing configuration
    publishing {
        tokens = providers.environmentVariable("PUBLISH_TOKEN")
        // channels = listOf("beta")  // optional marketplace channels
    }

    // Plugin verification
    pluginVerification {
        // IDE versions to verify against
        // ideVersions = listOf("2024.3", "2025.1")
    }
}
```

### gradle.properties

```properties
# Required
group = com.example
version = 1.0.0
pluginRepositoryUrl = https://github.com/example/my-plugin

# Optional but recommended
kotlin.stdlib.default.dependency = false
org.gradle.configuration-cache = true
org.gradle.caching = true
```

### Key Tasks

| Task | Description |
|------|-------------|
| `runIde` | Launch sandbox IDE with plugin loaded |
| `runIdeForUiTests` | Launch IDE for UI testing |
| `buildPlugin` | Build plugin ZIP distribution |
| `verifyPlugin` | Run Plugin Verifier against target IDEs |
| `check` | Run tests with coverage |
| `publishPlugin` | Publish to JetBrains Marketplace |
| `patchChangelog` | Update CHANGELOG.md with release notes |
| `getChangelog` | Output changelog sections |
| `qodana` | Run Qodana code inspection |

### Running the Sandbox IDE

```bash
./gradlew runIde
```

This launches a separate IntelliJ IDEA instance with your plugin installed. Useful for manual testing and debugging.

### Debugging

Attach debugger to the sandbox IDE:
1. Set breakpoints in your plugin code
2. Run `Run Plugin` configuration (debug mode)
3. The IDE will suspend at breakpoints

IDE logs are at `.intellijPlatform/sandbox/*/log/idea.log`.

### Kotlin Integration

If using Kotlin, ensure the `org.jetbrains.kotlin.jvm` plugin is applied. The template includes it by default.

Kotlin standard library is NOT bundled by default — set `kotlin.stdlib.default.dependency = false` in `gradle.properties`.

### Java Integration

For Java-only or mixed projects, create `src/main/java/` alongside `src/main/kotlin/`. No special Gradle configuration needed — the `intellijPlatform` plugin handles both.

### Changelog Plugin

The `org.jetbrains.changelog` plugin manages `CHANGELOG.md`:

```kotlin
changelog {
    groups.empty()  // Show no groups (flat list)
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
    versionPrefix = ""  // No "v" prefix
}
```

CHANGELOG.md format:
```markdown
# MyPlugin Changelog

## [Unreleased]
### Added
- Feature description

### Fixed
- Bug fix description

## [1.0.0] - 2025-01-15
### Added
- Initial release
```

Groups available: `Added`, `Changed`, `Deprecated`, `Removed`, `Fixed`, `Security`.

### Multi-IDE Support

To build for multiple IDEs, use Gradle variants or separate modules. The `intellijPlatform { intellijIdea(...) }` call targets one IDE type at a time.
