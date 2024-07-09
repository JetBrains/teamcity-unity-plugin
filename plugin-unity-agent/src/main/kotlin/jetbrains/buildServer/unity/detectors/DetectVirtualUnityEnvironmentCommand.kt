package jetbrains.buildServer.unity.detectors

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine
import jetbrains.buildServer.agent.runner.TerminationAction.KILL_PROCESS_TREE
import jetbrains.buildServer.unity.UnityEnvironment
import jetbrains.buildServer.unity.UnityVersion.Companion.tryParseVersion
import jetbrains.buildServer.unity.util.unityRootParam
import jetbrains.buildServer.unity.util.unityVersionParam
import jetbrains.buildServer.util.OSType.WINDOWS
import org.apache.commons.lang3.StringUtils.substringAfter
import org.apache.commons.lang3.StringUtils.substringBetween
import java.io.File
import java.nio.file.Paths
import java.util.Collections.synchronizedSet

class DetectVirtualUnityEnvironmentCommand(
    private val runnerContext: BuildRunnerContext,
) : CommandExecution {

    val results: MutableSet<UnityEnvironment> = synchronizedSet(LinkedHashSet())
    private val errors: MutableSet<String> = synchronizedSet(LinkedHashSet())

    override fun onStandardOutput(text: String) {
        if (text.isBlank()) {
            return
        }

        text.split(LINE_END).forEach {
            if (it.startsWith("log:")) {
                LOG.debug(it.substringAfter("log:"))
                return@forEach
            }

            val path = substringBetween(it, PATH_START, PATH_END)
            val version = tryParseVersion(substringAfter(it, VERSION_START))
            if (!path.isNullOrBlank() && version != null) {
                val expectedVersion = runnerContext.unityVersionParam()
                if (expectedVersion != null && version != expectedVersion) {
                    LOG.info(
                        "Skipping Unity environment because the Unity version is different, " +
                            "path=$path; version=$version; expected version=$expectedVersion",
                    )
                    return
                }
                val environment = UnityEnvironment(path, version, true)
                if (!results.contains(environment)) {
                    results.add(environment)
                }
            } else {
                LOG.debug("Failed to parse standard output: $text")
            }
        }
    }

    override fun onErrorOutput(text: String) {
        if (text.isBlank()) {
            return
        }

        text.split(LINE_END).forEach {
            val error = substringAfter(it, ERROR_START)
            if (error.isNullOrBlank()) {
                LOG.warn("Failed to parse error output: $text")
            } else if (!errors.contains(error)) {
                errors.add(error)
            }
        }
    }

    override fun makeProgramCommandLine(): ProgramCommandLine {
        val unityRootParam = runnerContext.unityRootParam()
        val environmentVariables =
            if (unityRootParam == null) {
                runnerContext.buildParameters.environmentVariables
            } else {
                runnerContext.buildParameters.environmentVariables + mapOf(UNITY_ROOT_PARAMETER to unityRootParam)
            }

        return SimpleProgramCommandLine(
            environmentVariables,
            resolvePath(runnerContext.workingDirectory.path),
            resolvePath(scriptSourcePath.toFile().canonicalPath.toString()),
            emptyList(),
        )
    }

    override fun beforeProcessStarted() {}
    override fun processStarted(programCommandLine: String, workingDirectory: File) {}
    override fun processFinished(exitCode: Int) {
        logResults(exitCode)
    }

    private fun logResults(exitCode: Int) {
        if (exitCode != 0) {
            LOG.warn("Command execution was finished with $exitCode exit code")
        }
        if (errors.isNotEmpty()) {
            LOG.warn("There were some errors during command execution")
            errors.forEach {
                LOG.warn("Command execution error: $it")
            }
        }
        if (results.isEmpty()) {
            LOG.warn(
                "Virtual Unity environment was not found. Please make sure that Unity executable exists inside specified image",
            )
        }
    }

    override fun interruptRequested() = KILL_PROCESS_TREE
    override fun isCommandLineLoggingEnabled() = true

    private val isWindows get() = runnerContext.virtualContext.targetOSType == WINDOWS

    private val scriptSourcePath
        get() =
            if (isWindows) {
                Paths.get(UNITY_ENVIRONMENT_DETECTOR_BAT_PATH).toAbsolutePath()
            } else {
                Paths.get(UNITY_ENVIRONMENT_DETECTOR_SH_PATH).toAbsolutePath()
            }

    private fun resolvePath(path: String) = runnerContext.virtualContext.resolvePath(path)

    companion object {
        private val LOG = Logger.getInstance(DetectVirtualUnityEnvironmentCommand::class.java.name)

        private const val UNITY_ENVIRONMENT_DETECTOR_SH = "unity-environment-detector.sh"
        private const val UNITY_ENVIRONMENT_DETECTOR_SH_PATH =
            "../plugins/teamcity-unity-agent/tools/$UNITY_ENVIRONMENT_DETECTOR_SH"

        private const val UNITY_ENVIRONMENT_DETECTOR_BAT = "unity-environment-detector.bat"
        private const val UNITY_ENVIRONMENT_DETECTOR_BAT_PATH =
            "../plugins/teamcity-unity-agent/tools/$UNITY_ENVIRONMENT_DETECTOR_BAT"

        private const val UNITY_ROOT_PARAMETER = "UNITY_ROOT_PARAMETER"

        private const val PATH_START = "path="
        private const val PATH_END = ";"
        private const val VERSION_START = "version="
        private const val ERROR_START = "error="
        private val LINE_END = "\r?\n|\r".toRegex()
    }
}
