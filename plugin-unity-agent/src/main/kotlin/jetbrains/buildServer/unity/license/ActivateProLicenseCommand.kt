package jetbrains.buildServer.unity.license

import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine
import jetbrains.buildServer.unity.UnityConstants.BUILD_FEATURE_TYPE
import jetbrains.buildServer.unity.UnityConstants.PARAM_PASSWORD
import jetbrains.buildServer.unity.UnityConstants.PARAM_SERIAL_NUMBER
import jetbrains.buildServer.unity.UnityConstants.PARAM_USERNAME
import jetbrains.buildServer.unity.UnityEnvironment
import jetbrains.buildServer.unity.util.FileSystemService
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class ActivateProLicenseCommand(
    private val runnerContext: BuildRunnerContext,
    fileSystemService: FileSystemService,
) : UnityLicenseCommand(runnerContext, fileSystemService) {

    private lateinit var unityEnvironment: UnityEnvironment
    private lateinit var commandLogFile: Path

    fun withUnityEnvironment(unityEnvironment: UnityEnvironment): ActivateProLicenseCommand {
        this.unityEnvironment = unityEnvironment
        return this
    }

    override fun makeProgramCommandLine(): ProgramCommandLine {
        val feature = runnerContext.build.getBuildFeaturesOfType(BUILD_FEATURE_TYPE).first()

        val arguments = listOf(
            "-quit", "-batchmode", "-nographics",
            *feature.produceArgsForEditor(
                sequenceOf(
                    Pair("-serial", PARAM_SERIAL_NUMBER),
                    Pair("-username", PARAM_USERNAME),
                    Pair("-password", PARAM_PASSWORD),
                )
            ),
            "-logFile", resolvePath(commandLogFile.absolutePathString()),
        )

        return SimpleProgramCommandLine(
            runnerContext.buildParameters.environmentVariables,
            resolvePath(runnerContext.workingDirectory.path),
            resolvePath(unityEnvironment.unityPath),
            arguments,
        )
    }

    override fun beforeProcessStarted() {
        commandLogFile = generateLogFile(runnerContext.build, "activate-license-log-")
    }

    override val logBlockName: String = "Activate Unity license"

    override fun logFile(): Path = commandLogFile
}