# release-script-helper

![Build](https://github.com/brandtjo/release-script-helper/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/17417-release-script-helper)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/17417-release-script-helper)

<!-- Plugin description -->
This plugin adds an additional "new-file-menu" entry, to add new release script files with semi-automatically generated names.
A release script file name adheres to this pattern:<br>
`(timestamp|custom)_[(ticket-type)(ticket-number)_](description).(suffix)`

The script generation is guided by a small dialog, allowing to opt out of using a ticket system, or defining ticket type, number and description.
<!-- Plugin description end -->

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template