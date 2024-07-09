

package jetbrains.buildServer.unity.util

import com.intellij.execution.configurations.GeneralCommandLine
import jetbrains.buildServer.ExecResult
import jetbrains.buildServer.SimpleCommandLineProcessRunner

interface CommandLineRunner {
    fun run(command: GeneralCommandLine): ExecResult
}

class SimpleCommandLineRunner : CommandLineRunner {
    override fun run(command: GeneralCommandLine): ExecResult {
        return SimpleCommandLineProcessRunner.runCommand(command, byteArrayOf())
    }
}
