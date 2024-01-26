

package jetbrains.buildServer.unity.detectors

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.ExtensionHolder
import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildAgent
import jetbrains.buildServer.agent.BuildAgentConfiguration
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.agent.ToolProvider
import jetbrains.buildServer.agent.ToolProvidersRegistry
import jetbrains.buildServer.agent.config.AgentParametersSupplier
import jetbrains.buildServer.unity.DetectionMode
import jetbrains.buildServer.unity.UnityConstants
import jetbrains.buildServer.unity.UnityConstants.RUNNER_DISPLAY_NAME
import jetbrains.buildServer.unity.UnityConstants.RUNNER_TYPE
import jetbrains.buildServer.unity.UnityConstants.UNITY_CONFIG_NAME
import jetbrains.buildServer.unity.UnityEnvironment
import jetbrains.buildServer.unity.ProjectAssociatedUnityVersionIdentifier
import jetbrains.buildServer.unity.UnityConstants.PARAM_DETECTION_MODE
import jetbrains.buildServer.unity.UnityProjectFilesAccessor
import jetbrains.buildServer.unity.UnityVersion
import jetbrains.buildServer.unity.UnityVersion.Companion.parseVersion
import jetbrains.buildServer.unity.util.unityRootParam
import jetbrains.buildServer.unity.util.unityVersionParam
import jetbrains.buildServer.util.EventDispatcher
import java.io.File
import java.io.InputStream

/**
 * Determines tool location.
 */
