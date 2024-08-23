# Changelog

All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased

### Added

### Changed

### Fixed

## 1.4.2 - 2024-08-23

### Fixed

- Build commands running twice when the license scope is set for the entire build or when no license activation is enabled at all.
This bug was introduced in version 1.4.1 [TW-89422](https://youtrack.jetbrains.com/issue/TW-89422)

## 1.4.1 - 2024-07-19

### Fixed

- Return build-step-scoped, serial-based (Pro) license on build interruption [TW-88607](https://youtrack.jetbrains.com/issue/TW-88607)

## 1.4.0 - 2024-07-09

### Added

- Unity Accelerator support [TW-81927](https://youtrack.jetbrains.com/issue/TW-81927)

## 1.3.0 - 2024-06-11

### Added

- Allow acquiring a license before a build and returning it afterward [TW-85266](https://youtrack.jetbrains.com/issue/TW-85266) (for serial-based Unity licenses)

### Changed

- Custom command line arguments field is now multiline and expandable [TW-84677](https://youtrack.jetbrains.com/issue/TW-84677)

## 1.2.1 - 2023-12-12

### Fixed

- Unity environment detection fails in a Windows container [TW-85189](https://youtrack.jetbrains.com/issue/TW-85189)
- Broken Unity environment detection in a Linux container since version 1.2.0 [TW-85468](https://youtrack.jetbrains.com/issue/TW-85468)

## 1.2.0 - 2023-11-08

### Added

- The plugin can now pick up the Unity version specified in the project settings (ProjectVersion.txt).
This means that this version will be pre-filled in auto-detected build steps. Additionally, when in 'auto'
Unity detection mode and no version is explicitly specified, the version from the project will be chosen if it's available on an agent
[TW-59619](https://youtrack.jetbrains.com/issue/TW-59619), [TW-84683](https://youtrack.jetbrains.com/issue/TW-84683)

### Fixed

- Previously, random Unity version was selected when no one is specified.
The new behavior ensures the latest Unity version is consistently chosen [TW-84210](https://youtrack.jetbrains.com/issue/TW-84210)
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
- [TW-59710](https://youtrack.jetbrains.com/issue/TW-59710)
\: Build feature now generates agent requirement when Unity version specified
- [TW-68480](https://youtrack.jetbrains.com/issue/TW-68480)
\: Unity CN (Chinese version) installation discovery on Windows
