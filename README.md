# TeamCity Unity plugin

[![official JetBrains project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![build status](https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityUnityPlugin_Build)/statusIcon.svg)](https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityUnityPlugin_Build&guest=1)

The plugin supports building Unity projects on TeamCity.

## Features

* Unity versions detection on build agents
* Unity build runner with smart completions
* Automatic discovery of Unity build steps
* Structured build log with problems highlighting
* Unity Editor tests reporting
* Unity build settings feature

### Unity build settings feature

This [TeamCity build feature](https://confluence.jetbrains.com/display/TCDL/Adding+Build+Features) allows to **automatically activate and return Unity Editor license** before build start and after build completion.

Also it allows to configure **Unity cache server** address in one place to use this setting within Unity build steps. 

# Download

You can [download the plugin](https://plugins.jetbrains.com/plugin/11453-unity-support) and install it as [an additional TeamCity plugin](https://confluence.jetbrains.com/display/TCDL/Installing+Additional+Plugins).

**Note**: After installation, you need to restart TeamCity server by going to Administration => Diagnostics => Restart Server

# Compatibility

The plugin is compatible with [TeamCity](https://www.jetbrains.com/teamcity/download/) 2018.1+ and was verified to work with Unity 2017+.

# Configuration

The plugin automatically detects Editors installed via Unity Hub. Also it searches Editors in the `PATH` environment variable and in the following well-known directories:
* macOS: `/Applications/Unity*` and `/Applications/Unity/Hub/Editor/*`
* Linux: `/opt/Unity*`/`~/Unity*` and `/opt/Unity/Hub/Editor/*`/`~/Unity/Hub/Editor/*`
* Windows: `%Program Files%/Unity*` and `%Program Files%/Unity/Hub/Editor/*`
* TeamCity agent tools location, which allows installing Unity as an [agent tool](https://confluence.jetbrains.com/display/TCDL/Installing+Agent+Tools). 

**Note**: you need to start TeamCity build agent under the same user account which is used for Unity Hub installation.

To add Unity installation located in custom path you could use `UNITY_HOME` environment variable, e.g. `UNITY_HOME=C:\Tools\Unity_2018.1\`. Multiple paths could be specified by using [default path delimiter](https://docs.oracle.com/javase/7/docs/api/java/io/File.html#separator).

To search Unity installation directories in custom path matching `Unity*` pattern you could use `UNITY_HINT_PATH` environment variable, e.g. `UNITY_HINT_PATH=C:\Tools`. Multiple paths could be specified by using [default path delimiter](https://docs.oracle.com/javase/7/docs/api/java/io/File.html#separator).

## Agent configuration parameters

All detected Unity versions will be reported as build agent configuration parameters with the `unity.path.%unityVersion%` prefix. They could be found on the Agents -> "%agentName%" -> Agent Parameters -> Configuration Parameters page in TeamCity server UI.

## Custom error logging settings

The runner allows overriding default error logging settings by using "Line statuses file" parameter where you could specify the path to configuration file. Example file contents:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<lines>
  <line level="warning" message="warning CS\d+" />
  <line level="error" message="error CS\d+" />
</lines>
```

## Common problems

### Unmet requirements: Exists=>unity.path.xxx

This problem indicates that the Unity Editor installation was not found on any of build agent machines. Check that you have [installed Editor](https://unity3d.com/get-unity/download) on your build agents and machines were [properly configured](#configuration).

# Build

This project uses Gradle as the build system. You can easily open it in [IntelliJ IDEA](https://www.jetbrains.com/idea/help/importing-project-from-gradle-model.html) or [Eclipse](http://gradle.org/eclipse/).

# Contributions

We appreciate all kinds of feedback, so please feel free to send a PR or submit an issue.
