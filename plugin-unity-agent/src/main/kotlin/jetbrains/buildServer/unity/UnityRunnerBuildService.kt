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
import jetbrains.buildServer.agent.runner.BuildServiceAdapter
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.messages.Status
import jetbrains.buildServer.messages.serviceMessages.Message
import jetbrains.buildServer.unity.logging.LineStatusProvider
import jetbrains.buildServer.unity.logging.UnityLoggingListener
import jetbrains.buildServer.unity.messages.ImportData
import jetbrains.buildServer.util.StringUtil
import org.apache.commons.io.input.Tailer
import org.apache.commons.io.input.TailerListenerAdapter
import java.io.File

/**
 * Unity runner service.
 */
class UnityRunnerBuildService(private val unityToolProvider: UnityToolProvider) : BuildServiceAdapter() {

    private var unityTestsReportFile: File? = null
    private var unityLogFileTailer: Tailer? = null
    private var unityLineStatusesFile: File? = null
    private val unityListeners by lazy {
        val statusesFile = unityLineStatusesFile
        val problemsProvider = try {
            if (statusesFile != null && statusesFile.exists()) {
                LineStatusProvider(statusesFile).apply {
                    logger.message("Using line statuses file $statusesFile")
                }
            } else {
                LineStatusProvider()
            }
        } catch (e: Exception) {
            val message = "Failed to parse file $statusesFile with line statuses"
            logger.message(Message(message, Status.WARNING.text, null).asString())
            LOG.infoAndDebugDetails(message, e)
            LineStatusProvider()
        }
        listOf(UnityLoggingListener(logger, problemsProvider))
    }

    private val verbosity: Verbosity by lazy {
        runnerParameters[UnityConstants.PARAM_VERBOSITY]?.let {
            Verbosity.tryParse(it)
        } ?: Verbosity.Normal
    }

    private val logFilePath: String?
        get() = runnerParameters[UnityConstants.PARAM_UNITY_LOG_FILE]?.trim()

    private val verbosityArgument: String
        get() = when (verbosity) {
            Verbosity.Minimal -> "-cleanedLogFile"
            else -> ARG_LOG_FILE
        }

    override fun makeProgramCommandLine(): ProgramCommandLine {
        val (version, toolPath) = unityToolProvider.getUnity(UnityConstants.RUNNER_TYPE, build, runnerContext)
        val arguments = mutableListOf("-batchmode")

        var projectPath = "./"
        runnerParameters[UnityConstants.PARAM_PROJECT_PATH]?.let {
            if (it.isNotEmpty()) {
                projectPath = it.trim()
            }
        }

        if (version > UNITY_2018_2) {
            arguments.addAll(listOf("-projectPath", projectPath))
        } else {
            // In Unity < 2018.2 we should specify project path argument with equals sign
            // https://answers.unity.com/questions/622429/i-have-a-problem-the-log-is-couldnt-set-project-pa.html
            val path = if (projectPath.contains(' ')) {
                StringUtil.doubleQuote(projectPath)
            } else {
                projectPath
            }
            arguments.add("-projectPath=$path")
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
                arguments.add(ARG_NO_GRAPHICS)
            }
        }

