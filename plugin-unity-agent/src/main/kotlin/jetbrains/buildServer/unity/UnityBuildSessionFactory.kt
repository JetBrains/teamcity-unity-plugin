

package jetbrains.buildServer.unity

import jetbrains.buildServer.agent.AgentBuildRunnerInfo
import jetbrains.buildServer.agent.BuildAgentConfiguration
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession
import jetbrains.buildServer.agent.runner.MultiCommandBuildSessionFactory
import jetbrains.buildServer.unity.UnityConstants.RUNNER_TYPE
import jetbrains.buildServer.unity.detectors.DetectVirtualUnityEnvironmentCommand
import jetbrains.buildServer.unity.detectors.UnityToolProvider
import jetbrains.buildServer.unity.license.UnityBuildStepScopeLicenseActivator
import jetbrains.buildServer.unity.util.CommandLineRunner
import jetbrains.buildServer.unity.util.FileSystemService

class UnityBuildSessionFactory(
    private val unityToolProvider: UnityToolProvider,
    private val fileSystemService: FileSystemService,
    private val commandLineRunner: CommandLineRunner,
) : MultiCommandBuildSessionFactory {

    override fun createSession(runnerContext: BuildRunnerContext): MultiCommandBuildSession =
        UnityCommandBuildSession(
            UnityBuildRunnerContext(runnerContext),
            fileSystemService,
            unityEnvironmentProvider(runnerContext),
            UnityBuildStepScopeLicenseActivator(fileSystemService, runnerContext, commandLineRunner),
        )

    private fun unityEnvironmentProvider(runnerContext: BuildRunnerContext) =
        UnityEnvironmentProvider(
            unityToolProvider,
            DetectVirtualUnityEnvironmentCommand(
                runnerContext,
            ),
        )

    override fun getBuildRunnerInfo() = object : AgentBuildRunnerInfo {
        override fun getType() = RUNNER_TYPE
        override fun canRun(config: BuildAgentConfiguration) = true
    }
}
