# TeamCity Unity plugin

[![official JetBrains project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![build status](https://teamcity.jetbrains.com/app/rest/builds/buildType:id:TeamCityPluginsByJetBrains_TeamCityUnityPlugin_TeamCityUnityPlugin_MasterBuild/statusIcon.svg)

The plugin supports building Unity projects on TeamCity.

## Features

* Unity versions detection on build agents
* Unity build runner with smart completions
* Automatic discovery of Unity build steps
* Structured build log with problems highlighting
* Unity Editor tests reporting
* Unity build settings feature
* Unity as Agent tool
* Running Unity build step inside a container

### Unity build settings feature

This [TeamCity build feature](https://confluence.jetbrains.com/display/TCDL/Adding+Build+Features) allows to **automatically activate and return Unity Editor license** before build start and after build completion.

Additionally, it allows you to configure the address for the assets caching proxy,
which can be either the [Cache server][cache-server] or the [Unity accelerator][unity-accelerator].
The appropriate arguments will be used based on the asset pipeline version used in the project.

# Download

You can [download the plugin](https://plugins.jetbrains.com/plugin/11453-unity-support) and install it as [an additional TeamCity plugin](https://confluence.jetbrains.com/display/TCDL/Installing+Additional+Plugins).

**Note**: After installation, you need to restart TeamCity server by going to Administration => Diagnostics => Restart Server

# Compatibility

The plugin is compatible with:
* [TeamCity](https://www.jetbrains.com/teamcity/download/) 2022.10 and above.

  Currently, it still supports all the Java versions that TeamCity
  [supports](https://www.jetbrains.com/help/teamcity/supported-platforms-and-environments.html#Supported+Java+Versions+for+TeamCity+Server),
  though it will be moved to Java 11 in the future.
  Please consider upgrading the Java version your TeamCity instance is running on.


* Unity 2017 and above.

# Configuration

The plugin automatically detects Editors installed via Unity Hub. Also it searches Editors in the `PATH` environment variable and in the following well-known directories:
* macOS: `/Applications/Unity*` and `/Applications/Unity/Hub/Editor/*`
* Linux: `/opt/Unity*`/`~/Unity*` and `/opt/Unity/Hub/Editor/*`/`~/Unity/Hub/Editor/*`
* Windows: `%Program Files%/Unity*` and `%Program Files%/Unity/Hub/Editor/*`

**Note**: you need to start TeamCity build agent under the same user account which is used for Unity Hub installation.

To add Unity installation located in custom path you could use `UNITY_HOME` environment variable, e.g. `UNITY_HOME=C:\Tools\Unity_2018.1\`. Multiple paths could be specified by using [default path delimiter](https://docs.oracle.com/javase/7/docs/api/java/io/File.html#separator).

To search Unity installation directories in custom path matching `Unity*` pattern you could use `UNITY_HINT_PATH` environment variable, e.g. `UNITY_HINT_PATH=C:\Tools`. Multiple paths could be specified by using [default path delimiter](https://docs.oracle.com/javase/7/docs/api/java/io/File.html#separator).

You may also install Unity as a TeamCity agent tool. See [TeamCity Agent Tool Configuration](#teamcity-agent-tool-configuration) for more information.

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
## TeamCity Agent Tool Configuration

This plugin supports optionally installing Unity as a TeamCity [agent tool](https://confluence.jetbrains.com/display/TCDL/Installing+Agent+Tools).

### Creating Tool Zip Archive

To create a tool zip file for Unity, do the following:
1. Locally install (or extract) the desired version of Unity along with any/all desired Target Support (Android, iOS, Xbox, etc.)
2. Zip the `Editor` folder + [plugin descriptor][plugin-descriptor] into an archive named `Unity-<version>.zip` such as `Unity-2018.4.9f1.zip`
    It should look like this (for Windows):
    ```
    📁 Unity-2018.4.9f1.zip
    |- 📄 teamcity-plugin.xml
    |- 📂 Editor
    │  |- 📂 BugReporter
    │  |- 📂 Data
    │  |- 📂 locales
    │  |- 📄 Unity.exe
    │  |- ...
    ```
   Note that the archive structure may vary depending on the distributed binaries.
   For example, when packing for MacOS, the top-level folder inside the archive should be `Unity.app`.
3. Upload as a Unity Tool on the Administration > Tools page on TeamCity

#### NB!
- Agent environment must contain all the required global dependencies to make Unity work on a given OS.


## Running Unity build step inside a container
This plugin is integrated with [Container Wrapper](https://www.jetbrains.com/help/teamcity/container-wrapper.html) extension.
The integration works only with `2023.09` and above versions of TeamCity.

## Common problems

### Unmet requirements: Exists=>unity.path.xxx

This problem indicates that the Unity Editor installation was not found on any of build agent machines. Check that you have [installed Editor](https://unity3d.com/get-unity/download) on your build agents and machines were [properly configured](#configuration).

## How to Contribute

We place a high value on user feedback and encourage you to share your experience and suggestions.
Send a Pull Request to contribute or contact us via [YouTrack][youtrack] to report an issue.

## Development

### Prerequisites

* JDK 8

### Building

1. Clone the repo
2. Setup local git hooks
    ```shell
    git config --local core.hooksPath .githooks
    ```
3. Build the project using Gradle
    ```shell
    ./gradlew build
    ```

## Additional Resources

- [Changelog](CHANGELOG.md)
- [Maintainership](MAINTAINERSHIP.md)

[plugin-descriptor]: https://plugins.jetbrains.com/docs/teamcity/plugins-packaging.html#Tools
[plugin-descriptor.executables]: https://plugins.jetbrains.com/docs/teamcity/plugins-packaging.html#Making+File+Executable
[youtrack]: https://youtrack.jetbrains.com/newIssue?project=TW&c=Team%20Build%20Tools%20Integrations&c=tag%20tc-unity
[cache-server]: https://docs.unity3d.com/2019.2/Documentation/Manual/CacheServer.html
[unity-accelerator]: https://docs.unity3d.com/Manual/UnityAccelerator.html
