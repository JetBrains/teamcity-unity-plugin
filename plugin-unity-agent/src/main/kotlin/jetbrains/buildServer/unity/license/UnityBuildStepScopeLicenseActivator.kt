package jetbrains.buildServer.unity.license

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.FlowGenerator
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.agent.runner.TerminationAction
import jetbrains.buildServer.unity.UnityEnvironment
import jetbrains.buildServer.unity.UnityLicenseScope
import jetbrains.buildServer.unity.UnityLicenseTypeParameter.PERSONAL
import jetbrains.buildServer.unity.UnityLicenseTypeParameter.PROFESSIONAL
import jetbrains.buildServer.unity.license.commands.ActivatePersonalLicenseCommand
import jetbrains.buildServer.unity.license.commands.ActivateProLicenseCommand
import jetbrains.buildServer.unity.license.commands.ReturnProLicenseCommand
import jetbrains.buildServer.unity.util.CommandLineRunner
import jetbrains.buildServer.unity.util.FileSystemService
import jetbrains.buildServer.unity.util.unityLicenseScopeParam
import jetbrains.buildServer.unity.util.unityLicenseTypeParam
import java.util.concurrent.atomic.AtomicBoolean

class UnityBuildStepScopeLicenseActivator(
    private val fileSystemService: FileSystemService,
    private val runnerContext: BuildRunnerContext,
    private val commandLineRunner: CommandLineRunner,
) {
    private val proLicenseActivated = AtomicBoolean(false)

    private val licenseContext = createLicenseCommandContext()
    private val activatePersonalCommand = ActivatePersonalLicenseCommand(licenseContext)
    private val activateProCommand = ActivateProLicenseCommand(licenseContext) { exitCode ->
        if (exitCode == 0) { proLicenseActivated.set(true) }
    }
    private val returnProCommand = ReturnProLicenseCommand(licenseContext) { exitCode ->
        if (exitCode == 0) { proLicenseActivated.set(false) }
    }

    private fun BuildRunnerContext.buildStepLicenseScope(): Boolean {
        if (this.unityLicenseTypeParam() == null) {
            return false
        }

        val licenseScope = this.build.unityLicenseScopeParam()
        return licenseScope == null || licenseScope == UnityLicenseScope.BUILD_STEP
    }

    private fun activateLicense(unityEnvironment: UnityEnvironment) = sequence {
        when (runnerContext.unityLicenseTypeParam()) {
            PROFESSIONAL -> yield(activateProCommand.withUnityEnvironment(unityEnvironment))
            PERSONAL -> yield(activatePersonalCommand.withUnityEnvironment(unityEnvironment))
            else -> {}
        }
    }

    private fun returnLicense(unityEnvironment: UnityEnvironment) = sequence {
        when (runnerContext.unityLicenseTypeParam()) {
            PROFESSIONAL -> yield(returnProCommand.withUnityEnvironment(unityEnvironment))
            else -> {}
        }
    }

    fun withLicense(
        unityEnvironment: UnityEnvironment,
        commands: () -> Sequence<CommandExecution>,
    ) = sequence {
        if (!runnerContext.buildStepLicenseScope()) {
            yieldAll(commands())
            return@sequence
        }

        yieldAll(activateLicense(unityEnvironment))
        yieldAll(
            commands().map { it.returnLicenseOnInterruption(unityEnvironment) },
        )
        yieldAll(returnLicense(unityEnvironment))
    }

    private fun CommandExecution.returnLicenseOnInterruption(
        unityEnvironment: UnityEnvironment,
    ) = object : CommandExecution by this {
        override fun interruptRequested(): TerminationAction {
            if (proLicenseActivated.get()) {
                returnProLicense(unityEnvironment)
            }

            return this@returnLicenseOnInterruption.interruptRequested()
        }
    }

    private fun returnProLicense(unityEnvironment: UnityEnvironment) {
        val flowLogger = runnerContext.build.buildLogger.getFlowLogger(FlowGenerator.generateNewFlow())
        flowLogger.startFlow()
        try {
            logger.info("Build has been interrupted. The previously activated serial-based license will be returned")
            flowLogger.message("Build has been interrupted. The previously activated serial-based license will be returned")
            val command = ReturnProLicenseCommand(object : LicenseCommandContext by licenseContext {
                override val buildLogger = flowLogger
            }).withUnityEnvironment(unityEnvironment)
            commandLineRunner.execute(command, flowLogger)
        } finally {
            flowLogger.disposeFlow()
        }
    }

    private fun createLicenseCommandContext() = object : LicenseCommandContext {
        override val build = runnerContext.build
        override val buildLogger = runnerContext.build.buildLogger
        override val fileSystemService: FileSystemService = this@UnityBuildStepScopeLicenseActivator.fileSystemService
        override val environmentVariables = runnerContext.buildParameters.environmentVariables
        override val workingDirectory = runnerContext.workingDirectory.path
        override fun resolvePath(path: String) = runnerContext.virtualContext.resolvePath(path)
    }

    companion object {
        private val logger = Logger.getInstance(UnityBuildStepScopeLicenseActivator::class.java.name)
    }
}
