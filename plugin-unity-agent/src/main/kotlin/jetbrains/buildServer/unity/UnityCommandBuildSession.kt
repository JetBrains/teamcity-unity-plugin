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

import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession
import jetbrains.buildServer.unity.license.UnityLicenseManager
import jetbrains.buildServer.unity.util.FileSystemService

class UnityCommandBuildSession(
    private val runnerContext: BuildRunnerContext,
    private val fileSystemService: FileSystemService,
    private val unityEnvironmentProvider: UnityEnvironmentProvider,
    private val unityLicenseManager: UnityLicenseManager,
) : MultiCommandBuildSession {

    private var commands: Iterator<CommandExecution>? = null
    private var lastBuildCommands = arrayListOf<BuildCommandExecutionAdapter>()

    override fun sessionStarted() {
        commands = commandsSequence().iterator()
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
        return lastBuildCommands.lastOrNull()?.result
    }

    private fun commandsSequence() = sequence {
        detectUnityEnvironment()
        activateLicenceIfNeeded()
        executeBuild()
        returnLicenceIfNeeded()
    }

    private suspend fun SequenceScope<CommandExecution>.detectUnityEnvironment() {
        yieldAll(unityEnvironmentProvider.provide(runnerContext))
    }

    private suspend fun SequenceScope<CommandExecution>.activateLicenceIfNeeded() {
        yieldAll(
            unityLicenseManager.activateLicense(
                unityEnvironmentProvider.unityEnvironment(),
                runnerContext,
            )
        )
    }

    private suspend fun SequenceScope<CommandExecution>.executeBuild() {
        yieldAll(
            UnityRunnerBuildService
                .createAdapters(unityEnvironmentProvider.unityEnvironment(), runnerContext, fileSystemService)
                .map {
                    it.initialize(runnerContext.build, runnerContext)
                    val command = BuildCommandExecutionAdapter(it)
                    lastBuildCommands.add(command)
                    command
                }
        )
    }

    private suspend fun SequenceScope<CommandExecution>.returnLicenceIfNeeded() {
        yieldAll(
            unityLicenseManager.returnLicense(
                unityEnvironmentProvider.unityEnvironment(),
                runnerContext,
            )
        )
    }
}
