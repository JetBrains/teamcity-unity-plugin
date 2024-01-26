

package jetbrains.buildServer.unity

import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.runner.*
import java.io.File

class BuildCommandExecutionAdapter(private val buildService: CommandLineBuildService) : CommandExecution {

    private val processListeners by lazy { buildService.listeners }

    var result: BuildFinishedStatus? = null
        private set

    override fun processFinished(exitCode: Int) {
        buildService.afterProcessFinished()

        processListeners.forEach {
            it.processFinished(exitCode)
        }

        result = buildService.getRunResult(exitCode)
        if (result == BuildFinishedStatus.FINISHED_SUCCESS) {
            buildService.afterProcessSuccessfullyFinished()
        }
    }

    override fun processStarted(programCommandLine: String, workingDirectory: File) {
        processListeners.forEach {
            it.processStarted(programCommandLine, workingDirectory)
        }
    }

    override fun onStandardOutput(text: String) {
        processListeners.forEach {
            it.onStandardOutput(text)
        }
    }

    override fun onErrorOutput(text: String) {
        processListeners.forEach {
            it.onErrorOutput(text)
        }
    }

    override fun interruptRequested(): TerminationAction {
        return buildService.interrupt()
    }

    override fun makeProgramCommandLine(): ProgramCommandLine {
        return buildService.makeProgramCommandLine()
    }

    override fun isCommandLineLoggingEnabled() = buildService.isCommandLineLoggingEnabled

    override fun beforeProcessStarted() {
        buildService.beforeProcessStarted()
    }
}