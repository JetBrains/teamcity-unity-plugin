package jetbrains.buildServer.unity.util

import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.unity.UnityConstants.BUILD_FEATURE_TYPE
import jetbrains.buildServer.unity.UnityConstants.PARAM_ACTIVATE_LICENSE
import jetbrains.buildServer.unity.UnityConstants.PARAM_UNITY_LICENSE_SCOPE
import jetbrains.buildServer.unity.UnityConstants.PARAM_UNITY_LICENSE_TYPE
import jetbrains.buildServer.unity.UnityConstants.PARAM_UNITY_PERSONAL_LICENSE_CONTENT
import jetbrains.buildServer.unity.UnityConstants.PARAM_UNITY_ROOT
import jetbrains.buildServer.unity.UnityConstants.PARAM_UNITY_VERSION
import jetbrains.buildServer.unity.UnityLicenseScope
import jetbrains.buildServer.unity.UnityLicenseTypeParameter
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

fun BuildRunnerContext.unityLicenseTypeParam() = this.build.unityLicenseTypeParam()

fun AgentRunningBuild.unityLicenseTypeParam(): UnityLicenseTypeParameter? {
    unityBuildFeatureParams(this)?.let { params ->
        if (params[PARAM_ACTIVATE_LICENSE].toBoolean()) {
            return UnityLicenseTypeParameter.PROFESSIONAL
        }
        params[PARAM_UNITY_LICENSE_TYPE]?.let { licenseTypeId ->
            return UnityLicenseTypeParameter.from(licenseTypeId)
        }
    }
    return null
}

fun BuildRunnerContext.unityPersonalLicenseContentParam(): String? {
    unityBuildFeatureParams(this)?.let { params ->
        return params[PARAM_UNITY_PERSONAL_LICENSE_CONTENT]
    }
    return null
}

fun AgentRunningBuild.unityLicenseScopeParam(): UnityLicenseScope? {
    unityBuildFeatureParams(this)?.let { params ->
        params[PARAM_UNITY_LICENSE_SCOPE]?.let { licenseScopeId ->
            return UnityLicenseScope.from(licenseScopeId)
        }
    }
    return null
}

private fun unityBuildFeatureParams(runnerContext: BuildRunnerContext) = unityBuildFeatureParams(runnerContext.build)

private fun unityBuildFeatureParams(build: AgentRunningBuild) =
    build.getBuildFeaturesOfType(BUILD_FEATURE_TYPE).firstOrNull()?.parameters
