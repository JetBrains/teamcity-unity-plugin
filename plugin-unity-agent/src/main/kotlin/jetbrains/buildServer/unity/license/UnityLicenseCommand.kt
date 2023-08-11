package jetbrains.buildServer.unity.license

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.agent.runner.TerminationAction.KILL_PROCESS_TREE
import jetbrains.buildServer.messages.DefaultMessagesInfo.createBlockEnd
import jetbrains.buildServer.messages.DefaultMessagesInfo.createBlockStart
import jetbrains.buildServer.unity.util.FileSystemService
import java.io.File
import java.lang.System.lineSeparator
import java.nio.file.Path

abstract class UnityLicenseCommand(
    private val runnerContext: BuildRunnerContext,
    private val fileSystemService: FileSystemService,
) : CommandExecution {

    protected abstract val logBlockName: String

    protected abstract fun logFile(): Path

    private val buildLogger get() = runnerContext.build.buildLogger

    override fun processStarted(programCommandLine: String, workingDirectory: File) {
        buildLogger.logMessage(createBlockStart(logBlockName, BUILD_LOG_BLOCK_TYPE))
        buildLogger.message("Starting: $programCommandLine")
    }

    override fun processFinished(exitCode: Int) {
        if (exitCode != 0)
            buildLogger.warning("Process exited with code ${exitCode}. Unity log:${lineSeparator()}${readLogFile()}")

        if (LOG.isDebugEnabled)
            LOG.debug("Unity log:${lineSeparator()}${readLogFile()}")

        val blockEnd = createBlockEnd(logBlockName, BUILD_LOG_BLOCK_TYPE)
        buildLogger.logMessage(blockEnd)
    }

    override fun onStandardOutput(text: String) = Unit
    override fun onErrorOutput(text: String) = Unit
    override fun interruptRequested() = KILL_PROCESS_TREE
    override fun isCommandLineLoggingEnabled() = true

    protected fun resolvePath(path: String) = runnerContext.virtualContext.resolvePath(path)

    protected fun generateLogFile(build: AgentRunningBuild, prefix: String): Path =
        fileSystemService.createTempFile(
            build.agentTempDirectory.toPath(), prefix, suffix = "-${build.buildId}.txt"
        )

    private fun readLogFile() = fileSystemService.readText(logFile())

    companion object {
        private val LOG = Logger.getInstance(UnityLicenseCommand::class.java.name)

        private const val BUILD_LOG_BLOCK_TYPE = "unity"
    }
}