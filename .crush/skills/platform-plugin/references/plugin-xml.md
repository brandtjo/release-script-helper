# Plugin Configuration File (plugin.xml) Reference

Located at `src/main/resources/META-INF/plugin.xml`. This is the manifest that declares everything about your plugin.

## Root Element: `<idea-plugin>`

Required attributes: none (but `<id>`, `<name>`, `<vendor>` are strongly recommended).

Optional attributes:
- `url` — link to plugin homepage
- `require-restart` — whether install/update/uninstall requires IDE restart (default: `false`)

## Required Elements

### `<id>`
Unique identifier. Use reverse domain notation (e.g., `com.example.myplugin`). Cannot change after public release. Only `.`, `-`, `_` allowed.

### `<name>`
User-visible display name (Title Case).

### `<vendor>`
Vendor name. Optional `url` and `email` attributes for Marketplace listing.

```xml
<vendor email="dev@example.com" url="https://example.com">Example Inc</vendor>
```

### `<depends>`
Specifies dependencies on other plugins or platform modules.

```xml
<!-- Required platform dependency -->
<depends>com.intellij.modules.platform</depends>

<!-- Required plugin dependency -->
<depends>com.jetbrains.php</depends>

<!-- Optional dependency with conditional config file -->
<depends optional="true" config-file="kotlin-only.xml">org.jetbrains.kotlin</depends>
```

Common module dependencies:
- `com.intellij.modules.platform` — minimal (works in all IDEs)
- `com.intellij.modules.java` — Java IDE features
- `com.intellij.modules.python` — Python features

### `<idea-version>`
Sets IDE compatibility range. Can be omitted if configured via Gradle.

```xml
<idea-version since-build="231.9011.34"/>
<idea-version since-build="231" until-build="232.*"/>
```

## Extension Registration

### `<extensions>`
Registers service implementations, activities, tool windows, etc.

```xml
<extensions defaultExtensionNs="com.intellij">
    <applicationService serviceImplementation="com.example.MyAppService"/>
    <projectService serviceInterface="com.example.MyProjectService"
                    serviceImplementation="com.example.MyProjectServiceImpl"/>
    <postStartupActivity implementation="com.example.MyActivity"/>
    <toolWindow factoryClass="com.example.MyToolWindowFactory" id="MyToolWindow"/>
</extensions>
```

### `<extensionPoints>`
Define extension points that other plugins can extend.

```xml
<extensionPoints>
    <extensionPoint name="myExtension"
                    interface="com.example.MyExtensionPoint"/>
</extensionPoints>
```

## Actions

### `<actions>`
Defines menu items, toolbar buttons, and action groups.

```xml
<actions resource-bundle="messages.ActionsBundle">
    <!-- Standalone action -->
    <action id="com.example.MyAction" class="com.example.MyAction"
            text="My Action" description="Does something">
        <add-to-group group-id="ToolsMenu" anchor="last"/>
        <keyboard-shortcut keymap="$default"
            first-keystroke="control alt M"/>
    </action>

    <!-- Action group -->
    <group id="com.example.MyGroup" text="My Tools" popup="true">
        <reference ref="com.example.MyAction"/>
        <separator/>
        <add-to-group group-id="EditMenu" anchor="before"/>
    </group>
</actions>
```

Common group IDs for `<add-to-group>`:
- `MainMenu` — main menu
- `ToolsMenu` — Tools menu
- `EditMenu` — Edit menu
- `SearchMenu` — Search menu
- `ProjectViewPopupMenu` — Project view context menu
- `EditorPopupMenu` — Editor context menu
- `$SearchEverywhere` — Search Everywhere
- `$FindActionPopup` — Find Action popup

### `<action>` attributes
- `id` — unique action ID
- `class` — implementation class
- `text` — display text (or use resource bundle)
- `description` — status bar text on focus
- `icon` — icon reference (e.g., `AllIcons.Actions.GC`)

### `<group>` attributes
- `id` — unique group ID
- `class` — implementation class (defaults to `DefaultActionGroup`)
- `text` — menu label
- `popup` — show as submenu (`true`) or section (`false`)

## Listeners

### `<applicationListeners>`
Application-scoped event listeners.

```xml
<applicationListeners>
    <listener topic="com.intellij.openapi.fileTypes.FileTypeManager"
              class="com.example.MyFileTypeListener"/>
</applicationListeners>
```

### `<projectListeners>`
Project-scoped event listeners. Same `<listener>` element structure.

Listener attributes:
- `topic` — fully qualified listener interface
- `class` — implementation class
- `os` — restrict to OS (`linux`, `mac`, `windows`)
- `activeInTestMode` — instantiate in tests (default: `true`)
- `activeInHeadlessMode` — instantiate in headless mode (default: `true`)

## Resource Bundles

```xml
<resource-bundle>messages.MyBundle</resource-bundle>
```

Maps to `src/main/resources/messages/MyBundle.properties`.

## XInclude

```xml
<idea-plugin xmlns:xi="http://www.w3.org/2001/XInclude">
    <xi:include href="/META-INF/optional-features.xml">
        <xi:fallback/>
    </xi:include>
</idea-plugin>
```

## Common Extension Points Reference

| Extension Point | Purpose |
|----------------|---------|
| `com.intellij.applicationService` | Application-level service |
| `com.intellij.projectService` | Project-level service |
| `com.intellij.postStartupActivity` | Run on project open |
| `com.intellij.toolWindowFactory` | Tool window panel |
| `com.intellij.projectConfigurable` | Settings page |
| `com.intellij.fileTypeFactory` | Custom file type |
| `com.intellij.lang` | Language support |
| `com.intellij.codeInsight.intention` | Quick fix / intention |
| `com.intellij.codeInsight.completion` | Code completion |
| `com.intellij.highlighter` | Syntax highlighting |
| `com.intellij.erroranalyzer` | Error strip |
| `com.intellij.notificationGroup` | Notification group |
| `com.intellij.vcs.log` | VCS log integration |
| `com.intellij.execution.lineMarker` | Run configuration gutter icon |
| `com.intellij.uiEditorTab` | Editor tab provider |

For the complete list, browse [IntelliJ Platform Explorer](https://jb.gg/ipe).
