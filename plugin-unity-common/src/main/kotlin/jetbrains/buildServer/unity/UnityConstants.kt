

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
    const val PARAM_NO_QUIT = "noQuit"
    const val PARAM_ARGUMENTS = "arguments"
    const val PARAM_UNITY_VERSION = "unityVersion"
    const val PARAM_UNITY_ROOT = "unityRoot"
    const val PARAM_RUN_EDITOR_TESTS = "runEditorTests"
    const val PARAM_TEST_PLATFORM = "testPlatform"
    const val PARAM_TEST_CATEGORIES = "testCategories"
    const val PARAM_TEST_NAMES = "testNames"
    const val PARAM_SILENT_CRASHES = "silentCrashes"
    const val PARAM_LINE_STATUSES_FILE = "lineStatusesFile"
    const val PARAM_UNITY_LOG_FILE = "logFilePath"
    const val PARAM_VERBOSITY = "verbosity"
    const val PARAM_DETECTION_MODE = "detectionMode"

    const val PARAM_ACTIVATE_LICENSE = "activateLicense" // deprecated, needed for backwards compatibility
    const val PARAM_UNITY_LICENSE_TYPE = "unityLicenseType"
    const val PARAM_UNITY_PERSONAL_LICENSE_CONTENT = Constants.SECURE_PROPERTY_PREFIX + "unityPersonalLicenseContent"
    const val PARAM_SERIAL_NUMBER = Constants.SECURE_PROPERTY_PREFIX + "serialNumber"
    const val PARAM_USERNAME = "username"
    const val PARAM_PASSWORD = Constants.SECURE_PROPERTY_PREFIX + "password"
    const val PARAM_CACHE_SERVER = "cacheServer"

    const val VAR_UNITY_HOME = "UNITY_HOME"
    const val VAR_UNITY_HINT_PATH = "UNITY_HINT_PATH"

    const val UNITY_TOOL_NAME = "Unity"
    const val UNITY_TOOL_DISPLAY_NAME = "Unity"
    const val UNITY_TOOL_DESCRIPTION = "Used by the Unity build runner"
    const val UNITY_TOOL_EXTENSION = "zip"

    const val DETECTION_MODE_AUTO = "auto"
    const val DETECTION_MODE_MANUAL = "manual"

    const val PLUGIN_DOCKER_IMAGE = "plugin.docker.imageId"
    const val DOCKER_WRAPPER_ID = "dockerWrapper"
}