package jetbrains.buildServer.unity

import jetbrains.buildServer.serverSide.BuildFeature
import jetbrains.buildServer.web.openapi.PluginDescriptor

class UnityBuildFeature(descriptor: PluginDescriptor) : BuildFeature() {

    private val _editUrl: String = descriptor.getPluginResourcesPath("editBuildFeature.jsp")

    override fun getType() = UnityConstants.BUILD_FEATURE_TYPE

    override fun getDisplayName() = UnityConstants.BUILD_FEATURE_DISPLAY_NAME

    override fun getEditParametersUrl() = _editUrl

    override fun isMultipleFeaturesPerBuildTypeAllowed() = false

    override fun describeParameters(parameters: Map<String, String>): String {
        val builder = StringBuilder()
        parameters[UnityConstants.PARAM_ACTIVATE_LICENSE]?.let {
            if (it.toBoolean()) {
                builder.append("Activate Unity license: ON\n")
            }
        }
        parameters[UnityConstants.PARAM_CACHE_SERVER]?.let {
            if (it.isNotEmpty()) {
                builder.append("Use cache server: ${it.trim()}\n")
            }
        }
        return builder.toString().trim()
    }
}