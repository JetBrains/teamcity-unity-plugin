

package jetbrains.buildServer.unity

import jetbrains.buildServer.agent.AgentBuildRunnerInfo
import jetbrains.buildServer.agent.BuildAgentConfiguration
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession
import jetbrains.buildServer.agent.runner.MultiCommandBuildSessionFactory
import jetbrains.buildServer.unity.UnityConstants.RUNNER_TYPE
import jetbrains.buildServer.unity.detectors.DetectVirtualUnityEnvironmentCommand
import jetbrains.buildServer.unity.detectors.UnityToolProvider
import jetbrains.buildServer.unity.license.LicenseCommandContext
import jetbrains.buildServer.unity.license.commands.ActivatePersonalLicenseCommand
import jetbrains.buildServer.unity.license.commands.ActivateProLicenseCommand
import jetbrains.buildServer.unity.license.commands.ReturnProLicenseCommand
import jetbrains.buildServer.unity.license.UnityBuildStepScopeLicenseActivator
import jetbrains.buildServer.unity.util.FileSystemService

class UnityBuildSessionFactory(
    private val unityToolProvider: UnityToolProvider,
    private val fileSystemService: FileSystemService,
) : MultiCommandBuildSessionFactory {

    override fun createSession(runnerContext: BuildRunnerContext): MultiCommandBuildSession =
        UnityCommandBuildSession(
            UnityBuildRunnerContext(runnerContext),
            fileSystemService,
            unityEnvironmentProvider(runnerContext),
            unityLicenseManager(runnerContext),
        )

    private fun unityEnvironmentProvider(runnerContext: BuildRunnerContext) =
        UnityEnvironmentProvider(
            unityToolProvider,
            DetectVirtualUnityEnvironmentCommand(
                runnerContext,
            ),
        )

    private fun unityLicenseManager(runnerContext: BuildRunnerContext): UnityBuildStepScopeLicenseActivator {
        val commandContext = object : LicenseCommandContext {
            override val build = runnerContext.build
            override val fileSystemService = this@UnityBuildSessionFactory.fileSystemService
            override val environmentVariables = runnerContext.buildParameters.environmentVariables
            override val workingDirectory = runnerContext.workingDirectory.path
            override fun resolvePath(path: String) = runnerContext.virtualContext.resolvePath(path)
        }

        return UnityBuildStepScopeLicenseActivator(
            ActivatePersonalLicenseCommand(commandContext),
            ActivateProLicenseCommand(commandContext),
            ReturnProLicenseCommand(commandContext),
        )
    }

    override fun getBuildRunnerInfo(): AgentBuildRunnerInfo {
        return object : AgentBuildRunnerInfo {
            override fun getType(): String {
                return RUNNER_TYPE
            }

            override fun canRun(config: BuildAgentConfiguration): Boolean {
                return true
            }
        }
    }
}