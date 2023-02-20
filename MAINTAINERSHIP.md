# Maintainership

## Release

To release a new version, follow these steps.

1. Choose the new version according to [Semantic Versioning][semver]. It should consist of three numbers (i.e. `1.0.0`).
2. Make sure there are some entries under `Unreleased` section in the `CHANGELOG.md` 
3. Execute `./bump-version.sh` script passing a version as an argument. For example 
```shell
./bump-version.sh 1.2.1
```
It will switch to the master branch (if it's not already there), update the `CHANGELOG.md`, tag the current `HEAD` with `v1.2.1`,
and push everything to the origin remote.

The new version of the plugin will be published to [marketplace][marketplace.plugin-page] automatically.

[semver]: https://semver.org/spec/v2.0.0.html
[marketplace.plugin-page]: https://plugins.jetbrains.com/plugin/11453-unity-support