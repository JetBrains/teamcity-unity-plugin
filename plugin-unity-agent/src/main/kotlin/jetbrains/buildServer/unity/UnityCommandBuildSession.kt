/*
 * Copyright 2000-2020 JetBrains s.r.o.
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
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.agent.runner.CommandLineBuildService
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession

/**
 * Unity runner service.
 */
class UnityCommandBuildSession(private val runnerContext: BuildRunnerContext,
                               private val unityToolProvider: UnityToolProvider) : MultiCommandBuildSession {

    private var buildSteps: Iterator<CommandExecution>? = null
    private var lastCommands = arrayListOf<CommandExecutionAdapter>()

    override fun sessionStarted() {
        buildSteps = getSteps()
    }

    override fun getNextCommand(): CommandExecution? {
        buildSteps?.let {
            if (it.hasNext()) {
                return it.next()
            }
        }

        return null
    }

    override fun sessionFinished(): BuildFinishedStatus? {
        return lastCommands.last().result
    }

    private fun getSteps() = iterator<CommandExecution> {
        yield(addCommand(UnityRunnerBuildService(unityToolProvider)))
    }

    private fun addCommand(buildService: CommandLineBuildService) = CommandExecutionAdapter(buildService.apply {
            this.initialize(runnerContext.build, runnerContext)
        }).apply {
            lastCommands.add(this)
        }
}
