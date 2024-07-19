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
    private val onFinish: (Int) -> Unit,
    private val commandName: String,
    private val logFilePrefix: String,
) : CommandExecution {

    protected val build = context.build
    protected val fileSystemService = context.fileSystemService
    protected lateinit var logFile: Path

    override fun beforeProcessStarted() {
        context.buildLogger.logMessage(createBlockStart(commandName, BUILD_LOG_BLOCK_TYPE))

        logFile = fileSystemService.createTempFile(
            build.agentTempDirectory.toPath(),
            logFilePrefix,
            suffix = "-${build.buildId}.txt",
        )
    }

    override fun processStarted(programCommandLine: String, workingDirectory: File) = Unit

    override fun processFinished(exitCode: Int) {
        LOG.debug("License command \"$commandName\" has finished with exit code: $exitCode")

        if (exitCode != 0) {
            context.buildLogger.warning("Process exited with code $exitCode. Unity log:${lineSeparator()}${readLogFile()}")
        }

        if (LOG.isDebugEnabled) {
            LOG.debug("Unity log:${lineSeparator()}${readLogFile()}")
        }

        context.buildLogger.logMessage(createBlockEnd(commandName, BUILD_LOG_BLOCK_TYPE))
        onFinish(exitCode)
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
