/*
 * Copyright 2000-2021 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    val logFilePath: String
        get() = UnityConstants.PARAM_UNITY_LOG_FILE

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