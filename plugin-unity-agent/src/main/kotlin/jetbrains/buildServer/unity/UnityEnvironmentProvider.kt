package jetbrains.buildServer.unity

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.unity.detectors.DetectVirtualUnityEnvironmentCommand
import jetbrains.buildServer.unity.detectors.UnityToolProvider

class UnityEnvironmentProvider(
    private val unityToolProvider: UnityToolProvider,
    private val detectVirtualEnvCommand: DetectVirtualUnityEnvironmentCommand,
) {

    private var unityEnvironment: UnityEnvironment? = null

    fun unityEnvironment() =
        unityEnvironment ?: throw ToolCannotBeFoundException("Unity environment is not initialized yet")

    fun provide(runnerContext: UnityBuildRunnerContext): Sequence<CommandExecution> = sequence {
        unityEnvironment = if (runnerContext.isVirtualContext) {
            LOG.debug("Detecting Unity virtual environment")
            yield(detectVirtualEnvCommand)

            val results = detectVirtualEnvCommand.results
            if (results.isEmpty()) {
                throw ToolCannotBeFoundException("Failed to detect Unity virtual environment")
            }
            results.first()
        } else {
            LOG.debug("Detecting Unity environment")
            unityToolProvider.getUnity(runnerContext)
        }
    }

    companion object {
        private val LOG = Logger.getInstance(UnityEnvironmentProvider::class.java.name)
    }
}
