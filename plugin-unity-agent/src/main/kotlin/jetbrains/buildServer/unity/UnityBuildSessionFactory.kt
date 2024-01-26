

package jetbrains.buildServer.unity

import jetbrains.buildServer.agent.AgentBuildRunnerInfo
import jetbrains.buildServer.agent.BuildAgentConfiguration
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession
import jetbrains.buildServer.agent.runner.MultiCommandBuildSessionFactory
import jetbrains.buildServer.unity.UnityConstants.RUNNER_TYPE
import jetbrains.buildServer.unity.detectors.DetectVirtualUnityEnvironmentCommand
import jetbrains.buildServer.unity.detectors.UnityToolProvider
import jetbrains.buildServer.unity.license.ActivatePersonalLicenseCommand
import jetbrains.buildServer.unity.license.ActivateProLicenseCommand
import jetbrains.buildServer.unity.license.ReturnProLicenseCommand
import jetbrains.buildServer.unity.license.UnityLicenseManager
import jetbrains.buildServer.unity.util.FileSystemService

class UnityBuildSessionFactory(
    private val unityToolProvider: UnityToolProvider,
    private val fileSystemService: FileSystemService,
) : MultiCommandBuildSessionFactory {

    override fun createSession(runnerContext: BuildRunnerContext): MultiCommandBuildSession =
        UnityCommandBuildSession(
            runnerContext,
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

    private fun unityLicenseManager(runnerContext: BuildRunnerContext) = UnityLicenseManager(
        ActivatePersonalLicenseCommand(
            runnerContext,
            fileSystemService,
        ),
        ActivateProLicenseCommand(
            runnerContext,
            fileSystemService,
        ),
        ReturnProLicenseCommand(
            runnerContext,
            fileSystemService,
        ),
    )

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