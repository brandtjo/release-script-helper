# Core Development Concepts

## Services

Services are the primary way to hold state and logic in a plugin.

### Light Services (Preferred)

Use `@Service` annotation — no plugin.xml registration needed.

```kotlin
// Application-level (singleton)
@Service
class MyAppService {
    fun doSomething() { /* ... */ }
}

// Project-level (one per project)
@Service(Service.Level.PROJECT)
class MyProjectService(private val project: Project) {
    fun doSomething() { /* ... */ }
}
```

```java
@Service
public final class MyAppService {
    public void doSomething() { /* ... */ }
}

@Service(Service.Level.PROJECT)
public final class MyProjectService {
    private final Project project;
    public MyProjectService(Project project) { this.project = project; }
}
```

**Restrictions**: class must be `final`, no constructor injection of other services, no plugin.xml registration.

### Registered Services

For interfaces, subclassing, or exposing API:

```kotlin
interface MyService {
    fun doSomething(): String
}

class MyServiceImpl(private val project: Project) : MyService {
    override fun doSomething() = "result"
}
```

Register in plugin.xml:
```xml
<extensions defaultExtensionNs="com.intellij">
    <projectService serviceInterface="com.example.MyService"
                    serviceImplementation="com.example.MyServiceImpl"/>
</extensions>
```

### Retrieving Services

**Never cache service instances in fields.** Always call `getService()` on demand.

```kotlin
// Kotlin
val service = project.service<MyService>()
val appService = service<MyAppService>()

// Java
MyService service = project.getService(MyService.class);
MyAppService appService = ApplicationManager.getApplication().getService(MyAppService.class);

// Static convenience method
val service = MyService.getInstance(project)
```

### Persistent State

For state that survives IDE restarts, implement `PersistentStateComponent`:

```kotlin
@Service(Service.Level.PROJECT)
class MyPersistentService(private val project: Project) : PersistentStateComponent<MyPersistentService.State> {

    data class State(var value: String = "default")

    private var state = State()

    override fun getState() = state

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, this.state)
    }

    companion object {
        fun getInstance(project: Project) = project.getService(MyPersistentService::class.java)
    }
}
```

For application-level persistence, roamType must be `DISABLED`.

## Actions

### AnAction

```kotlin
class MyAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val data = e.getData(LangDataKeys.PSI_FILE)
        // ...
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
```

### Common Data Keys

```kotlin
LangDataKeys.PSI_FILE          // Current PSI file
EditorDataKeys.EDITOR          // Current editor
CommonDataKeys.PROJECT         // Current project
CommonDataKeys.SELECTED_ITEMS  // Selected files in project view
PlatformDataKeys.CONTEXT_COMPONENT  // Component under mouse
```

### Action Groups

```kotlin
class MyActionGroup : DefaultActionGroup() {
    override fun update(e: AnActionEvent) {
        // Dynamically add/remove actions
        removeAll()
        add(MyAction("Option A"))
        add(MyAction("Option B"))
    }
}
```

### Adding to Menus

```xml
<add-to-group group-id="MainMenu" anchor="first"/>
<add-to-group group-id="ToolsMenu" anchor="last"/>
<add-to-group group-id="ProjectViewPopupMenu" anchor="first"/>
<add-to-group group-id="EditorPopupMenu" anchor="before"/>
<add-to-group group-id="$SearchEverywhere" anchor="first"/>
```

## Tool Windows

### Factory

```kotlin
class MyToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = MyToolWindowContent(project)
        val content = ContentFactory.getInstance()
            .createContent(panel.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true
}
```

### Content

```kotlin
class MyToolWindowContent(private val project: Project) {
    private val panel = JBPanel<JBPanel<*>>().apply {
        layout = BorderLayout()
        add(JLabel("Hello"), BorderLayout.CENTER)
        add(JButton("Click").apply {
            addActionListener { /* ... */ })
        }, BorderLayout.SOUTH)
    }

    fun getContent() = panel
}
```

### Tool Window Properties

```xml
<toolWindow factoryClass="com.example.MyToolWindowFactory"
            id="MyToolWindow"
            icon="AllIcons.Toolwindows.WebToolWindow"
            secondary="true"
            anchor="right"/>
```

- `secondary` — docked in bottom panel instead of side
- `anchor` — `left`, `right`, `bottom`
- `weight` — relative size (default 50)

## Startup Activities

### ProjectActivity (Kotlin coroutine)