class UnityToolProvider(
    private val agentConfiguration: BuildAgentConfiguration,
    private val projectAssociatedUnityVersionIdentifier: ProjectAssociatedUnityVersionIdentifier,
    unityDetectorFactory: UnityDetectorFactory,
    toolsRegistry: ToolProvidersRegistry,
    extensionHolder: ExtensionHolder,
    events: EventDispatcher<AgentLifeCycleListener>
) : AgentLifeCycleAdapter(), AgentParametersSupplier, ToolProvider {

    private val unityDetector = unityDetectorFactory.unityDetector()
    private val unityVersions = mutableMapOf<UnityVersion, String>()

    init {
        toolsRegistry.registerToolProvider(this)
        events.addListener(this)
        extensionHolder.registerExtension(AgentParametersSupplier::class.java, this::class.java.simpleName, this)
    }

    override fun agentStarted(agent: BuildAgent) {
        // If unityVersions is empty, it may mean the agent was initialized from a state cache
        if (unityVersions.isNotEmpty()) {
            return
        }

        val unityToolsParameters = agentConfiguration.configurationParameters
            .filter { entry -> entry.key.startsWith(UNITY_CONFIG_NAME) }
            .map { entry ->
                val version = entry.key.substring(UNITY_CONFIG_NAME.length)
                parseVersion(version) to entry.value
            }.toMap()

        unityVersions.putAll(unityToolsParameters)
    }

    override fun getParameters(): MutableMap<String, String> {
        LOG.info("Locating $RUNNER_DISPLAY_NAME tools")

        val detectedUnityVersions = unityDetector.findInstallations().map { versionPair ->
            LOG.info("Found Unity ${versionPair.first} at ${versionPair.second.absolutePath}")
            versionPair.first to versionPair.second.absolutePath
        }.toMap()

        unityVersions.putAll(detectedUnityVersions)

        return unityVersions.map {
            getParameterName(it.key) to it.value
        }.toMap().toMutableMap()
    }

    private fun getParameterName(version: UnityVersion) = "$UNITY_CONFIG_NAME$version"

    override fun supports(toolName: String): Boolean {
        return RUNNER_TYPE.equals(toolName, true)
    }

    override fun getPath(toolName: String): String {
        return getUnity(toolName).unityPath
    }

    override fun getPath(
        toolName: String,
        ignored: AgentRunningBuild,
        runner: BuildRunnerContext
    ): String {
        return getUnity(toolName, runner).unityPath
    }

    fun getUnity(toolName: String, runnerContext: BuildRunnerContext? = null): UnityEnvironment {
        if (!supports(toolName)) {
            throw ToolCannotBeFoundException("Unsupported tool $toolName")
        }

        // We're unable to detect the environment without the runner context, fallback to the latest available version
        if (runnerContext == null) {
            return unityVersions.getLatestEnvironment()
        }

        val detectionMode = runnerContext.runnerParameters[PARAM_DETECTION_MODE]?.let {
            DetectionMode.tryParse(it)
        } ?: if (runnerContext.unityRootParam().isNullOrEmpty()) DetectionMode.Auto else DetectionMode.Manual

        val environment =  when (detectionMode) {
            DetectionMode.Auto -> {
                discoverUnityByVersion(runnerContext)
            }
            DetectionMode.Manual -> {
                discoverUnityByPath(runnerContext)
            }
        }

        LOG.info("Unity version '${environment.unityVersion}' located at '${environment.unityPath}' was chosen for the build")

        return environment
    }

    private fun discoverUnityByVersion(runnerContext: BuildRunnerContext): UnityEnvironment {
        val unityVersion = runnerContext.unityVersionParam()

        if (unityVersion == null) {
            LOG.info("Unity version has not been explicitly specified. Will try to implicitly select the proper one")
            return implicitlySelectProperUnity(runnerContext)
        }

        unityVersions[unityVersion]?.let { path ->
            return createEnvironment(path, unityVersion)
        }

        val upperVersion = getUpperVersion(unityVersion)
        LOG.info("Specified Unity '$unityVersion' version was not found. Will try to find the latest version up to '$upperVersion'")
        unityVersions.entries
            .filter {
                it.key >= unityVersion && it.key < upperVersion
            }
            .maxByOrNull { it.key }
            ?.let { (version, path) ->
                return createEnvironment(path, version)
            }

        throw ToolCannotBeFoundException(
            """
            Unable to locate tool $RUNNER_TYPE $unityVersion in system. 
            Please make sure to specify UNITY_PATH environment variable
            """.trimIndent()
        )
    }

    private fun implicitlySelectProperUnity(runnerContext: BuildRunnerContext): UnityEnvironment {
        tryToFindAssociatedUnityVersion(runnerContext)?.let { version ->
            unityVersions[version]?.let { path ->
                return createEnvironment(path, version)
            }

            LOG.info("Agent has no Unity '$version' version installed")
        }

        LOG.info("Will take the latest available Unity on the agent for the build")
        return unityVersions.getLatestEnvironment()
    }

    private fun discoverUnityByPath(runnerContext: BuildRunnerContext): UnityEnvironment {
        val rootPath = runnerContext.unityRootParam()

        if (rootPath.isNullOrEmpty()) { throw ToolCannotBeFoundException(
            "Unable to locate tool $RUNNER_TYPE in system. Manual detection mode has been chosen, but no path has been specified"
        )}

        val version =
            unityDetector.getVersionFromInstall(File(rootPath)) ?: throw ToolCannotBeFoundException(
                "Unable to locate tool $RUNNER_TYPE in system. Please make sure correct Unity binary tool is installed"
            )

        return createEnvironment(rootPath, version)
    }

    private fun tryToFindAssociatedUnityVersion(runnerContext: BuildRunnerContext): UnityVersion? {
        val projectPath = runnerContext.runnerParameters[UnityConstants.PARAM_PROJECT_PATH].let {
            val relativeProjectPath = if (!it.isNullOrBlank()) {
                it.trim()
            } else {
                ""
            }

            File(runnerContext.workingDirectory.absolutePath, relativeProjectPath)
        }

        return projectAssociatedUnityVersionIdentifier.identify(
            object : UnityProjectFilesAccessor {
                private var current = projectPath

                override fun directory(name: String): UnityProjectFilesAccessor? {
                    current = current.listFiles()?.firstOrNull { it.isDirectory && it.name == name } ?: return null
                    return this
                }

                override fun file(name: String): InputStream? {
                    val file = current.listFiles()?.firstOrNull { it.isFile && it.name == name } ?: return null
                    return file.inputStream()
                }
            }
        )
    }

    private fun Map<UnityVersion, String>.getLatestEnvironment(): UnityEnvironment {
        return entries
            .maxByOrNull { it.key }
            ?.let { (version, path) -> createEnvironment(path, version) }
            ?: throw ToolCannotBeFoundException("No Unity was found")
    }

    private fun createEnvironment(path: String, version: UnityVersion): UnityEnvironment =
        UnityEnvironment(unityDetector.getEditorPath(File(path)).absolutePath, version)

    private fun getUpperVersion(version: UnityVersion): UnityVersion = when (version.minor) {
        null -> version.nextMajor()
        else -> version.nextMinor()
    }

    companion object {
        private val LOG = Logger.getInstance(UnityToolProvider::class.java.name)
    }
}