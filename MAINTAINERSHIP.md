# Maintainership

## Release

To release a new version, follow these steps.

1. Choose the new version according to [Semantic Versioning][semver]. It should consist of three numbers (i.e. `1.0.0`).
2. Make sure there are some entries under `Unreleased` section in the `CHANGELOG.md` 
3. Execute the following Gradle task to update the changelog 
   (this task comes from the [plugin][gradle-changelog-plugin] we use to keep a changelog)
    ```shell
    ./gradlew patchChangelog -Pversion="$version"
    ```
4. Open a pull request and merge changes (you could do it beforehand in any other pr)
5. Switch to a commit you want to tag (usually it's the HEAD of the master branch) and execute
    ```shell
    ./tag-release-and-push.sh
    ```

It will tag the current `HEAD` with latest version from the changelog, and push it to the origin remote.

The new version of the plugin will be published to [marketplace][marketplace.plugin-page] automatically.

[semver]: https://semver.org/spec/v2.0.0.html
[marketplace.plugin-page]: https://plugins.jetbrains.com/plugin/11453-unity-support
[gradle-changelog-plugin]: https://github.com/JetBrains/gradle-changelog-plugin