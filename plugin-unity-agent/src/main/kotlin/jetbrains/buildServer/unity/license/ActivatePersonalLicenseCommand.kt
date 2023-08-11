package jetbrains.buildServer.unity.license

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine
import jetbrains.buildServer.unity.UnityEnvironment
import jetbrains.buildServer.unity.util.FileSystemService
import jetbrains.buildServer.unity.util.unityPersonalLicenseContentParam
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class ActivatePersonalLicenseCommand(
    private val runnerContext: BuildRunnerContext,
    private val fileSystemService: FileSystemService,
) : UnityLicenseCommand(runnerContext, fileSystemService) {

    private lateinit var unityEnvironment: UnityEnvironment
    private lateinit var tempLicenseFile: Path
    private lateinit var commandLogFile: Path

    fun withUnityEnvironment(unityEnvironment: UnityEnvironment): ActivatePersonalLicenseCommand {
        this.unityEnvironment = unityEnvironment
        return this
    }

    override fun makeProgramCommandLine(): ProgramCommandLine {
        tempLicenseFile = fileSystemService.createTempFile(
            directory = runnerContext.build.buildTempDirectory.toPath(),
            prefix = "unity-personal-license-",
            suffix = ".ulf",
        )

        val licenseContent = runnerContext.unityPersonalLicenseContentParam()
            ?: throw IllegalStateException("Personal license content is not provided")
        fileSystemService.writeText(tempLicenseFile, licenseContent)
        val arguments = listOf(
            "-quit", "-batchmode", "-nographics",
            "-manualLicenseFile", resolvePath(tempLicenseFile.absolutePathString()),
            "-logFile", resolvePath(commandLogFile.absolutePathString()),
        )

        return SimpleProgramCommandLine(
            runnerContext.buildParameters.environmentVariables,
            resolvePath(runnerContext.workingDirectory.path),
            resolvePath(unityEnvironment.unityPath),
            arguments,
        )
    }

    override val logBlockName = "Activate Unity license"

    override fun logFile(): Path = commandLogFile

    override fun beforeProcessStarted() {
        commandLogFile = generateLogFile(runnerContext.build, "activate-license-log-")
    }

    override fun processFinished(exitCode: Int) {
        super.processFinished(exitCode)
        try {
            if (!fileSystemService.deleteFile(tempLicenseFile))
                LOG.warn("The .ulf file does not exist")
        } catch (e: Exception) {
            LOG.error("Failed to delete .ulf file", e)
        }
    }

    companion object {
        private val LOG = Logger.getInstance(UnityLicenseCommand::class.java.name)
    }
}