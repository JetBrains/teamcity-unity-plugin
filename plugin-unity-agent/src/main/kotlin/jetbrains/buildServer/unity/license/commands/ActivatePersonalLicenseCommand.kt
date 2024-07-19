package jetbrains.buildServer.unity.license.commands

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine
import jetbrains.buildServer.unity.UnityConstants.BUILD_FEATURE_TYPE
import jetbrains.buildServer.unity.UnityConstants.PARAM_UNITY_PERSONAL_LICENSE_CONTENT
import jetbrains.buildServer.unity.UnityEnvironment
import jetbrains.buildServer.unity.license.LicenseCommandContext
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class ActivatePersonalLicenseCommand(
    private val context: LicenseCommandContext,
    onFinish: (Int) -> Unit = {},
) : UnityLicenseCommand(
    context,
    onFinish,
    "Activate Personal Unity license",
    "activate-personal-license-log-",
) {

    private lateinit var unityEnvironment: UnityEnvironment
    private lateinit var tempLicenseFile: Path

    fun withUnityEnvironment(unityEnvironment: UnityEnvironment): ActivatePersonalLicenseCommand {
        this.unityEnvironment = unityEnvironment
        return this
    }

    override fun makeProgramCommandLine(): ProgramCommandLine {
        tempLicenseFile = fileSystemService.createTempFile(
            directory = build.buildTempDirectory.toPath(),
            prefix = "unity-personal-license-",
            suffix = ".ulf",
        )

        val licenseContent = build.licenseContent()
            ?: throw IllegalStateException("Personal license content is not provided")
        fileSystemService.writeText(tempLicenseFile, licenseContent)
        val arguments = listOf(
            "-quit",
            "-batchmode",
            "-nographics",
            "-manualLicenseFile",
            resolvePath(tempLicenseFile.absolutePathString()),
            "-logFile",
            resolvePath(logFile.absolutePathString()),
        )

        return SimpleProgramCommandLine(
            context.environmentVariables,
            resolvePath(context.workingDirectory),
            resolvePath(unityEnvironment.unityPath),
            arguments,
        )
    }

    override fun processFinished(exitCode: Int) {
        super.processFinished(exitCode)
        try {
            if (!fileSystemService.deleteFile(tempLicenseFile)) {
                LOG.warn("The .ulf file does not exist")
            }
        } catch (e: Exception) {
            LOG.error("Failed to delete .ulf file", e)
        }
    }

    private fun AgentRunningBuild.licenseContent(): String? =
        this
            .getBuildFeaturesOfType(BUILD_FEATURE_TYPE)
            .firstOrNull()
            ?.parameters
            ?.let { it[PARAM_UNITY_PERSONAL_LICENSE_CONTENT] }

    companion object {
        private val LOG = Logger.getInstance(UnityLicenseCommand::class.java.name)
    }
}
