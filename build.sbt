lazy val commonSettings = Def.settings(
  scalaVersion := "2.13.5",
)

lazy val ideaSettings = Def.settings(
  ThisBuild / intellijPluginName := "silicon-idea",
  ThisBuild / intellijPlatform := IntelliJPlatform.IdeaCommunity,
  ThisBuild / intellijBuild := "203.7148.57",
  intellijPlugins += "org.intellij.scala".toPlugin
)

lazy val siliconIdeaPlugin: Project = project
  .in(file("."))
  .settings(commonSettings)
  .settings(ideaSettings)
  .enablePlugins(SbtIdeaPlugin)
