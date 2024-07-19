package jetbrains.buildServer.unity.license

import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.impl.AgentEventDispatcher
import jetbrains.buildServer.unity.UnityLicenseScope
import jetbrains.buildServer.unity.UnityLicenseTypeParameter
import jetbrains.buildServer.unity.detectors.UnityToolProvider
import jetbrains.buildServer.unity.license.commands.ActivateProLicenseCommand
import jetbrains.buildServer.unity.license.commands.ReturnProLicenseCommand
import jetbrains.buildServer.unity.util.CommandLineRunner
import jetbrains.buildServer.unity.util.FileSystemService
import jetbrains.buildServer.unity.util.unityLicenseScopeParam
import jetbrains.buildServer.unity.util.unityLicenseTypeParam

class UnityBuildScopeLicenseActivator(
    private val toolProvider: UnityToolProvider,
    private val fileSystemService: FileSystemService,
    private val commandLineRunner: CommandLineRunner,
    eventDispatcher: AgentEventDispatcher,
) : AgentLifeCycleAdapter() {

    init {
        eventDispatcher.addListener(this)
    }

    private fun AgentRunningBuild.entireBuildLicenseScope(): Boolean {
        if (this.unityLicenseTypeParam() != UnityLicenseTypeParameter.PROFESSIONAL) {
            return false
        }

        return this.unityLicenseScopeParam() == UnityLicenseScope.BUILD_CONFIGURATION
    }

    override fun preparationFinished(build: AgentRunningBuild) {
        if (!build.entireBuildLicenseScope()) {
            return
        }

        val command = ActivateProLicenseCommand(getLicenseCommandContext(build))
            .withUnityEnvironment(toolProvider.getUnity(build))

        commandLineRunner.execute(command, build.buildLogger)
    }

    override fun beforeBuildFinish(build: AgentRunningBuild, buildStatus: BuildFinishedStatus) {
        if (!build.entireBuildLicenseScope()) {
            return
        }

        val command = ReturnProLicenseCommand(getLicenseCommandContext(build))
            .withUnityEnvironment(toolProvider.getUnity(build))

        commandLineRunner.execute(command, build.buildLogger)
    }

    private fun getLicenseCommandContext(build: AgentRunningBuild) =
        object : LicenseCommandContext {
            override val buildLogger = build.buildLogger
            override val build = build
            override val fileSystemService = this@UnityBuildScopeLicenseActivator.fileSystemService
            override val environmentVariables = build.sharedBuildParameters.environmentVariables
            override val workingDirectory = build.checkoutDirectory.path
            override fun resolvePath(path: String) = path
        }
}
