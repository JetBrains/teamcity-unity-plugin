

package jetbrains.buildServer.unity

import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.serverSide.BuildFeature
import jetbrains.buildServer.unity.UnityConstants.BUILD_FEATURE_DISPLAY_NAME
import jetbrains.buildServer.unity.UnityConstants.BUILD_FEATURE_TYPE
import jetbrains.buildServer.unity.UnityConstants.DETECTION_MODE_AUTO
import jetbrains.buildServer.unity.UnityConstants.DETECTION_MODE_MANUAL
import jetbrains.buildServer.unity.UnityConstants.PARAM_ACTIVATE_LICENSE
import jetbrains.buildServer.unity.UnityConstants.PARAM_CACHE_SERVER
import jetbrains.buildServer.unity.UnityConstants.PARAM_DETECTION_MODE
import jetbrains.buildServer.unity.UnityConstants.PARAM_UNITY_LICENSE_TYPE
import jetbrains.buildServer.unity.UnityConstants.PARAM_UNITY_ROOT
import jetbrains.buildServer.unity.UnityConstants.PARAM_UNITY_VERSION
import jetbrains.buildServer.web.openapi.PluginDescriptor

class UnityBuildFeature(descriptor: PluginDescriptor) : BuildFeature() {

    private val _editUrl: String = descriptor.getPluginResourcesPath("editBuildFeature.jsp")

    override fun getType() = BUILD_FEATURE_TYPE

    override fun getDisplayName() = BUILD_FEATURE_DISPLAY_NAME

    override fun getEditParametersUrl() = _editUrl

    override fun isMultipleFeaturesPerBuildTypeAllowed() = false

    override fun describeParameters(parameters: Map<String, String>): String {
        val builder = StringBuilder()
        if (parameters[PARAM_ACTIVATE_LICENSE].toBoolean() ||
            UnityLicenseTypeParameter.from(parameters[PARAM_UNITY_LICENSE_TYPE] ?: "") != null
        ) {
            builder.append("Activate Unity license: ON\n")
        }
        parameters[PARAM_CACHE_SERVER]?.let {
            if (it.isNotEmpty()) {
                builder.append("Use cache server: ${it.trim()}\n")
            }
        }

        val detectionMode = parameters[PARAM_DETECTION_MODE]
        detectionMode?.let {
            val prefix = "Unity installation: detection: $detectionMode"

            val unityVersion = parameters[PARAM_UNITY_VERSION]
            if (it == DETECTION_MODE_AUTO && unityVersion != null) {
                builder.append("$prefix, version: $unityVersion")
            }

            val unityRoot = parameters[PARAM_UNITY_ROOT]
            if (it == DETECTION_MODE_MANUAL && unityRoot != null) {
                builder.append("$prefix, unity root: $unityRoot")
            }
        }

        return builder.toString().trim()
    }

    override fun getRequirements(params: MutableMap<String, String>?): MutableCollection<Requirement> {
        return mutableListOf()
    }
}