        runnerParameters[UnityConstants.PARAM_SILENT_CRASHES]?.let {
            if (it.toBoolean()) {
                arguments.add("-silent-crashes")
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

        appendRunTestsArguments(arguments)

        appendLogArgument(arguments, version)

        // Use line statuses file if available
        runnerParameters[UnityConstants.PARAM_LINE_STATUSES_FILE]?.let {
            if (it.isNotEmpty()) {
                unityLineStatusesFile = File(workingDirectory, it.trim())
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

        return createProgramCommandline(toolPath, arguments)
    }

    private fun appendRunTestsArguments(arguments: MutableList<String>) {
        val runTests = runnerParameters[UnityConstants.PARAM_RUN_EDITOR_TESTS]?.toBoolean() ?: false
        val testPlatform = runnerParameters[UnityConstants.PARAM_TEST_PLATFORM]

        // For tests run we should not add -quit argument
        val runTestIndex = arguments.indexOfFirst { RUN_TESTS_REGEX.matches(it) }
        if (runTestIndex < 0) {
            if (runTests) {
                // Append -runTests argument if selected test platform
                // otherwise use -runEditorTests argument
                if (testPlatform.isNullOrEmpty()) {
                    arguments.add(ARG_RUN_EDITOR_TESTS)
                } else {
                    arguments.addAll(listOf(ARG_RUN_TESTS, "-testPlatform", testPlatform))
                }
            } else {
                arguments.add("-quit")
                return
            }
        }

        // Check test results argument
        val index = arguments.indexOfFirst { RUN_TEST_RESULTS_REGEX.matches(it)}
        unityTestsReportFile = if (index > 0 && index + 1 < arguments.size) {
            val testsResultPath = arguments[index + 1]
            File(testsResultPath)
        } else {
            File.createTempFile(
                    "unityTestResults-",
                    ".xml",
                    build.buildTempDirectory
            ).apply {
                val testResultsArgument = if (testPlatform.isNullOrEmpty()) {
                    ARG_EDITOR_TESTS_RESULT_FILE
                } else {
                    ARG_TEST_RESULTS_FILE
                }
                arguments.addAll(listOf(testResultsArgument, this.absolutePath))
            }
        }

        runnerParameters[UnityConstants.PARAM_TEST_CATEGORIES]?.let {
            if (it.isNotEmpty()) {
                val categories = StringUtil.split(it).joinToString(";")
                arguments.addAll(listOf("-editorTestsCategories", categories))
            }
        }

        runnerParameters[UnityConstants.PARAM_TEST_NAMES]?.let {
            if (it.isNotEmpty()) {
                val names = StringUtil.split(it).joinToString(";")
                arguments.addAll(listOf("-editorTestsFilter", names))
            }
        }

        // apply quiet mode for test xml reports watcher
        runnerContext.addRunnerParameter("xmlReportParsing.quietMode", "true")
    }

    override fun isCommandLineLoggingEnabled() = true

    override fun afterProcessFinished() {
        unityLogFileTailer?.apply {
            // Wait while Tailer will complete read
            Thread.sleep(DEFAULT_DELAY_MILLIS)
            stop()
        }
        unityTestsReportFile?.let {
            if (it.exists()) {
                logger.message(ImportData("nunit", it.absolutePath).asString())
            }
        }
    }

    override fun getListeners() = unityListeners

    private fun appendLogArgument(arguments: MutableList<String>, version: Semver) {
        val verbosityArg = verbosityArgument
        arguments.add(verbosityArg)

        if (!SystemInfo.isWindows && logFilePath.isNullOrEmpty()) {
            return
        }

        // On Windows unity could not write log into stdout, so we need to read a log file contents:
        // https://issuetracker.unity3d.com/issues/command-line-logfile-with-no-parameters-outputs-to-screen-on-os-x-but-not-on-windows
        // Was resolved in 2019.1 but only for -logFile with -nographics option
        if (version >= UNITY_2019 && verbosityArg == ARG_LOG_FILE && arguments.contains(ARG_NO_GRAPHICS) && logFilePath.isNullOrEmpty()) {
            return
        }

        val logFile = if (logFilePath.isNullOrEmpty()) {
            File.createTempFile(
                    "unityBuildLog-",
                    ".txt",
                    build.buildTempDirectory
            )
        } else {
            File(logFilePath)
        }

        arguments.add(logFile.absolutePath)

        unityLogFileTailer = Tailer.create(logFile, object : TailerListenerAdapter() {
            override fun handle(line: String) {
                listeners.forEach {
                    it.onStandardOutput(line)
                }
            }
            override fun fileRotated() {
                unityLogFileTailer?.stop()
            }
        }, DEFAULT_DELAY_MILLIS, false)
    }

    companion object {
        private val LOG = Logger.getInstance(UnityRunnerBuildService::class.java.name)
        private const val DEFAULT_DELAY_MILLIS = 500L
        private const val ARG_RUN_TESTS = "-runTests"
        private const val ARG_RUN_EDITOR_TESTS = "-runEditorTests"
        private const val ARG_TEST_RESULTS_FILE = "-testResults"
        private const val ARG_EDITOR_TESTS_RESULT_FILE = "-editorTestsResultFile"
        private const val ARG_LOG_FILE = "-logFile"
        private const val ARG_NO_GRAPHICS = "-nographics"
        private val RUN_TESTS_REGEX = Regex("-run(Editor)?Tests")
        private val RUN_TEST_RESULTS_REGEX = Regex("-(editorTestsResultFile|testResults)")
        private val UNITY_2018_2 = Semver("2018.2.0")
        private val UNITY_2019 = Semver("2019.1.0")
    }
}
