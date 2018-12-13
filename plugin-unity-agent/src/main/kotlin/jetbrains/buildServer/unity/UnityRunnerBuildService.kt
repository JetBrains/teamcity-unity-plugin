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
import jetbrains.buildServer.util.FileUtil
import jetbrains.buildServer.util.StringUtil
import org.apache.commons.io.input.Tailer
import org.apache.commons.io.input.TailerListenerAdapter
import java.io.File

/**
 * Unity runner service.
 */
class UnityRunnerBuildService : BuildServiceAdapter() {

    private var unityLogFile: File? = null
    private var unityLogFileTailer: Tailer? = null

    override fun makeProgramCommandLine(): ProgramCommandLine {
        val toolPath = getToolPath(UnityConstants.RUNNER_TYPE)
        val arguments = mutableListOf("-batchmode", "-quit")

        runnerParameters[UnityConstants.PARAM_PROJECT_PATH]?.let {
            if (it.isNotEmpty()) {
                val projectDir = File(workingDirectory, it.trim())
                arguments.addAll(listOf("-projectPath", projectDir.absolutePath))
            }
        }

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
            arguments.addAll(listOf("-logFile", logFile.absolutePath))
        } else {
           arguments.add("-logFile")
        }

        return createProgramCommandline(toolPath, arguments)
    }

    override fun isCommandLineLoggingEnabled() = true

    override fun beforeProcessStarted() {
        // On Windows unity could not write log into stdout
        // so we need to read a log file contents
        if (SystemInfo.isWindows) {
            unityLogFile = File(build.buildTempDirectory, "unity.txt").apply {
                if (exists()) {
                    FileUtil.delete(this)
                    createNewFile()
                }
            }

            unityLogFileTailer = Tailer.create(unityLogFile, object: TailerListenerAdapter() {
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
    }

    companion object {
        private var DEFAULT_DELAY_MILLIS = 500L
    }
}
