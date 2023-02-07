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

package jetbrains.buildServer.unity

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.Logger
import com.vdurmont.semver4j.Semver
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.impl.AgentEventDispatcher
import jetbrains.buildServer.messages.DefaultMessagesInfo
import jetbrains.buildServer.unity.util.CommandLineRunner
import java.io.File

class UnityLicenseManager(
    private val unityToolProvider: UnityToolProvider,
    private val commandLineRunner: CommandLineRunner,
    eventDispatcher: AgentEventDispatcher
) : AgentLifeCycleAdapter() {

    private var unityEditorPath: String? = null

    init {
        eventDispatcher.addListener(this)
    }

    override fun buildStarted(build: AgentRunningBuild) {
        unityEditorPath = null

        val feature = build.getBuildFeaturesOfType(UnityConstants.BUILD_FEATURE_TYPE).firstOrNull() ?: return
        if (!build.buildRunners.any(::isUnityRunner)) return

        val parameters = feature.parameters
        parameters[UnityConstants.PARAM_ACTIVATE_LICENSE]?.let activate@{ activateLicense ->
            if (!activateLicense.toBoolean()) {
                return@activate
            }

            // Activate Unity license
            val unityVersion = parameters[UnityConstants.PARAM_UNITY_VERSION]?.let {
                Semver(it, Semver.SemverType.LOOSE)
            }
            try {
                unityEditorPath = unityToolProvider.getUnityPath(UnityConstants.RUNNER_TYPE, unityVersion)
            } catch (e: Exception) {
                build.buildLogger.warning("Failed to find Unity version ${unityVersion ?: ""}")
                return
            }

            val arguments = listOf("-quit", "-batchmode", "-nographics",
                *feature.produceArgsForEditor(sequenceOf(
                    Pair("-serial", UnityConstants.PARAM_SERIAL_NUMBER),
                    Pair("-username", UnityConstants.PARAM_USERNAME),
                    Pair("-password", UnityConstants.PARAM_PASSWORD)
                ))
            )

            executeCommandLine("Activate Unity license", arguments, build)
        }
    }

    override fun buildFinished(build: AgentRunningBuild, buildStatus: BuildFinishedStatus) {
        if (unityEditorPath.isNullOrEmpty()) {
            return
        }

        val feature = build.getBuildFeaturesOfType(UnityConstants.BUILD_FEATURE_TYPE).firstOrNull() ?: return
        val arguments = listOf("-quit", "-batchmode", "-nographics", "-returnlicense",
            *feature.produceArgsForEditor(sequenceOf(
                Pair("-username", UnityConstants.PARAM_USERNAME),
                Pair("-password", UnityConstants.PARAM_PASSWORD)
            ))
        )

        executeCommandLine("Return Unity license", arguments, build)
    }

    private fun AgentBuildFeature.produceArgsForEditor(args: Sequence<Pair<String, String>>) = sequence {
        args.forEach { (editorArg, featureParameter) ->
            this@produceArgsForEditor.parameters[featureParameter]?.let {
                yield(editorArg)
                yield(it.trim())
            }
        }
    }.toList().toTypedArray()

    private fun executeCommandLine(command: String, arguments: List<String>, build: AgentRunningBuild) {
        val commandLine = GeneralCommandLine()
        commandLine.exePath = unityEditorPath
        commandLine.addParameters(arguments)
        val logFile = generateLogFile(build)
        commandLine.addParameters("-logFile", logFile.absolutePath)

        underLogBlock(command, build.buildLogger, false) {
            build.buildLogger.message("Starting: $commandLine")

            val result = commandLineRunner.run(commandLine)
            if (result.exitCode != 0) {
                build.buildLogger.warning(
                        "Process exited with code ${result.exitCode}. Unity log:\n" + logFile.readText()
                )
            }

            if (LOG.isDebugEnabled) {
                LOG.debug("Unity log:\n${logFile.readText()}")
            }
        }
    }

    private fun isUnityRunner(it: BuildRunnerSettings): Boolean {
        if (!it.isEnabled) return false
        if (it.runType != UnityConstants.RUNNER_TYPE) return false
        return it.runnerParameters["plugin.docker.imageId"].isNullOrEmpty()
    }

    private fun <T> underLogBlock(blockName: String, logger: BuildProgressLogger, isVerbose: Boolean, block: () -> T): T {
        val blockStart = DefaultMessagesInfo.createBlockStart(blockName, "unity")
        logger.logMessage(if (isVerbose) DefaultMessagesInfo.internalize(blockStart) else blockStart)
        try {
            return block()
        } finally {
            val blockEnd = DefaultMessagesInfo.createBlockEnd(blockName, "unity")
            logger.logMessage(if (isVerbose) DefaultMessagesInfo.internalize(blockEnd) else blockEnd)
        }
    }

    private fun generateLogFile(build: AgentRunningBuild): File = File.createTempFile(
        "unityBuildLog-",
        "-${build.buildId}.txt",
        build.agentTempDirectory
    )

    companion object {
        private val LOG = Logger.getInstance(UnityLicenseManager::class.java.name)
    }
}