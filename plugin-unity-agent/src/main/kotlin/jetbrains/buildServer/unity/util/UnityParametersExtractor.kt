package jetbrains.buildServer.unity.util

import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.unity.UnityConstants.BUILD_FEATURE_TYPE
import jetbrains.buildServer.unity.UnityConstants.PARAM_UNITY_ROOT
import jetbrains.buildServer.unity.UnityConstants.PARAM_UNITY_VERSION
import jetbrains.buildServer.unity.UnityVersion
import jetbrains.buildServer.unity.UnityVersion.Companion.tryParseVersion

/**
 * Searches for "unityRoot" parameter in the following places:
 * 1. Runner parameters
 * 2. Parameters of "UnityBuildFeature"
 *
 * @return the value of "unityRoot" parameter or null
 */
fun BuildRunnerContext.unityRootParam(): String? {
    this.runnerParameters[PARAM_UNITY_ROOT]?.let { return it }
    unityBuildFeatureParams(this)?.let { return it[PARAM_UNITY_ROOT] }
    return null
}

/**
 * Searches for "unityVersion" parameter in the following places:
 * 1. Runner parameters
 * 2. Parameters of "UnityBuildFeature"
 *
 * @return the value of "unityVersion" parameter or null
 */
fun BuildRunnerContext.unityVersionParam(): UnityVersion? {
    this.runnerParameters[PARAM_UNITY_VERSION]?.trim()?.let { v ->
        return tryParseVersion(v)
    }

    unityBuildFeatureParams(this)?.let { params ->
        params[PARAM_UNITY_VERSION]?.let { v -> return tryParseVersion(v) }
    }
    return null
}

private fun unityBuildFeatureParams(runnerContext: BuildRunnerContext) =
    runnerContext.build.getBuildFeaturesOfType(BUILD_FEATURE_TYPE).firstOrNull()?.parameters