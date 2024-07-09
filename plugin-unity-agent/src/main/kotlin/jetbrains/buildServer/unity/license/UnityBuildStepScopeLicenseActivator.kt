package jetbrains.buildServer.unity.license

import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.unity.UnityEnvironment
import jetbrains.buildServer.unity.UnityLicenseScope
import jetbrains.buildServer.unity.UnityLicenseTypeParameter.PERSONAL
import jetbrains.buildServer.unity.UnityLicenseTypeParameter.PROFESSIONAL
import jetbrains.buildServer.unity.license.commands.ActivatePersonalLicenseCommand
import jetbrains.buildServer.unity.license.commands.ActivateProLicenseCommand
import jetbrains.buildServer.unity.license.commands.ReturnProLicenseCommand
import jetbrains.buildServer.unity.util.unityLicenseScopeParam
import jetbrains.buildServer.unity.util.unityLicenseTypeParam

class UnityBuildStepScopeLicenseActivator(
    private val activatePersonalCommand: ActivatePersonalLicenseCommand,
    private val activateProCommand: ActivateProLicenseCommand,
    private val returnProCommand: ReturnProLicenseCommand,
) {

    private fun BuildRunnerContext.isLicenseActivationPerBuildStepEnabled(): Boolean {
        if (this.unityLicenseTypeParam() == null) {
            return false
        }

        val licenseScope = this.build.unityLicenseScopeParam()
        return licenseScope == null || licenseScope == UnityLicenseScope.BUILD_STEP
    }

    fun activateLicense(
        unityEnvironment: UnityEnvironment,
        runnerContext: BuildRunnerContext,
    ): Sequence<CommandExecution> = sequence {
        if (!runnerContext.isLicenseActivationPerBuildStepEnabled()) {
            return@sequence
        }

        when (runnerContext.unityLicenseTypeParam()) {
            PROFESSIONAL -> yield(activateProCommand.withUnityEnvironment(unityEnvironment))
            PERSONAL -> yield(activatePersonalCommand.withUnityEnvironment(unityEnvironment))
            else -> {}
        }
    }

    fun returnLicense(
        unityEnvironment: UnityEnvironment,
        runnerContext: BuildRunnerContext,
    ): Sequence<CommandExecution> = sequence {
        if (!runnerContext.isLicenseActivationPerBuildStepEnabled()) {
            return@sequence
        }
        when (runnerContext.unityLicenseTypeParam()) {
            PROFESSIONAL -> yield(returnProCommand.withUnityEnvironment(unityEnvironment))
            else -> {}
        }
    }
}
