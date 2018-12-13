# TeamCity Unity support plugin

It provides support for Unity projects build.

## Features

* Unity detection on build agents
* Unity build runner
* Automatic discovery of Unity build steps in repository

# Compatibility

The plugin is compatible with [TeamCity](https://www.jetbrains.com/teamcity/download/) 2018.1.x and greater.

# Configuration

The plugin searches Unity installations in the following paths:
* Mac OS: `/Applications/Unity*` and `/Applications/Unity/Hub/Editor/*`
* Windows: `%Program Files%/Unity*` and `%Program Files%/Unity/Hub/Editor/*`

# Build

This project uses gradle as the build system. You can easily open it in [IntelliJ IDEA](https://www.jetbrains.com/idea/help/importing-project-from-gradle-model.html) or [Eclipse](http://gradle.org/eclipse/).

# Contributions

We appreciate all kinds of feedback, so please feel free to send a PR or submit an issue.