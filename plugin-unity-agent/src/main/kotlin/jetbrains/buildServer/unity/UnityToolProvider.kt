/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.unity

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfo
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
        else -> null
    }

    init {
        toolsRegistry.registerToolProvider(this)
        events.addListener(this)
    }

    override fun beforeAgentConfigurationLoaded(agent: BuildAgent) {
        LOG.info("Locating ${UnityConstants.RUNNER_DISPLAY_NAME} tools")

        // Report all Unity versions
        val versions = unityDetector?.findInstallations()?.toMap() ?: return
        versions.forEach { (version, path) ->
            LOG.info("Found Unity $version at $path")
            agent.configuration.apply {
                val name = "${UnityConstants.UNITY_CONFIG_NAME}$version${UnityConstants.UNITY_CONFIG_PATH}"
                addConfigurationParameter(name, path.absolutePath)
            }
        }

        // Report maximum Unity version
        versions.entries.maxBy { it.key }?.let { (version, path) ->
            agent.configuration.apply {
                val name = "${UnityConstants.RUNNER_TYPE}${UnityConstants.UNITY_CONFIG_VERSION}"
                addConfigurationParameter(name, version.toString())
            }
            agent.configuration.apply {
                val name = "${UnityConstants.RUNNER_TYPE}${UnityConstants.UNITY_CONFIG_PATH}"
                addConfigurationParameter(name, path.absolutePath)
            }
        }
    }

    override fun supports(toolName: String): Boolean {
        return UnityConstants.RUNNER_TYPE.equals(toolName, true)
    }

    override fun getPath(toolName: String): String {
        if (!supports(toolName)) throw ToolCannotBeFoundException("Unsupported tool $toolName")

        throw ToolCannotBeFoundException("""
                Unable to locate tool $toolName in system. Please make sure to add it in the PATH variable
                """.trimIndent())
    }

    override fun getPath(toolName: String,
                         build: AgentRunningBuild,
                         runner: BuildRunnerContext): String {
        if (unityDetector == null) throw ToolCannotBeFoundException(toolName)

        val unityVersions = getUnityVersions(build)
        val unityVersion = runner.runnerParameters[UnityConstants.PARAM_UNITY_VERSION]

        val unityPath = if (unityVersion.isNullOrEmpty()) {
            unityVersions.entries.lastOrNull()?.value
        } else {
            unityVersions[unityVersion]
        } ?: throw ToolCannotBeFoundException("$toolName, version $unityVersion")

        return unityDetector.getEditorPath(File(unityPath)).absolutePath
    }

    private fun getUnityVersions(build: AgentRunningBuild): Map<String, String> {
        return build.agentConfiguration.configurationParameters.filter { it ->
            it.key.startsWith(UnityConstants.UNITY_CONFIG_NAME) && it.key.endsWith(UnityConstants.UNITY_CONFIG_PATH)
        }.mapKeys { it ->
            it.key.substring(
                    UnityConstants.UNITY_CONFIG_NAME.length,
                    it.key.length - UnityConstants.UNITY_CONFIG_PATH.length
            )
        }
    }

    companion object {
        private val LOG = Logger.getInstance(UnityToolProvider::class.java.name)
    }
}
