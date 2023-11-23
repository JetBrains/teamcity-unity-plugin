# Changelog
All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased

### Added

### Changed

### Fixed
- Unity environment detection fails in a Windows container [TW-85189](https://youtrack.jetbrains.com/issue/TW-85189)

## 1.2.0 - 2023-11-08

### Added
- The plugin can now pick up the Unity version specified in the project settings (ProjectVersion.txt). 
This means that this version will be pre-filled in auto-detected build steps. Additionally, when in 'auto' 
Unity detection mode and no version is explicitly specified, the version from the project will be chosen if it's available on an agent
[TW-59619](https://youtrack.jetbrains.com/issue/TW-59619/Unity-Plugin-set-Unity-Version-from-ProjectVersion-file-for-auto-detected-build-step), [TW-84683](https://youtrack.jetbrains.com/issue/TW-84683/Unity-Plugin-use-Unity-version-from-a-project-in-auto-detection-mode-when-no-version-is-specified)

### Fixed
- Previously, random Unity version was selected when no one is specified. 
The new behavior ensures the latest Unity version is consistently chosen [TW-84210](https://youtrack.jetbrains.com/issue/TW-84210/Unity-plugin-random-Unity-version-is-selected-when-no-one-is-specified)
- Runner now has a logo

## 1.1.1 - 2023-09-20

### Fixed
- Support compatibility with Java 1.8
- Support for class name in tests reporting

## 1.1.0 - 2023-09-05

### Added
- Support for running Unity inside a container starting from 2023.09 TeamCity version
- Support for Unity Personal license activation via .ulf file upload
- Add option to remove '-quit' argument

## 1.0.4 - 2023-05-03

### Changed
- License return now happens just before a build finishes

## 1.0.3 - 2023-03-17

### Fixed
- Version detection on Linux when Unity was distributed as a TeamCity tool

## 1.0.2 - 2023-03-01

### Fixed
- Licence activation on "fresh" agents

## 1.0.1 - 2023-02-23

### Changed
- Correct minimal compatible TeamCity version was specified (2022.10)

## 1.0.0 - 2023-02-20
As of now, the plugin is switching to SemVer versioning. This is the initial plugin release with the new scheme.
So if you used an old version of the plugin, please uninstall it before installing the new one.

### Added
- Managing Unity as a TC tool support - Credits to [AaronZurawski](https://github.com/AaronZurawski)

### Changed
- Editor console output now fully captured by TC
- The plugin is now only compatible with TeamCity 2020.1+

### Fixed
- Automatic licence return on the latest versions of Unity (via build feature)
- [TW-59710](https://youtrack.jetbrains.com/issue/TW-59710/Unity-build-feature-setting-Unity-version-doesnt-generate-an-agent-requirement) 
\: Build feature now generates agent requirement when Unity version specified
- [TW-68480](https://youtrack.jetbrains.com/issue/TW-68480/Unity-plugin-Unity-not-detected-with-no-apparent-reason-why)
\: Unity CN (Chinese version) installation discovery on Windows
