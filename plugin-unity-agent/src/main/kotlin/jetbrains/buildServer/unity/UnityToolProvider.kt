/*
 * Copyright 2000-2020 JetBrains s.r.o.
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

package jetbrains.buildServer.unity

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfo
import com.vdurmont.semver4j.Semver
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.util.EventDispatcher
import java.io.File

/**
 * Determines tool location.
 */
class UnityToolProvider(toolsRegistry: ToolProvidersRegistry,
                        events: EventDispatcher<AgentLifeCycleListener>)
    : AgentLifeCycleAdapter(), ToolProvider {

    private val unityDetector = when {
        SystemInfo.isWindows -> WindowsUnityDetector()
        SystemInfo.isMac -> MacOsUnityDetector()
        SystemInfo.isLinux -> LinuxUnityDetector()
        else -> null
    }

    private val unityVersions = mutableMapOf<Semver, String>()

    init {
        toolsRegistry.registerToolProvider(this)
        events.addListener(this)
    }

    override fun afterAgentConfigurationLoaded(agent: BuildAgent) {
        LOG.info("Locating ${UnityConstants.RUNNER_DISPLAY_NAME} tools")

        unityDetector?.let { detector ->
            detector.registerAdditionalHintPath(agent.configuration.agentToolsDirectory)

            detector.findInstallations().let { versions ->
                unityVersions.putAll(versions.sortedBy { it.first }.map {
                    it.first to it.second.absolutePath
                }.toMap())
            }
        }

        // Report all Unity versions
        unityVersions.forEach { (version, path) ->
            LOG.info("Found Unity $version at $path")
            agent.configuration.apply {
                val name = "${UnityConstants.UNITY_CONFIG_NAME}$version"
                addConfigurationParameter(name, path)
            }
        }
    }

    override fun supports(toolName: String): Boolean {
        return UnityConstants.RUNNER_TYPE.equals(toolName, true)
    }

    override fun getPath(toolName: String): String {
        return getUnityPath(toolName, null)
    }

    override fun getPath(toolName: String,
                         build: AgentRunningBuild,
                         runner: BuildRunnerContext): String {
        if (runner.isVirtualContext) {
            return UnityConstants.RUNNER_TYPE
        }

        val unityVersion = getUnityVersion(runner, build)
        return getUnityPath(toolName, unityVersion)
    }

    fun getUnityPath(toolName: String, unityVersion: Semver?): String {
        if (!supports(toolName)) {
            throw ToolCannotBeFoundException("Unsupported tool $toolName")
        }

        if (unityDetector == null) {
            throw ToolCannotBeFoundException(UnityConstants.RUNNER_TYPE)
        }

        val unity = getUnity(toolName, unityVersion)
        return unityDetector.getEditorPath(File(unity.second)).absolutePath
    }

    fun getUnity(toolName: String,
                 build: AgentRunningBuild,
                 runner: BuildRunnerContext): Pair<Semver, String> {
        if (runner.isVirtualContext) {
            return Semver("2019.1.0") to UnityConstants.RUNNER_TYPE
        }

        val unityVersion = getUnityVersion(runner, build)
        var explicitExecutable = runner.runnerParameters[UnityConstants.PARAM_UNITY_EXECUTABLE]?.trim()
        if (explicitExecutable.isNullOrEmpty())
            return getUnity(toolName, unityVersion)

        if (unityVersion == null) {
            return Semver("2019.1.0") to explicitExecutable
        }
        return unityVersion to explicitExecutable
    }

    private fun getUnity(toolName: String, unityVersion: Semver?): Pair<Semver, String> {
        if (!supports(toolName)) {
            throw ToolCannotBeFoundException("Unsupported tool $toolName")
        }

        if (unityDetector == null) {
            throw ToolCannotBeFoundException(UnityConstants.RUNNER_TYPE)
        }

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
        } ?: throw ToolCannotBeFoundException("""
                Unable to locate tool $toolName $unityVersion in system. Please make sure to specify UNITY_PATH environment variable
                """.trimIndent())

        return version to unityDetector.getEditorPath(File(path)).absolutePath
    }

    private fun getUpperVersion(version: Semver): Semver = when {
        version.minor == null -> version.toStrict().nextMajor()
        else -> version.toStrict().nextMinor()
    }

    private fun getUnityVersion(runner: BuildRunnerContext, build: AgentRunningBuild): Semver? {
        var unityVersion = runner.runnerParameters[UnityConstants.PARAM_UNITY_VERSION]?.trim()
        if (unityVersion.isNullOrEmpty()) {
            build.getBuildFeaturesOfType(UnityConstants.BUILD_FEATURE_TYPE).firstOrNull()?.let { feature ->
                unityVersion = feature.parameters[UnityConstants.PARAM_UNITY_VERSION]?.trim()
            }
        }

        if (unityVersion == null) {
            return null
        }

        return Semver(unityVersion, Semver.SemverType.LOOSE)
    }

    companion object {
        private val LOG = Logger.getInstance(UnityToolProvider::class.java.name)
    }
}
