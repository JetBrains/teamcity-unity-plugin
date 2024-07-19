package jetbrains.buildServer.unity.license

import com.intellij.execution.configurations.GeneralCommandLine
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.unity.util.CommandLineRunner

fun CommandLineRunner.execute(command: CommandExecution, buildLogger: BuildProgressLogger) = with(command) {
    beforeProcessStarted()
    val commandLine = makeProgramCommandLine().toGeneralCommandLine()
    buildLogger.message("Starting: $commandLine")
    val result = run(commandLine)
    processFinished(result.exitCode)
}

private fun ProgramCommandLine.toGeneralCommandLine(): GeneralCommandLine {
    val commandLine = GeneralCommandLine()
    commandLine.exePath = this.executablePath
    commandLine.addParameters(this.arguments)
    commandLine.setWorkDirectory(this.workingDirectory)
    commandLine.envParams = this.environment
    return commandLine
}
