

package jetbrains.buildServer.unity

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.TCSystemInfo
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.BuildServiceAdapter
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.messages.Status.WARNING
import jetbrains.buildServer.messages.serviceMessages.Message
import jetbrains.buildServer.unity.UnityConstants.BUILD_FEATURE_TYPE
import jetbrains.buildServer.unity.UnityConstants.PARAM_ARGUMENTS
import jetbrains.buildServer.unity.UnityConstants.PARAM_BUILD_PLAYER
import jetbrains.buildServer.unity.UnityConstants.PARAM_BUILD_PLAYER_PATH
import jetbrains.buildServer.unity.UnityConstants.PARAM_BUILD_TARGET
import jetbrains.buildServer.unity.UnityConstants.PARAM_CACHE_SERVER
import jetbrains.buildServer.unity.UnityConstants.PARAM_EXECUTE_METHOD
import jetbrains.buildServer.unity.UnityConstants.PARAM_LINE_STATUSES_FILE
import jetbrains.buildServer.unity.UnityConstants.PARAM_NO_GRAPHICS
import jetbrains.buildServer.unity.UnityConstants.PARAM_NO_QUIT
import jetbrains.buildServer.unity.UnityConstants.PARAM_PROJECT_PATH
import jetbrains.buildServer.unity.UnityConstants.PARAM_RUN_EDITOR_TESTS
import jetbrains.buildServer.unity.UnityConstants.PARAM_SILENT_CRASHES
import jetbrains.buildServer.unity.UnityConstants.PARAM_TEST_CATEGORIES
import jetbrains.buildServer.unity.UnityConstants.PARAM_TEST_NAMES
import jetbrains.buildServer.unity.UnityConstants.PARAM_TEST_PLATFORM
import jetbrains.buildServer.unity.UnityConstants.PARAM_UNITY_LOG_FILE
import jetbrains.buildServer.unity.UnityConstants.PARAM_VERBOSITY
import jetbrains.buildServer.unity.UnityVersion.UnitySpecialVersions.UNITY_2018_2_0
import jetbrains.buildServer.unity.UnityVersion.UnitySpecialVersions.UNITY_2019_1_0
import jetbrains.buildServer.unity.Verbosity.Minimal
import jetbrains.buildServer.unity.Verbosity.Normal
import jetbrains.buildServer.unity.logging.LineStatusProvider
import jetbrains.buildServer.unity.logging.UnityLoggingListener
import jetbrains.buildServer.unity.messages.ImportData
import jetbrains.buildServer.unity.util.FileSystemService
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.util.StringUtil.splitCommandArgumentsAndUnquote
import org.apache.commons.io.input.Tailer
import org.apache.commons.io.input.TailerListenerAdapter
import java.io.File
import java.io.FileNotFoundException
import java.io.RandomAccessFile
import kotlin.io.path.absolutePathString
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

