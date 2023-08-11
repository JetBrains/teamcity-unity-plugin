package jetbrains.buildServer.unity.license

import jetbrains.buildServer.agent.AgentBuildFeature
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.unity.UnityEnvironment
import jetbrains.buildServer.unity.UnityLicenseTypeParameter.PERSONAL
import jetbrains.buildServer.unity.UnityLicenseTypeParameter.PROFESSIONAL
import jetbrains.buildServer.unity.util.unityLicenseTypeParam

class UnityLicenseManager(
    private val activatePersonalCommand: ActivatePersonalLicenseCommand,
    private val activateProCommand: ActivateProLicenseCommand,
    private val returnProCommand: ReturnProLicenseCommand,
) {

    fun activateLicense(
        unityEnvironment: UnityEnvironment,
        runnerContext: BuildRunnerContext,
    ): Sequence<CommandExecution> = sequence {
        when (runnerContext.unityLicenseTypeParam()) {
            PROFESSIONAL -> yield(activateProCommand.withUnityEnvironment(unityEnvironment))
            PERSONAL -> yield(activatePersonalCommand.withUnityEnvironment(unityEnvironment))
            else -> {}
        }
    }

    fun returnLicense(
        unityEnvironment: UnityEnvironment,
        runnerContext: BuildRunnerContext,
    ): Sequence<CommandExecution> = sequence {
        when (runnerContext.unityLicenseTypeParam()) {
            PROFESSIONAL -> yield(returnProCommand.withUnityEnvironment(unityEnvironment))
            else -> {}
        }
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