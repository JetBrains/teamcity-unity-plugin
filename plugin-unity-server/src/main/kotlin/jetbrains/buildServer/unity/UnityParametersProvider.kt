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

    val silentCrashes: String
        get() = UnityConstants.PARAM_SILENT_CRASHES

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

    val buildPlayers: List<StandalonePlayer>
        get() = StandalonePlayer.values().toList()

    val verbosity: String
        get() = UnityConstants.PARAM_VERBOSITY

    val verbosityValues: List<Verbosity>
        get() = Verbosity.values().toList()
}