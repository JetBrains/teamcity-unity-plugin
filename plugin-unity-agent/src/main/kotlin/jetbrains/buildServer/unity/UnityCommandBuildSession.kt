/*
 * Copyright 2000-2023 JetBrains s.r.o.
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

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession
import jetbrains.buildServer.unity.UnityConstants.RUNNER_TYPE
import jetbrains.buildServer.unity.detectors.DetectVirtualUnityEnvironmentCommand
import jetbrains.buildServer.unity.detectors.UnityToolProvider

/**
 * Unity runner service.
 */
class UnityCommandBuildSession(
    private val runnerContext: BuildRunnerContext,
    private val unityToolProvider: UnityToolProvider
) : MultiCommandBuildSession {

    private var commands: Iterator<CommandExecution>? = null
    private var lastCommands = arrayListOf<CommandExecutionAdapter>()

    override fun sessionStarted() {
        commands = commands().iterator()
    }

    override fun getNextCommand(): CommandExecution? {
        commands?.let {
            if (it.hasNext()) {
                return it.next()
            }
        }

        return null
    }

    override fun sessionFinished(): BuildFinishedStatus? {
        return lastCommands.lastOrNull()?.result
    }

    private fun commands() = sequence {
        val unityEnvironment = unityEnvironment()

        yieldAll(
            UnityRunnerBuildService
                .createAdapters(unityEnvironment, runnerContext)
                .map {
                    it.initialize(runnerContext.build, runnerContext)
                    val command = CommandExecutionAdapter(it)
                    lastCommands.add(command)
                    command
                }
        )
    }

    private suspend fun SequenceScope<CommandExecution>.unityEnvironment(): UnityEnvironment {
        return if (runnerContext.isVirtualContext) {
            LOG.debug("Detecting Unity virtual environment")
            val detectCommand = DetectVirtualUnityEnvironmentCommand(runnerContext)
            yield(detectCommand)

            if (detectCommand.results.isEmpty())
                throw ToolCannotBeFoundException("Failed to detect Unity virtual environment")

            detectCommand.results.first()
        } else {
            LOG.debug("Detecting Unity environment")
            unityToolProvider.getUnity(RUNNER_TYPE, runnerContext)
        }
    }

    companion object {
        private val LOG = Logger.getInstance(UnityCommandBuildSession::class.java.name)
    }
}
