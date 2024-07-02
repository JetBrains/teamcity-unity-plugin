package jetbrains.buildServer.unity.license.commands

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.agent.runner.TerminationAction.KILL_PROCESS_TREE
import jetbrains.buildServer.messages.DefaultMessagesInfo.createBlockEnd
import jetbrains.buildServer.messages.DefaultMessagesInfo.createBlockStart
import jetbrains.buildServer.unity.license.LicenseCommandContext
import java.io.File
import java.lang.System.lineSeparator
import java.nio.file.Path

abstract class UnityLicenseCommand(
    private val context: LicenseCommandContext,
    private val logBlockName: String,
    private val logFilePrefix: String,
) : CommandExecution {

    protected val build = context.build
    protected val fileSystemService = context.fileSystemService
    protected lateinit var logFile: Path
    private val buildLogger = build.buildLogger

    override fun beforeProcessStarted() {
        buildLogger.logMessage(createBlockStart(logBlockName, BUILD_LOG_BLOCK_TYPE))

        logFile = fileSystemService.createTempFile(
            build.agentTempDirectory.toPath(),
            logFilePrefix,
            suffix = "-${build.buildId}.txt",
        )
    }

    override fun processStarted(programCommandLine: String, workingDirectory: File) = Unit

    override fun processFinished(exitCode: Int) {
        if (exitCode != 0) {
            buildLogger.warning("Process exited with code $exitCode. Unity log:${lineSeparator()}${readLogFile()}")
        }

        if (LOG.isDebugEnabled) {
            LOG.debug("Unity log:${lineSeparator()}${readLogFile()}")
        }

        val blockEnd = createBlockEnd(logBlockName, BUILD_LOG_BLOCK_TYPE)
        buildLogger.logMessage(blockEnd)
    }

    override fun onStandardOutput(text: String) = Unit
    override fun onErrorOutput(text: String) = Unit
    override fun interruptRequested() = KILL_PROCESS_TREE
    override fun isCommandLineLoggingEnabled() = true

    protected fun resolvePath(path: String) = context.resolvePath(path)

    private fun readLogFile() = fileSystemService.readText(logFile)

    companion object {
        private val LOG = Logger.getInstance(UnityLicenseCommand::class.java.name)

        private const val BUILD_LOG_BLOCK_TYPE = "unity"
    }
}
