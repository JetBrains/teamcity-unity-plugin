/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.unity

/**
 * Provides parameters for unity runner.
 */
class UnityParametersProvider {
    val projectPath: String
        get() = UnityConstants.PARAM_PROJECT_PATH

    val executeMethod: String
        get() = UnityConstants.PARAM_EXECUTE_METHOD

    val buildTarget: String
        get() = UnityConstants.PARAM_BUILD_TARGET

    val buildPlayer: String
        get() = UnityConstants.PARAM_BUILD_PLAYER

    val buildPlayerPath: String
        get() = UnityConstants.PARAM_BUILD_PLAYER_PATH

    val unityVersion: String
        get() = UnityConstants.PARAM_UNITY_VERSION

    val noGraphics: String
        get() = UnityConstants.PARAM_NO_GRAPHICS

    val arguments: String
        get() = UnityConstants.PARAM_ARGUMENTS

    val runEditorTests: String
        get() = UnityConstants.PARAM_RUN_EDITOR_TESTS

    val testPlatform: String
        get() = UnityConstants.PARAM_TEST_PLATFORM

    val testCategories: String
        get() = UnityConstants.PARAM_TEST_CATEGORIES

    val testNames: String
        get() = UnityConstants.PARAM_TEST_NAMES

    val lineStatusesFile: String
        get() = UnityConstants.PARAM_LINE_STATUSES_FILE

    val activateLicense: String
        get() = UnityConstants.PARAM_ACTIVATE_LICENSE

    val serialNumber: String
        get() = UnityConstants.PARAM_SERIAL_NUMBER

    val username: String
        get() = UnityConstants.PARAM_USERNAME

    val password: String
        get() = UnityConstants.PARAM_PASSWORD

    val cacheServer: String
        get() = UnityConstants.PARAM_CACHE_SERVER

    val buildPlayers: List<Pair<String, String>>
        get() = BUILD_PLAYERS.map { it.toPair() }

    val verbosity: String
        get() = UnityConstants.PARAM_VERBOSITY

    val verbosityValues: List<Verbosity>
        get() = Verbosity.values().toList()

    companion object {
        val BUILD_PLAYERS = mapOf(
                "buildLinux32Player" to "Linux 32-bit",
                "buildLinux64Player" to "Linux 64-bit",
                "buildLinuxUniversalPlayer" to "Linux 32-bit and 64-bit",
                "buildOSXPlayer" to "Mac OSX 32-bit",
                "buildOSX64Player" to "Mac OSX 64-bit",
                "buildOSXUniversalPlayer" to "Mac OSX 32-bit and 64-bit",
                "buildWindowsPlayer" to "Windows 32-bit",
                "buildWindows64Player" to "Windows 64-bit"
        )
    }
}