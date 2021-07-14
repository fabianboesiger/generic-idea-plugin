# Generic IDEA Plugin for Scala Macro Support

This is an experimental project to add Scala macro support to IntelliJ IDEA for any macro.

## TODOs

* Currently, the communication between the macro and this plugin uses reading and writing of XML files. This is not ideal performance-wise.
* This solution only works for macros which transform/generate class members/companion object members of the annotated class. Maybe it could be expanded to work on a wider range of macros
* Remove all mentions of Silicon, as this project can be used independently of Silicon.

## Usage

### On the Macro Side

* Build your custom macro for your Scala project using the template found in `macro/template.scala`.
* The method `saveConfig` from the template saves all relevant data to a directory called `.plugin` in your Scala project.

### On the Plugin Side

* Compile the plugin using `sbt packagePlugin`.
* The compiled plugin is ready to be installed in `target/plugin/silicon-idea/lib/siliconIdeaPlugin.jar`.
* To install, inside IntelliJ: `Preferences` -> `Plugins` -> `Install plugin from disk`
* If the plugin is correctly installed, the `.plugin` directory is read and IntelliJ should not highlight your macro usages as errors.

