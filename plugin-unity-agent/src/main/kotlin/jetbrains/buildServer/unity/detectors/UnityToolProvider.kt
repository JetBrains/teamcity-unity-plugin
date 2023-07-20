/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.unity.detectors

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfo
import jetbrains.buildServer.ExtensionHolder
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.config.AgentParametersSupplier
import jetbrains.buildServer.unity.UnityConstants
import jetbrains.buildServer.unity.UnityEnvironment
import jetbrains.buildServer.unity.UnityVersion
import jetbrains.buildServer.unity.UnityVersion.Companion.parseVersion
import jetbrains.buildServer.unity.UnityVersion.Companion.tryParseVersion
import jetbrains.buildServer.util.EventDispatcher
import java.io.File

/**
 * Determines tool location.
 */
class UnityToolProvider(
    private val agentConfiguration: BuildAgentConfiguration,
    toolsRegistry: ToolProvidersRegistry,
    extensionHolder: ExtensionHolder,
    events: EventDispatcher<AgentLifeCycleListener>
) : AgentLifeCycleAdapter(), AgentParametersSupplier, ToolProvider {

    private val unityDetector = when {
        SystemInfo.isWindows -> WindowsUnityDetector(PEProductVersionDetector())
        SystemInfo.isMac -> MacOsUnityDetector()
        SystemInfo.isLinux -> LinuxUnityDetector()
        else -> null
    }

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
            .filter { entry -> entry.key.startsWith(UnityConstants.UNITY_CONFIG_NAME) }
            .map { entry ->
                val version = entry.key.substring(UnityConstants.UNITY_CONFIG_NAME.length)
                parseVersion(version) to entry.value
            }.toMap()

        unityVersions.putAll(unityToolsParameters)
    }

    override fun getParameters(): MutableMap<String, String> {
        LOG.info("Locating ${UnityConstants.RUNNER_DISPLAY_NAME} tools")

        val detectedUnityVersions = unityDetector?.findInstallations()?.map { versionPair ->
            LOG.info("Found Unity ${versionPair.first} at ${versionPair.second.absolutePath}")
            versionPair.first to versionPair.second.absolutePath
        }?.toMap() ?: emptyMap()

        unityVersions.putAll(detectedUnityVersions)

        return unityVersions.map {
            getParameterName(it.key) to it.value
        }.toMap().toMutableMap()
    }

    private fun getParameterName(version: UnityVersion) = "${UnityConstants.UNITY_CONFIG_NAME}$version"

    override fun supports(toolName: String): Boolean {
        return UnityConstants.RUNNER_TYPE.equals(toolName, true)
    }

    override fun getPath(toolName: String): String {
        return getUnity(toolName, mapOf()).unityPath
    }

    override fun getPath(
        toolName: String,
        ignored: AgentRunningBuild,
        runner: BuildRunnerContext
    ): String {
        return getUnity(toolName, runner).unityPath
    }

    fun getUnity(
        toolName: String,
        runner: BuildRunnerContext
    ): UnityEnvironment {
        val parameters: Map<String?, String?> =
            if (!runner.runnerParameters[UnityConstants.PARAM_UNITY_VERSION].isNullOrEmpty() ||
                !getUnityRootParam(runner.runnerParameters).isNullOrEmpty()
            ) {
                runner.runnerParameters
            } else {
                val feature = runner.build.getBuildFeaturesOfType(UnityConstants.BUILD_FEATURE_TYPE).firstOrNull()
                feature?.parameters ?: mapOf()
            }

        return getUnity(toolName, parameters)
    }

    fun getUnity(toolName: String, parameters: Map<String?, String?>): UnityEnvironment {
        if (!supports(toolName)) {
            throw ToolCannotBeFoundException("Unsupported tool $toolName")
        }

        if (unityDetector == null) {
            throw ToolCannotBeFoundException(UnityConstants.RUNNER_TYPE)
        }

        // Path has been specified by Tool dropdown or provided explicitly
        val editorPath = getUnityRootParam(parameters)
        if (!editorPath.isNullOrEmpty()) {
            val unityVersion =
                unityDetector.getVersionFromInstall(File(editorPath)) ?: throw ToolCannotBeFoundException(
                    "Unable to locate tool $toolName in system. Please make sure correct Unity binary tool is installed"
                )
            return UnityEnvironment(unityDetector.getEditorPath(File(editorPath)).absolutePath, unityVersion)
        }

        // Path is to be discovered by UNITY_VERSION
        val unityVersion = getUnityVersionParam(parameters)

        val (version, path) = if (unityVersion == null) {
            unityVersions.entries.lastOrNull()?.toPair()
        } else {
            val unityPath = unityVersions[unityVersion]
            if (unityPath != null) {
                unityVersion to unityPath
            } else {
                val upperVersion = getUpperVersion(unityVersion)
                unityVersions.entries.lastOrNull {
                    it.key >= unityVersion && it.key < upperVersion
                }?.toPair()
            }
        } ?: throw ToolCannotBeFoundException(
            """
                Unable to locate tool $toolName $unityVersion in system. 
                Please make sure to specify UNITY_PATH environment variable
                """.trimIndent()
        )

        return UnityEnvironment(unityDetector.getEditorPath(File(path)).absolutePath, version)
    }

    private fun getUpperVersion(version: UnityVersion): UnityVersion = when (version.minor) {
        null -> version.nextMajor()
        else -> version.nextMinor()
    }

    companion object {
        private val LOG = Logger.getInstance(UnityToolProvider::class.java.name)

        fun getUnityRootParam(parameters: Map<String?, String?>): String? = parameters[UnityConstants.PARAM_UNITY_ROOT]

        fun getUnityVersionParam(parameters: Map<String?, String?>): UnityVersion? {
            val unityVersion = parameters[UnityConstants.PARAM_UNITY_VERSION]?.trim() ?: return null

            return tryParseVersion(unityVersion)
        }
    }
}
