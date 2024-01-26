

package jetbrains.buildServer.unity

import jetbrains.buildServer.serverSide.Parameter
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SimpleParameter
import jetbrains.buildServer.serverSide.parameters.types.PasswordsProvider

class UnityPasswordsProvider : PasswordsProvider {

    override fun getPasswordParameters(build: SBuild): List<Parameter> {
        val feature = build.getBuildFeaturesOfType(UnityConstants.BUILD_FEATURE_TYPE).firstOrNull()?:return emptyList()

        val parameters = mutableListOf<Parameter>()

        UnityConstants.PARAM_SERIAL_NUMBER.apply {
            feature.parameters[this]?.let {
                parameters.add(SimpleParameter(this, it.trim()))
            }
        }
        UnityConstants.PARAM_PASSWORD.apply {
            feature.parameters[this]?.let {
                parameters.add(SimpleParameter(this, it.trim()))
            }
        }
        UnityConstants.PARAM_UNITY_PERSONAL_LICENSE_CONTENT.apply {
            feature.parameters[this]?.let {
                parameters.add(SimpleParameter(this, it.trim()))
            }
        }

        return parameters
    }
}