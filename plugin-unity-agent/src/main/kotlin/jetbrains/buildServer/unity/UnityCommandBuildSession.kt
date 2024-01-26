

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