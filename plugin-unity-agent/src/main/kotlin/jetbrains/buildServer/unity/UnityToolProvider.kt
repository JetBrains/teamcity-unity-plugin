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

            detector.findInstallations().let { versions ->
                unityVersions.putAll(versions.sortedBy { it.first }.map {
                    it.first to it.second.absolutePath
                }.map {
                    LOG.info("${it.first} to ${it.second}")
                    it
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
        return getUnity(toolName, mapOf()).second
    }

    override fun getPath(toolName: String,
                         build: AgentRunningBuild,
                         runner: BuildRunnerContext): String {
        return getUnity(toolName, build, runner).second
    }

    fun getUnity(toolName: String,
                 build: AgentRunningBuild,
                 runner: BuildRunnerContext): Pair<Semver, String> {
        if (runner.isVirtualContext) {
            return Semver("2019.1.0") to UnityConstants.RUNNER_TYPE
        }

        val parameters:Map<String?, String?> =
                if (!runner.runnerParameters[UnityConstants.PARAM_UNITY_VERSION].isNullOrEmpty() ||
                    !runner.runnerParameters[UnityConstants.PARAM_UNITY_ROOT].isNullOrEmpty()) {
                    runner.runnerParameters
                } else {
                    val feature = build.getBuildFeaturesOfType(UnityConstants.BUILD_FEATURE_TYPE).firstOrNull()
                    feature?.parameters ?: mapOf()
                }
        return getUnity(toolName, parameters)
    }

    fun getUnity(toolName: String, parameters: Map<String?, String?>): Pair<Semver, String> {
        if (!supports(toolName)) {
            throw ToolCannotBeFoundException("Unsupported tool $toolName")
        }

        if (unityDetector == null) {
            throw ToolCannotBeFoundException(UnityConstants.RUNNER_TYPE)
        }

        // Path has been specified by Tool dropdown or provided explicitly
        val editorPath = parameters[UnityConstants.PARAM_UNITY_ROOT]
        if(!editorPath.isNullOrEmpty()) {
            val unityVersion = unityDetector.getVersionFromInstall(File(editorPath))
                    ?: throw ToolCannotBeFoundException("""
                        Unable to locate tool $toolName in system. Please make sure correct Unity binary tool is installed
                        """.trimIndent())
            return unityVersion to unityDetector.getEditorPath(File(editorPath)).absolutePath
        }

        // Path is to be discovered by UNITY_VERSION
        val unityVersion = getUnityVersion(parameters)

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


    private fun getUnityVersion(parameters: Map<String?, String?>): Semver? {
        val unityVersion = parameters[UnityConstants.PARAM_UNITY_VERSION]?.trim() ?: return null

        return Semver(unityVersion, Semver.SemverType.LOOSE)
    }

    companion object {
        private val LOG = Logger.getInstance(UnityToolProvider::class.java.name)
    }
}