class UnityRunnerBuildService(
    private val unityEnvironment: UnityEnvironment,
    private val unityProject: UnityProject,
    private val overriddenRunnerParameters: Map<String, String>,
    private val fileSystemService: FileSystemService,
) : BuildServiceAdapter() {

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
            logger.message(Message(message, WARNING.text, null).asString())
            LOG.infoAndDebugDetails(message, e)
            LineStatusProvider()
        }
        listOf(UnityLoggingListener(logger, problemsProvider))
    }

    private val parameters: Lazy<Map<String, String>>
        get() = lazy { runnerParameters + overriddenRunnerParameters }

    private val verbosity: Verbosity by lazy {
        parameters.value[PARAM_VERBOSITY]?.let {
            Verbosity.tryParse(it)
        } ?: Normal
    }

    private val logFilePath: String?
        get() = parameters.value[PARAM_UNITY_LOG_FILE]?.trim()

    private val verbosityArgument: String
        get() = when (verbosity) {
            Minimal -> ARG_CLEANED_LOG_FILE
            else -> ARG_LOG_FILE
        }

    override fun makeProgramCommandLine(): ProgramCommandLine {
        val unityVersion = unityEnvironment.unityVersion
        val unityPath = unityEnvironment.unityPath

        val arguments: MutableList<String> = sequence {
            yield(ARG_BATCH_MODE)
            projectPathArg(unityVersion)
            argIfNotEmpty(PARAM_BUILD_TARGET, ARG_BUILD_TARGET)
            buildPlayerArg()
            argIfTrue(PARAM_NO_GRAPHICS, ARG_NO_GRAPHICS)
            argIfTrue(PARAM_SILENT_CRASHES, ARG_SILENT_CRASHES)
            argIfNotEmpty(PARAM_EXECUTE_METHOD, ARG_EXECUTE_METHOD)
            otherArgs()
        }.toMutableList()

        addRunTestsArgs(arguments)
        addLogArgIfNotExists(arguments, unityVersion)

        createLineStatusesFile()
        addArgsFromBuildFeature(arguments, unityProject)

        return createProgramCommandline(unityPath, arguments)
    }

    private suspend fun SequenceScope<String>.projectPathArg(unityVersion: UnityVersion) {
        var projectPath = "./"
        parameters.value[PARAM_PROJECT_PATH]?.let {
            if (it.isNotEmpty()) {
                projectPath = it.trim()
            }
        }

        if (unityVersion > UNITY_2018_2_0) {
            yield(ARG_PROJECT_PATH)
            yield(resolvePath(projectPath))
        } else {
            // In Unity < 2018.2 we should specify project path argument with equals sign
            // https://answers.unity.com/questions/622429/i-have-a-problem-the-log-is-couldnt-set-project-pa.html
            val path = if (projectPath.contains(' ')) {
                StringUtil.doubleQuote(projectPath)
            } else {
                projectPath
            }
            yield("$ARG_PROJECT_PATH=$path")
        }
    }

    private suspend fun SequenceScope<String>.argIfNotEmpty(parameter: String, argument: String) {
        parameters.value[parameter]?.let {
            if (it.isNotEmpty()) {
                yield(argument)
                yield(it.trim())
            }
        }
    }

    private suspend fun SequenceScope<String>.argIfTrue(parameter: String, argument: String) {
        parameters.value[parameter]?.let {
            if (it.toBoolean()) {
                yield(argument)
            }
        }
    }

    private suspend fun SequenceScope<String>.buildPlayerArg() {
        parameters.value[PARAM_BUILD_PLAYER]?.let {
            val playerPath = parameters.value[PARAM_BUILD_PLAYER_PATH]
            if (it.isNotEmpty() && !playerPath.isNullOrEmpty()) {
                var playerFile = fileSystemService.createPath(playerPath.trim())
                if (!playerFile.isAbsolute) {
                    playerFile = fileSystemService.createPath(workingDirectory.toPath(), playerPath.trim())
                }
                yield("-" + it.trim())
                yield(resolvePath(playerFile.absolutePathString()))
            }
        }
    }

    private fun addArgsFromBuildFeature(arguments: MutableList<String>, unityProject: UnityProject) {
        build.getBuildFeaturesOfType(BUILD_FEATURE_TYPE).firstOrNull()?.let { feature ->
            val cacheServerParam = feature.parameters[PARAM_CACHE_SERVER]?.trim()
            if (!cacheServerParam.isNullOrEmpty()) {
                when (unityProject.assetPipelineVersion) {
                    null, AssetPipelineVersion.V1 -> {
                        val cacheServerLog = "Asset Pipeline version is either not specified or is V1. Arguments for the old cache server will be used"
                        logger.message(cacheServerLog)
                        LOG.info(cacheServerLog)
                        arguments.addAll(listOf(ARG_CACHE_SERVER_IP_ADDRESS, cacheServerParam))
                    }
                    AssetPipelineVersion.V2 -> {
                        val unityAcceleratorLog = "Asset Pipeline version is determined as V2. Arguments for the new Unity Accelerator cache server will be used"
                        logger.message(unityAcceleratorLog)
                        LOG.info(unityAcceleratorLog)
                        arguments.addAll(listOf(ARG_ENABLE_CACHE_SERVER, ARG_CACHE_SERVER_ENDPOINT, cacheServerParam))
                    }
                }
            }
        }
    }

    private fun createLineStatusesFile() {
        parameters.value[PARAM_LINE_STATUSES_FILE]?.let {
            if (it.isNotEmpty()) {
                unityLineStatusesFile = fileSystemService.createPath(workingDirectory.toPath(), it.trim()).toFile()
            }
        }
    }

    private suspend fun SequenceScope<String>.otherArgs() {
        parameters.value[PARAM_ARGUMENTS]?.let {
            if (it.isNotEmpty()) {
                splitCommandArgumentsAndUnquote(it).forEach { arg ->
                    yield(arg)
                }
            }
        }
    }

    private fun addRunTestsArgs(arguments: MutableList<String>) {
        val runTests = parameters.value[PARAM_RUN_EDITOR_TESTS]?.toBoolean() ?: false
        val testPlatform = parameters.value[PARAM_TEST_PLATFORM]

        // For tests run we should not add -quit argument
        val runTestIndex = arguments.indexOfFirst { RUN_TESTS_REGEX.matches(it) }
        if (runTestIndex < 0) {
            if (runTests) {
                // Append -runTests argument if selected test platform
                // otherwise use -runEditorTests argument
                if (testPlatform.isNullOrEmpty()) {
                    arguments.add(ARG_RUN_EDITOR_TESTS)
                } else {
                    arguments.addAll(listOf(ARG_RUN_TESTS, ARG_TEST_PLATFORM, testPlatform))
                }
            } else {
                val shouldAddQuitArg = !parameters.value[PARAM_NO_QUIT].toBoolean()
                if (shouldAddQuitArg) {
                    arguments.add(ARG_QUIT)
                }
                return
            }
        }

        // Check test results argument
        val index = arguments.indexOfFirst { RUN_TEST_RESULTS_REGEX.matches(it) }
        unityTestsReportFile = if (index > 0 && index + 1 < arguments.size) {
            val testsResultPath = arguments[index + 1]
            fileSystemService.createPath(testsResultPath).toFile()
        } else {
            fileSystemService.createTempFile(build.agentTempDirectory.toPath(), "unityTestResults-", ".xml").toFile()
                .apply {
                    val testResultsArgument = if (testPlatform.isNullOrEmpty()) {
                        ARG_EDITOR_TESTS_RESULT_FILE
                    } else {
                        ARG_TEST_RESULTS_FILE
                    }
                    arguments.addAll(listOf(testResultsArgument, resolvePath(this.absolutePath)))
                }
        }

        parameters.value[PARAM_TEST_CATEGORIES]?.let {
            if (it.isNotEmpty()) {
                val categories = StringUtil.split(it).joinToString(";")
                arguments.addAll(listOf(ARG_EDITOR_TESTS_CATEGORIES, categories))
            }
        }

        parameters.value[PARAM_TEST_NAMES]?.let {
            if (it.isNotEmpty()) {
                val names = StringUtil.split(it).joinToString(";")
                arguments.addAll(listOf(ARG_EDITOR_TESTS_FILTER, names))
            }
        }

        // apply quiet mode for test xml reports watcher
        runnerContext.addRunnerParameter("xmlReportParsing.quietMode", "true")
    }

    override fun isCommandLineLoggingEnabled() = true

    override fun afterProcessFinished() {
        unityLogFileTailer?.apply {
            // Wait while Tailer will complete read
            Thread.sleep(TAIL_DELAY_DURATION.toMillis())
            close()
        }
        unityTestsReportFile?.let {
            if (it.exists()) {
                logger.message(ImportData("nunit", it.absolutePath).asString())
            }
        }
    }

    override fun getListeners() = unityListeners

    private fun addLogArgIfNotExists(arguments: MutableList<String>, version: UnityVersion) {
        // Log file is already provided by user in command line arguments
        if (hasCustomLogArg()) {
            return
        }

        val verbosityArg = verbosityArgument
        arguments.add(verbosityArg)

        // On Windows unity could not write log into stdout, so we need to read a log file contents:
        // https://issuetracker.unity3d.com/issues/command-line-logfile-with-no-parameters-outputs-to-screen-on-os-x-but-not-on-windows
        // Was resolved in 2019.1 but only for -logFile with -nographics option
        fun currentSetupSupportsConsoleOutput() = !TCSystemInfo.isWindows ||
            (version >= UNITY_2019_1_0 && verbosityArg == ARG_LOG_FILE && arguments.contains(ARG_NO_GRAPHICS))

        if (logFilePath.isNullOrEmpty() && currentSetupSupportsConsoleOutput()) {
            arguments.add("-")
            return
        }

        val logPath = if (logFilePath.isNullOrEmpty()) {
            fileSystemService.createTempFile(build.agentTempDirectory.toPath(), "unityBuildLog-", ".txt")
        } else {
            val path = fileSystemService.createPath(logFilePath!!)
            trimLog(path.toFile())
            path
        }

        arguments.add(resolvePath(logPath.absolutePathString()))

        unityLogFileTailer = Tailer.builder()
            .setFile(logPath.toFile())
            .setTailFromEnd(false)
            .setDelayDuration(TAIL_DELAY_DURATION)
            .setTailerListener(object : TailerListenerAdapter() {
                override fun handle(line: String) {
                    listeners.forEach {
                        it.onStandardOutput(line)
                    }
                }

                override fun fileRotated() {
                    unityLogFileTailer?.close()
                }
            }).get()
    }

    private fun hasCustomLogArg() = arrayOf(ARG_LOG_FILE, ARG_CLEANED_LOG_FILE)
        .any { parameters.value[PARAM_ARGUMENTS]?.contains(it) == true }

    private fun trimLog(logFile: File) {
        var logFileAccess: RandomAccessFile? = null
        try {
            logFileAccess = RandomAccessFile(logFile, LOG_FILE_ACCESS_MODE)
            logFileAccess.setLength(0)
        } catch (e: FileNotFoundException) {
            return
        } catch (e: Throwable) {
            val message = "Failed to truncate log file $logFile"
            logger.message(Message(message, WARNING.text, null).asString())
            LOG.infoAndDebugDetails(message, e)
        } finally {
            logFileAccess?.close()
        }
    }

    private fun resolvePath(path: String) =
        if (runnerContext.isVirtualContext) {
            runnerContext.virtualContext.resolvePath(path)
        } else {
            path
        }

    companion object {
        private val LOG = Logger.getInstance(UnityRunnerBuildService::class.java.name)

        private const val ARG_BATCH_MODE = "-batchmode"
        private const val ARG_PROJECT_PATH = "-projectPath"
        private const val ARG_BUILD_TARGET = "-buildTarget"
        private const val ARG_SILENT_CRASHES = "-silent-crashes"
        private const val ARG_EXECUTE_METHOD = "-executeMethod"

        private const val ARG_RUN_TESTS = "-runTests"
        private const val ARG_TEST_PLATFORM = "-testPlatform"
        private const val ARG_RUN_EDITOR_TESTS = "-runEditorTests"
        private const val ARG_EDITOR_TESTS_CATEGORIES = "-editorTestsCategories"
        private const val ARG_EDITOR_TESTS_FILTER = "-editorTestsFilter"
        private const val ARG_TEST_RESULTS_FILE = "-testResults"
        private const val ARG_EDITOR_TESTS_RESULT_FILE = "-editorTestsResultFile"

        private const val ARG_LOG_FILE = "-logFile"
        private const val ARG_CLEANED_LOG_FILE = "-cleanedLogFile"
        private const val ARG_NO_GRAPHICS = "-nographics"
        private const val ARG_QUIT = "-quit"
        private const val ARG_CACHE_SERVER_IP_ADDRESS = "-CacheServerIPAddress"
        private const val ARG_ENABLE_CACHE_SERVER = "-EnableCacheServer"
        private const val ARG_CACHE_SERVER_ENDPOINT = "-cacheServerEndpoint"

        private const val LOG_FILE_ACCESS_MODE = "rw"

        private val RUN_TESTS_REGEX = Regex("-run(Editor)?Tests")
        private val RUN_TEST_RESULTS_REGEX = Regex("-(editorTestsResultFile|testResults)")
        private val TAIL_DELAY_DURATION = 500L.toDuration(DurationUnit.MILLISECONDS).toJavaDuration()

        fun createAdapters(
            unityEnvironment: UnityEnvironment,
            context: UnityBuildRunnerContext,
            fileSystemService: FileSystemService,
        ) = getParameterVariants(context)
            .ifEmpty {
                sequenceOf(emptyMap())
            }
            .map {
                UnityRunnerBuildService(
                    unityEnvironment,
                    context.unityProject,
                    it,
                    fileSystemService,
                )
            }

        private fun getParameterVariants(context: BuildRunnerContext) = sequence {
            if ("all".equals(context.runnerParameters[PARAM_TEST_PLATFORM], true)) {
                yield(mapOf(PARAM_TEST_PLATFORM to "editmode"))
                yield(mapOf(PARAM_TEST_PLATFORM to "playmode"))
            }
        }
    }
}
