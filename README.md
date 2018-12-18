# TeamCity Unity plugin

[![official JetBrains project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![build status](https://teamcity.jetbrains.com/app/rest/builds/buildType:(id:TeamCityUnityPlugin_Build)/statusIcon.svg)](https://teamcity.jetbrains.com/viewType.html?buildTypeId=TeamCityUnityPlugin_Build&guest=1)

It provides support for building Unity projects on TeamCity.

## Features

* Unity versions detection on build agents
* Unity build runner with smart completions
* Automatic discovery of Unity build steps
* Unity Editor tests reporting

# Download

You can [download the plugin](https://teamcity.jetbrains.com/repository/download/TeamCityUnityPlugin_Build/.lastSuccessful/teamcity-unity-plugin.zip?guest=1) and install it as [an additional TeamCity plugin](https://confluence.jetbrains.com/display/TCDL/Installing+Additional+Plugins).

# Compatibility

The plugin is compatible with [TeamCity](https://www.jetbrains.com/teamcity/download/) 2018.1.x and greater.

# Configuration

The plugin searches Unity installations in the following paths:
* macOS: `/Applications/Unity*` and `/Applications/Unity/Hub/Editor/*`
* Linux: `~/Unity*` and `~/Unity/Hub/Editor/*`
* Windows: `%Program Files%/Unity*` and `%Program Files%/Unity/Hub/Editor/*`

# Build

This project uses gradle as the build system. You can easily open it in [IntelliJ IDEA](https://www.jetbrains.com/idea/help/importing-project-from-gradle-model.html) or [Eclipse](http://gradle.org/eclipse/).

# Contributions

We appreciate all kinds of feedback, so please feel free to send a PR or submit an issue.
