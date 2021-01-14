/*
 * Copyright 2000-2021 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.unity

import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.runner.*
import java.io.File

class CommandExecutionAdapter(private val buildService: CommandLineBuildService) : CommandExecution {

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