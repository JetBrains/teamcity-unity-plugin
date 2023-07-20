package jetbrains.buildServer.unity.license

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine
import jetbrains.buildServer.agent.runner.TerminationAction.KILL_PROCESS_TREE
import jetbrains.buildServer.messages.DefaultMessagesInfo.createBlockEnd
import jetbrains.buildServer.messages.DefaultMessagesInfo.createBlockStart
import jetbrains.buildServer.unity.UnityConstants.BUILD_FEATURE_TYPE
import jetbrains.buildServer.unity.UnityConstants.PARAM_PASSWORD
import jetbrains.buildServer.unity.UnityConstants.PARAM_USERNAME
import jetbrains.buildServer.unity.UnityEnvironment
import jetbrains.buildServer.unity.util.FileSystemService
import java.io.File

class ReturnUnityLicenseCommand(
    private val runnerContext: BuildRunnerContext,
    private val fileSystemService: FileSystemService
) : CommandExecution {

    private lateinit var unityEnvironment: UnityEnvironment
    private lateinit var logFile: File

    private val buildLogger get() = runnerContext.build.buildLogger

    fun withUnityEnvironment(unityEnvironment: UnityEnvironment) {
        this.unityEnvironment = unityEnvironment
    }

    override fun processStarted(programCommandLine: String, workingDirectory: File) {
        buildLogger.logMessage(createBlockStart(BUILD_LOG_BLOCK, BUILD_LOG_BLOCK_TYPE))
        buildLogger.message("Starting: $programCommandLine")
    }

    override fun processFinished(exitCode: Int) {
        if (exitCode != 0)
            buildLogger.warning("Process exited with code ${exitCode}. Unity log:\n" + readLogFile())

        if (LOG.isDebugEnabled)
            LOG.debug("Unity log:\n${readLogFile()}")

        val blockEnd = createBlockEnd(BUILD_LOG_BLOCK, BUILD_LOG_BLOCK_TYPE)
        buildLogger.logMessage(blockEnd)
    }

    override fun makeProgramCommandLine(): ProgramCommandLine {
        val feature = runnerContext.build.getBuildFeaturesOfType(BUILD_FEATURE_TYPE).first()
        val arguments = listOf(
            "-quit", "-batchmode", "-nographics", "-returnlicense",
            *feature.produceArgsForEditor(
                sequenceOf(
                    Pair("-username", PARAM_USERNAME),
                    Pair("-password", PARAM_PASSWORD)
                )
            ),
            "-logFile", resolvePath(logFile.absolutePath)
        )

        return SimpleProgramCommandLine(
            runnerContext.buildParameters.environmentVariables,
            resolvePath(runnerContext.workingDirectory.path),
            resolvePath(unityEnvironment.unityPath),
            arguments
        )
    }

    override fun beforeProcessStarted() {
        logFile = generateLogFile(runnerContext.build)
    }

    private fun generateLogFile(build: AgentRunningBuild): File = fileSystemService.createTempFile(
        build.agentTempDirectory,
        LOG_TMP_FILE_NAME_PREFIX,
        "-${build.buildId}.txt"
    )

    private fun readLogFile() = fileSystemService.readText(logFile)

    override fun onStandardOutput(text: String) {}
    override fun onErrorOutput(text: String) {}
    override fun interruptRequested() = KILL_PROCESS_TREE
    override fun isCommandLineLoggingEnabled() = true

    private fun resolvePath(path: String) = runnerContext.virtualContext.resolvePath(path)

    companion object {
        private val LOG = Logger.getInstance(ReturnUnityLicenseCommand::class.java.name)

        private const val BUILD_LOG_BLOCK = "Return Unity license"
        private const val BUILD_LOG_BLOCK_TYPE = "unity"
        private const val LOG_TMP_FILE_NAME_PREFIX = "unityBuildLog-"
    }
}