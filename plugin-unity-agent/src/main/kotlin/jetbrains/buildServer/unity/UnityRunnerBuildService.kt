/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.unity

import com.intellij.openapi.util.SystemInfo
import jetbrains.buildServer.agent.runner.BuildServiceAdapter
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.util.StringUtil
import org.apache.commons.io.input.Tailer
import org.apache.commons.io.input.TailerListenerAdapter
import java.io.File

/**
 * Unity runner service.
 */
class UnityRunnerBuildService : BuildServiceAdapter() {

    private var unityLogFile: File? = null
    private var unityTestsReportFile: File? = null
    private var unityLogFileTailer: Tailer? = null

    override fun makeProgramCommandLine(): ProgramCommandLine {
        val toolPath = getToolPath(UnityConstants.RUNNER_TYPE)
        val arguments = mutableListOf("-batchmode")

        var workingDirectory = workingDirectory.absolutePath
        runnerParameters[UnityConstants.PARAM_PROJECT_PATH]?.let {
            if (it.isNotEmpty()) {
                val projectFile = File(it.trim())
                workingDirectory = projectFile.absolutePath
            }
        }
        arguments.addAll(listOf("-projectPath", workingDirectory))

        runnerParameters[UnityConstants.PARAM_BUILD_TARGET]?.let {
            if (it.isNotEmpty()) {
                arguments.addAll(listOf("-buildTarget", it.trim()))
            }
        }

        runnerParameters[UnityConstants.PARAM_BUILD_PLAYER]?.let {
            val playerPath = runnerParameters[UnityConstants.PARAM_BUILD_PLAYER_PATH]
            if (it.isNotEmpty() && !playerPath.isNullOrEmpty()) {
                var playerFile = File(playerPath.trim())
                if (!playerFile.isAbsolute) {
                    playerFile = File(workingDirectory, playerPath.trim())
                }
                arguments.addAll(listOf("-" + it.trim(), playerFile.absolutePath))
            }
        }

        runnerParameters[UnityConstants.PARAM_RUN_EDITOR_TESTS]?.let {
            if (it.toBoolean()) {
                arguments.add(ARG_RUN_TESTS)
            }
        }

        runnerParameters[UnityConstants.PARAM_NO_GRAPHICS]?.let {
            if (it.toBoolean()) {
                arguments.add("-nographics")
            }
        }

        runnerParameters[UnityConstants.PARAM_EXECUTE_METHOD]?.let {
            if (it.isNotEmpty()) {
                arguments.addAll(listOf("-executeMethod", it.trim()))
            }
        }

        runnerParameters[UnityConstants.PARAM_ARGUMENTS]?.let {
            if (it.isNotEmpty()) {
                arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(it))
            }
        }

        val logFile = unityLogFile
        if (logFile != null) {
            arguments.addAll(listOf(ARG_LOG_FILE, logFile.absolutePath))
        } else {
            arguments.add(ARG_LOG_FILE)
        }

        // -runEditorTests always executes -quit
        if (!arguments.contains(ARG_RUN_TESTS)) {
            arguments.add("-quit")
        } else {
            val index = arguments.indexOf(ARG_TESTS_FILE)
            unityTestsReportFile = if (index > 0 && index + 1 < arguments.size) {
                val testsResultPath = arguments[index + 1]
                File(testsResultPath)
            } else {
                File.createTempFile(
                        "unityTestResults-",
                        "-${build.buildId}.xml",
                        build.buildTempDirectory
                ).apply {
                    arguments.addAll(listOf(ARG_TESTS_FILE, this.absolutePath))
                }
            }

            runnerParameters[UnityConstants.PARAM_TEST_PLATFORM]?.let {
                if (it.isNotEmpty()) {
                    arguments.addAll(listOf("-testPlatform", it))
                }
            }

            runnerParameters[UnityConstants.PARAM_TEST_CATEGORIES]?.let {
                if (it.isNotEmpty()) {
                    val categories = StringUtil.split(it).joinToString(",")
                    arguments.addAll(listOf("-editorTestsCategories", categories))
                }
            }

            runnerParameters[UnityConstants.PARAM_TEST_NAMES]?.let {
                if (it.isNotEmpty()) {
                    val names = StringUtil.split(it).joinToString(",")
                    arguments.addAll(listOf("-editorTestsFilter", names))
                }
            }

            // Append build feature parameters
            build.getBuildFeaturesOfType(UnityConstants.BUILD_FEATURE_TYPE).firstOrNull()?.let { feature ->
                feature.parameters[UnityConstants.PARAM_CACHE_SERVER]?.let {
                    if (it.isNotEmpty()) {
                        arguments.addAll(listOf("-CacheServerIPAddress", it.trim()))
                    }
                }
            }
        }

        return createProgramCommandline(toolPath, arguments)
    }

    override fun isCommandLineLoggingEnabled() = true

    override fun beforeProcessStarted() {
        // On Windows unity could not write log into stdout
        // so we need to read a log file contents
        if (SystemInfo.isWindows) {
            unityLogFile = File.createTempFile(
                    "unityBuildLog-",
                    "-${build.buildId}.txt",
                    build.buildTempDirectory
            )

            unityLogFileTailer = Tailer.create(unityLogFile, object : TailerListenerAdapter() {
                override fun handle(line: String) {
                    logger.message(line)
                }

                override fun fileRotated() {
                    unityLogFileTailer?.stop()
                }
            }, DEFAULT_DELAY_MILLIS, false)
        }
    }

    override fun afterProcessFinished() {
        unityLogFileTailer?.apply {
            // Wait while Tailer will complete read
            Thread.sleep(DEFAULT_DELAY_MILLIS)
            stop()
        }
        unityTestsReportFile?.let {
            logger.message("##teamcity[importData type='nunit' path='${it.absolutePath}']")
        }
    }

    companion object {
        private const val DEFAULT_DELAY_MILLIS = 500L
        private const val ARG_LOG_FILE = "-logFile"
        private const val ARG_RUN_TESTS = "-runEditorTests"
        private const val ARG_TESTS_FILE = "-editorTestsResultFile"
    }
}