```kotlin
class MyActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        // Runs when project opens
        withContext(Dispatchers.IO) { /* background work */ }
    }
}
```

### StartupActivity (Java or sync Kotlin)

```kotlin
class MyStartupActivity : StartupActivity, DumbAware {
    override fun runActivity(project: Project) {
        // Runs on EDT immediately after project opens
    }
}
```

Register:
```xml
<extensions defaultExtensionNs="com.intellij">
    <postStartupActivity implementation="com.example.MyActivity"/>
    <backgroundPostStartupActivity implementation="com.example.MyBackgroundActivity"/>
</extensions>
```

## Listeners

### Document Listener

```kotlin
class MyDocumentListener : DocumentListener {
    override fun documentChanged(event: DocumentEvent) {
        val document = event.document
        val project = event.project ?: return
        // ...
    }
}
```

### File Listener

```kotlin
class MyFileListener : FileDocumentManagerListener {
    override fun fileDocumentManagerWillSave(document: Document) {
        // Called before document is saved
    }
}
```

### Application Listener

```kotlin
class MyAppListener : AppLifecycleListener {
    override fun appStarting() { /* ... */ }
    override fun appWillDispose() { /* ... */ }
    override fun frameCreated(frame: JFrame) { /* ... */ }
}
```

Register:
```xml
<applicationListeners>
    <listener topic="com.intellij.ide.AppLifecycleListener"
              class="com.example.MyAppListener"/>
</applicationListeners>
```

## Threading Model

### EDT (Event Dispatch Thread)

All UI updates MUST run on EDT:

```kotlin
ApplicationManager.getApplication().invokeLater {
    // UI code here
}

// Or with coroutine dispatchers
CoroutinesApi.dispatchToUi {
    // UI code here
}
```

### Background Threads

```kotlin
ApplicationManager.getApplication().executeOnPooledThread {
    // Non-UI work here
}

// With coroutines
CoroutinesApi.dispatchToBackground {
    // Background work
}
```

### Dumb Mode

Index-dependent code must wait for indexing:

```kotlin
DumbService.getInstance(project).runReadActionInSmartMode {
    // PSI access here
}
```

### Write Actions

File creation/modification requires write actions:

```kotlin
ApplicationManager.getApplication().runWriteAction {
    // File/PSI modification here
}
```

## PSI (Program Structure Interface)

### Creating Files

```kotlin
ApplicationManager.getApplication().runWriteAction {
    val psiManager = PsiManager.getInstance(project)
    val directory = psiManager.findDirectory(targetDir) ?: return@runWriteAction
    val psiFile = PsiFileFactory.getInstance(project)
        .createFileFromText("MyFile.sql", SQLLanguage.INSTANCE, content)
    directory.add(psiFile)
}
```

### Reading PSI

```kotlin
DumbService.getInstance(project).runReadActionInSmartMode {
    val psiFile = psiManager.findFile(virtualFile)
    psiFile?.let {
        val text = it.text
        // parse or analyze
    }
}
```

## Notifications

```kotlin
val group = NotificationGroupManager.getInstance()
    .getNotificationGroup("My Plugin Notifications")

group.createNotification(
    "Title",
    "Message body",
    NotificationType.INFORMATION
).notify(project)
```

Notification types: `INFORMATION`, `WARNING`, `ERROR`, `BALLOON`.

## Settings/Configuration

### PersistentSettings (Application-level)

```kotlin
@Service
class MySettings : PersistentStateComponent<MySettings.State> {

    data class State(
        @XCollection(elementTag = "entry")
        var entries: MutableList<Entry> = mutableListOf()
    ) {
        data class Entry(
            @XAttribute(key = "key")
            var key: String = "",
            @XAttribute(key = "value")
            var value: String = ""
        )
    }

    private var state = State()

    override fun getState() = state

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, this.state)
    }
}
```

### Configurable UI

```kotlin
class MyConfigurable : Configurable {
    private var myField: JTextField = JTextField()

    override fun createComponent() = JBPanel<JBPanel<*>>().apply {
        layout = GridLayout(0, 2)
        add(JLabel("Setting:"))
        add(myField)
    }

    override fun isModified() = myField.text != settings.value

    override fun apply() {
        settings.value = myField.text
    }

    override fun reset() {
        myField.text = settings.value
    }
}
```

Register settings page:
```xml
<extensions defaultExtensionNs="com.intellij">
    <projectConfigurable instance="com.example.MyConfigurable"/>
</extensions>
```
