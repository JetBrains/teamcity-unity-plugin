package jetbrains.buildServer.unity.license.commands

import jetbrains.buildServer.agent.AgentBuildFeature
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine
import jetbrains.buildServer.unity.UnityConstants.BUILD_FEATURE_TYPE
import jetbrains.buildServer.unity.UnityConstants.PARAM_PASSWORD
import jetbrains.buildServer.unity.UnityConstants.PARAM_SERIAL_NUMBER
import jetbrains.buildServer.unity.UnityConstants.PARAM_USERNAME
import jetbrains.buildServer.unity.UnityEnvironment
import jetbrains.buildServer.unity.license.LicenseCommandContext
import kotlin.io.path.absolutePathString

class ActivateProLicenseCommand(
    private val context: LicenseCommandContext,
    onFinish: (Int) -> Unit = {},
) : UnityLicenseCommand(
    context,
    onFinish,
    "Activate Unity license",
    "activate-license-log-",
) {
    private lateinit var unityEnvironment: UnityEnvironment

    fun withUnityEnvironment(unityEnvironment: UnityEnvironment): ActivateProLicenseCommand {
        this.unityEnvironment = unityEnvironment
        return this
    }

    override fun makeProgramCommandLine(): ProgramCommandLine {
        val feature = build.getBuildFeaturesOfType(BUILD_FEATURE_TYPE).first()

        val arguments = buildList {
            addAll(sequenceOf("-quit", "-batchmode", "-nographics"))
            addIfPresent(feature, PARAM_SERIAL_NUMBER, "-serial")
            addIfPresent(feature, PARAM_USERNAME, "-username")
            addIfPresent(feature, PARAM_PASSWORD, "-password")
            addAll(sequenceOf("-logFile", resolvePath(logFile.absolutePathString())))
        }

        return SimpleProgramCommandLine(
            context.environmentVariables,
            resolvePath(context.workingDirectory),
            resolvePath(unityEnvironment.unityPath),
            arguments,
        )
    }

    // We don't fail fast here for backwards compatibility - do we really need this?
    private fun MutableList<String>.addIfPresent(feature: AgentBuildFeature, parameterName: String, editorArgument: String) {
        feature.parameters[parameterName]?.let {
            add(editorArgument)
            add(it.trim())
        }
    }
}
