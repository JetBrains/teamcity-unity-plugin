# TeamCity Unity plugin

[![official JetBrains project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![build status](https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityUnityPlugin_Build)/statusIcon.svg)](https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityUnityPlugin_Build&guest=1)

The plugin supports building Unity projects on TeamCity.

## Features

* Unity versions detection on build agents
* Unity build runner with smart completions
* Automatic discovery of Unity build steps
* Unity Editor tests reporting

# Download

You can [download the plugin](https://plugins.jetbrains.com/plugin/11453-unity-support) and install it as [an additional TeamCity plugin](https://confluence.jetbrains.com/display/TCDL/Installing+Additional+Plugins).

# Compatibility

The plugin is compatible with [TeamCity](https://www.jetbrains.com/teamcity/download/) 2018.1.x and greater.

# Configuration

The plugin searches for Unity installations in the following directories:
* macOS: `/Applications/Unity*` and `/Applications/Unity/Hub/Editor/*`
* Linux: `~/Unity*` and `~/Unity/Hub/Editor/*`
* Windows: `%Program Files%/Unity*` and `%Program Files%/Unity/Hub/Editor/*`

All detected Unity versions will be reported as build agent configuration parameters with the `unity.path.%unityVersion%` prefix.

# Build

This project uses Gradle as the build system. You can easily open it in [IntelliJ IDEA](https://www.jetbrains.com/idea/help/importing-project-from-gradle-model.html) or [Eclipse](http://gradle.org/eclipse/).

# Contributions

We appreciate all kinds of feedback, so please feel free to send a PR or submit an issue.
