

package jetbrains.buildServer.unity

import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession
import jetbrains.buildServer.unity.license.UnityBuildStepScopeLicenseActivator
import jetbrains.buildServer.unity.util.FileSystemService

class UnityCommandBuildSession(
    private val unityBuildRunnerContext: UnityBuildRunnerContext,
    private val fileSystemService: FileSystemService,
    private val unityEnvironmentProvider: UnityEnvironmentProvider,
    private val unityLicenseActivator: UnityBuildStepScopeLicenseActivator,
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
        withLicense {
            executeBuild()
        }
    }

    private suspend fun SequenceScope<CommandExecution>.detectUnityEnvironment() =
        yieldAll(unityEnvironmentProvider.provide(unityBuildRunnerContext))

    private suspend fun SequenceScope<CommandExecution>.withLicense(commands: suspend SequenceScope<CommandExecution>.() -> Unit) =
        yieldAll(
            unityLicenseActivator.withLicense(unityEnvironmentProvider.unityEnvironment()) {
                sequence { commands() }
            },
        )

    private suspend fun SequenceScope<CommandExecution>.executeBuild() {
        yieldAll(
            UnityRunnerBuildService
                .createAdapters(unityEnvironmentProvider.unityEnvironment(), unityBuildRunnerContext, fileSystemService)
                .map {
                    it.initialize(unityBuildRunnerContext.build, unityBuildRunnerContext)
                    val command = BuildCommandExecutionAdapter(it)
                    lastBuildCommands.add(command)
                    command
                },
        )
    }
}
