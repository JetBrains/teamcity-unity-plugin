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

import jetbrains.buildServer.agent.Constants

/**
 * Cargo runner constants.
 */
object UnityConstants {
    const val RUNNER_TYPE = "unity"
    const val RUNNER_DISPLAY_NAME = "Unity"
    const val RUNNER_DESCRIPTION = "Provides build support for Unity projects"
    const val BUILD_FEATURE_TYPE = "UnityBuildFeature"
    const val BUILD_FEATURE_DISPLAY_NAME = "Unity build settings"
    const val UNITY_CONFIG_NAME = "$RUNNER_TYPE.path."

    const val PARAM_PROJECT_PATH = "projectPath"
    const val PARAM_EXECUTE_METHOD = "executeMethod"
    const val PARAM_BUILD_TARGET = "buildTarget"
    const val PARAM_BUILD_PLAYER = "buildPlayer"
    const val PARAM_BUILD_PLAYER_PATH = "buildPlayerPath"
    const val PARAM_NO_GRAPHICS = "noGraphics"
    const val PARAM_ARGUMENTS = "arguments"
    const val PARAM_UNITY_VERSION = "unityVersion"
    const val PARAM_RUN_EDITOR_TESTS = "runEditorTests"
    const val PARAM_TEST_PLATFORM = "testPlatform"
    const val PARAM_TEST_CATEGORIES = "testCategories"
    const val PARAM_TEST_NAMES = "testNames"
    const val PARAM_SILENT_CRASHES = "silentCrashes"
    const val PARAM_LINE_STATUSES_FILE = "lineStatusesFile"
    const val PARAM_UNITY_LOG_FILE = "logFilePath"
    const val PARAM_VERBOSITY = "verbosity"

    const val PARAM_ACTIVATE_LICENSE = "activateLicense"
    const val PARAM_SERIAL_NUMBER = Constants.SECURE_PROPERTY_PREFIX + "serialNumber"
    const val PARAM_USERNAME = "username"
    const val PARAM_PASSWORD = Constants.SECURE_PROPERTY_PREFIX + "password"
    const val PARAM_CACHE_SERVER = "cacheServer"

    const val VAR_UNITY_HOME = "UNITY_HOME"
    const val VAR_UNITY_HINT_PATH = "UNITY_HINT_PATH"
}
