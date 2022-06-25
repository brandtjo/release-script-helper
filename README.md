# release-script-helper

![Build](https://github.com/brandtjo/release-script-helper/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/17417-release-script-helper)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/17417-release-script-helper)

<!-- Plugin description -->
This plugin adds an additional "new-file-menu" and "generation-menu" entry, to add new release script files with semi-automatically generated names.
A release script file name adheres to this pattern:<br>
`(timestamp|custom)_[(ticket-type)(ticket-number)_](description).(suffix)`

The script generation is guided by a small dialog, allowing to opt out of using a ticket system, or defining ticket type, number and description.
In the plugin settings ticket types, default directory and file endings can be configured. Also a choice can be made to generate UNIX timestamps as file prefix by default or ask for a custom value.

The default hot key for opening the dialog is `ctrl + alt + shift + r`. If text was selected prior to pressing the hot key, it will be inserted into the generated release script file.
If the current project is a Git project, then a branch naming convention can be used to prefill the ticket type and ticket number field:
`(ticket-type)-*(ticket-number)-branch-name-...`

<!-- Plugin description end -->

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template