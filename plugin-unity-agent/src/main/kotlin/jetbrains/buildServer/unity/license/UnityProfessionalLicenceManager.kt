package jetbrains.buildServer.unity.license

import jetbrains.buildServer.agent.AgentBuildFeature
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.unity.UnityConstants.BUILD_FEATURE_TYPE
import jetbrains.buildServer.unity.UnityConstants.PARAM_ACTIVATE_LICENSE
import jetbrains.buildServer.unity.UnityEnvironment

class UnityProfessionalLicenceManager(
    private val activateCommand: ActivateUnityLicenseCommand,
    private val returnCommand: ReturnUnityLicenseCommand,
) {

    private fun isActivationRequired(runnerContext: BuildRunnerContext): Boolean {
        val feature = runnerContext.build.getBuildFeaturesOfType(BUILD_FEATURE_TYPE).firstOrNull() ?: return false

        val parameters = feature.parameters
        parameters[PARAM_ACTIVATE_LICENSE]?.let { activateLicense ->
            if (activateLicense.toBoolean()) {
                return true
            }
        }

        return false
    }

    fun activateLicence(
        unityEnvironment: UnityEnvironment,
        runnerContext: BuildRunnerContext
    ): Sequence<CommandExecution> = sequence {
        if (!isActivationRequired(runnerContext)) {
            return@sequence
        }
        activateCommand.withUnityEnvironment(unityEnvironment)
        yield(activateCommand)
    }

    fun returnLicence(
        unityEnvironment: UnityEnvironment,
        runnerContext: BuildRunnerContext
    ): Sequence<CommandExecution> = sequence {
        if (!isActivationRequired(runnerContext)) {
            return@sequence
        }
        returnCommand.withUnityEnvironment(unityEnvironment)
        yield(returnCommand)
    }
}

fun AgentBuildFeature.produceArgsForEditor(args: Sequence<Pair<String, String>>) = sequence {
    args.forEach { (editorArg, featureParameter) ->
        this@produceArgsForEditor.parameters[featureParameter]?.let {
            yield(editorArg)
            yield(it.trim())
        }
    }
}.toList().toTypedArray()