package jetbrains.buildServer.unity

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldContainOnlyOnce
import io.kotest.matchers.string.shouldNotContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.agent.AgentBuildFeature
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.agent.FlowLogger
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.unity.UnityConstants.PARAM_CACHE_SERVER
import jetbrains.buildServer.unity.UnityVersion.Companion.parseVersion
import jetbrains.buildServer.unity.util.FileSystemService
import org.testng.annotations.DataProvider
import java.io.File
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class UnityRunnerBuildServiceTest {
    private val defaultEditorPath = "somePath"
    private val defaultUnityVersion = parseVersion("2021.3.16")
    private val defaultUnityEnvironment = UnityEnvironment(defaultEditorPath, defaultUnityVersion)

    private val unityBuildRunnerContextMock = mockk<UnityBuildRunnerContext>(relaxed = true)
    private val agentRunningBuildMock = mockk<AgentRunningBuild>(relaxed = true)
    private val fileSystemServiceMock = mockk<FileSystemService>(relaxed = true)

    @BeforeTest
    fun setUp() {
        clearAllMocks()
        defaultMockSetup()
    }

    private fun defaultMockSetup() {
        unityBuildRunnerContextMock.apply {
            every { runnerParameters } returns mapOf(
                "noGraphics" to true.toString(),
            )
            every { buildParameters } returns mockk(relaxed = true)
            every { workingDirectory } returns File("dir")
            every { isVirtualContext } returns false
        }

        agentRunningBuildMock.apply {
            every { buildLogger } returns mockk(relaxed = true)
            every { buildId } returns Random.nextLong(0, Long.MAX_VALUE)
            every { getBuildFeaturesOfType(UnityConstants.BUILD_FEATURE_TYPE) } returns listOf(FakeUnityBuildFeature())
        }

        fileSystemServiceMock.apply {
            every { createPath(any()) } answers { callOriginal() }
            every { createPath(any(), any()) } answers { callOriginal() }
        }
    }

    @Test
    fun `should add correct log argument`() {
        // arrange
        val buildService = UnityRunnerBuildService(defaultUnityEnvironment, createUnityProject(), emptyMap(), fileSystemServiceMock)
        buildService.initialize(agentRunningBuildMock, unityBuildRunnerContextMock)

        // act
        val commandLine = buildService.makeProgramCommandLine()

        // assert
        commandLine shouldNotBe null
        val commandString = commandLine.arguments.joinToString(" ")
        commandString shouldContain "-logFile -"
    }

    @Test
    fun `should not add log argument if provided by user`() {
        // arrange
        unityBuildRunnerContextMock.apply {
            every { runnerParameters } returns mapOf(
                "arguments" to "-logFile 42.log",
            )
        }

        val buildService = UnityRunnerBuildService(defaultUnityEnvironment, createUnityProject(), emptyMap(), fileSystemServiceMock)
        buildService.initialize(agentRunningBuildMock, unityBuildRunnerContextMock)

        // act
        val commandLine = buildService.makeProgramCommandLine()

        // assert
        commandLine shouldNotBe null

        val commandString = commandLine.arguments.joinToString(" ")
        commandString shouldContainOnlyOnce "-logFile"
        commandString shouldContain "logFile 42.log"
    }

    @DataProvider
    fun `cache server cases`(): Array<Array<Any?>> = arrayOf(
        arrayOf(null),
        arrayOf(AssetPipelineVersion.V1),
    )

    @Test(dataProvider = "cache server cases")
    fun `should use arguments for old Unity Cache Server if Asset Pipeline version is unknown or determined as V1`(
        version: AssetPipelineVersion?,
    ) {
        // arrange
        every { agentRunningBuildMock.getBuildFeaturesOfType(UnityConstants.BUILD_FEATURE_TYPE) } returns listOf(
            FakeUnityBuildFeature(
                mapOf(
                    PARAM_CACHE_SERVER to "1.1.1.1:1111",
                ),
            ),
        )

        val unityProject = mockk<UnityProject> {
            every { assetPipelineVersion } returns version
        }

        val buildService = UnityRunnerBuildService(defaultUnityEnvironment, unityProject, emptyMap(), fileSystemServiceMock)
        buildService.initialize(agentRunningBuildMock, unityBuildRunnerContextMock)

        // act
        val commandLine = buildService.makeProgramCommandLine()

        // assert
        assertNotNull(commandLine)
        val arguments = commandLine.arguments.joinToString(separator = " ")
        arguments shouldNotContain "-EnableCacheServer"
        arguments shouldNotContain "-cacheServerEndpoint"
        arguments shouldContain "-CacheServerIPAddress"
    }

    @Test
    fun `should use arguments for Unity Accelerator if Asset Pipeline version determined as V2`() {
        // arrange
        every { agentRunningBuildMock.getBuildFeaturesOfType(UnityConstants.BUILD_FEATURE_TYPE) } returns listOf(
            FakeUnityBuildFeature(
                mapOf(
                    PARAM_CACHE_SERVER to "1.1.1.1:1111",
                ),
            ),
        )

        val unityProject = mockk<UnityProject> {
            every { assetPipelineVersion } returns AssetPipelineVersion.V2
        }

        val buildService = UnityRunnerBuildService(defaultUnityEnvironment, unityProject, emptyMap(), fileSystemServiceMock)
        buildService.initialize(agentRunningBuildMock, unityBuildRunnerContextMock)

        // act
        val commandLine = buildService.makeProgramCommandLine()

        // assert
        assertNotNull(commandLine)
        val arguments = commandLine.arguments.joinToString(separator = " ")
        arguments shouldContain "-EnableCacheServer"
        arguments shouldContain "-cacheServerEndpoint"
        arguments shouldNotContain "-CacheServerIPAddress"
    }

    data class QuitArgTestCase(
        val noQuitParam: String?,
        val runEditorTestsParam: String? = null,
        val shouldAddQuitArg: Boolean,
    )

    @DataProvider
    fun noQuitTestData(): Array<QuitArgTestCase> {
        return arrayOf(
            QuitArgTestCase(noQuitParam = "false", shouldAddQuitArg = true),
            QuitArgTestCase(noQuitParam = "", shouldAddQuitArg = true),
            QuitArgTestCase(noQuitParam = null, shouldAddQuitArg = true),

            QuitArgTestCase(noQuitParam = "true", shouldAddQuitArg = false),
            QuitArgTestCase(noQuitParam = "true", runEditorTestsParam = "true", shouldAddQuitArg = false),
            QuitArgTestCase(noQuitParam = "false", runEditorTestsParam = "true", shouldAddQuitArg = false),
            QuitArgTestCase(noQuitParam = "", runEditorTestsParam = "true", shouldAddQuitArg = false),
            QuitArgTestCase(noQuitParam = null, runEditorTestsParam = "true", shouldAddQuitArg = false),
        )
    }

    @Test(dataProvider = "noQuitTestData")
    fun `should correctly handle -quit argument`(case: QuitArgTestCase) {
        // arrange
        val buildService = UnityRunnerBuildService(defaultUnityEnvironment, createUnityProject(), emptyMap(), fileSystemServiceMock)
        unityBuildRunnerContextMock.apply {
            every { runnerParameters } returns mapOf(
                "noQuit" to case.noQuitParam,
                "runEditorTests" to case.runEditorTestsParam,
            )
        }
        buildService.initialize(agentRunningBuildMock, unityBuildRunnerContextMock)

        // act
        val commandLine = buildService.makeProgramCommandLine()

        // assert
        commandLine shouldNotBe null
        val commandString = commandLine.arguments.joinToString(" ")
        if (case.shouldAddQuitArg) {
            commandString shouldContain "-quit"
        } else {
            commandString shouldNotContain "-quit"
        }
    }

    @Test
    fun `should generate correct command line when virtual context is used`() {
        // arrange
        val virtualContext = mockk<VirtualContext> {
            every { resolvePath("./") } returns "/converted/project"
            every { resolvePath(File("/logs").absolutePath) } returns "/converted/logs"
            every { resolvePath(File("/player").absolutePath) } returns "/converted/player"
        }

        unityBuildRunnerContextMock.apply {
            every { isVirtualContext } returns true
            every { getVirtualContext() } returns virtualContext
            every { runnerParameters } returns mapOf(
                "noGraphics" to true.toString(),
                "logFilePath" to File("/logs").absolutePath,
                "buildPlayer" to "player",
                "buildPlayerPath" to File("/player").absolutePath,
            )
        }

        val buildService = UnityRunnerBuildService(defaultUnityEnvironment, createUnityProject(), emptyMap(), fileSystemServiceMock)
        buildService.initialize(agentRunningBuildMock, unityBuildRunnerContextMock)

        // act
        val commandLine = buildService.makeProgramCommandLine()

        // assert
        commandLine shouldNotBe null
        val commandString = commandLine.arguments.joinToString(" ")
        commandString shouldBeEqual "-batchmode -projectPath /converted/project " +
            "-player /converted/player -nographics -quit -logFile /converted/logs"
    }

    @Test
    fun `buildProfile set adds -activeBuildProfile argument and omits -buildTarget`() {
        unityBuildRunnerContextMock.apply {
            every { runnerParameters } returns mapOf(
                UnityConstants.PARAM_BUILD_PROFILE to "Assets/Settings/Build Profiles/Android.asset",
            )
        }

        val buildService = UnityRunnerBuildService(defaultUnityEnvironment, createUnityProject(), emptyMap(), fileSystemServiceMock)
        buildService.initialize(agentRunningBuildMock, unityBuildRunnerContextMock)

        val args = buildService.makeProgramCommandLine().arguments.joinToString(" ")
        args shouldContain "-activeBuildProfile Assets/Settings/Build Profiles/Android.asset"
        args shouldNotContain "-buildTarget"
    }

    @Test
    fun `buildProfile with buildPlayerPath adds -build argument`() {
        unityBuildRunnerContextMock.apply {
            every { runnerParameters } returns mapOf(
                UnityConstants.PARAM_BUILD_PROFILE to "Assets/Settings/Build Profiles/Android.asset",
                UnityConstants.PARAM_BUILD_PLAYER_PATH to File("output/game.apk").absolutePath,
            )
        }

        val buildService = UnityRunnerBuildService(defaultUnityEnvironment, createUnityProject(), emptyMap(), fileSystemServiceMock)
        buildService.initialize(agentRunningBuildMock, unityBuildRunnerContextMock)

        val args = buildService.makeProgramCommandLine().arguments.joinToString(" ")
        args shouldContain "-build"
    }

    @Test
    fun `createAdapters returns prewarm service first when buildProfile and buildTarget both set`() {
        every { unityBuildRunnerContextMock.runnerParameters } returns mapOf(
            UnityConstants.PARAM_BUILD_PROFILE to "Assets/Settings/Build Profiles/Android.asset",
            UnityConstants.PARAM_BUILD_TARGET to "Android",
        )
        every { unityBuildRunnerContextMock.unityProject } returns createUnityProject()

        val adapters = UnityRunnerBuildService.createAdapters(
            defaultUnityEnvironment,
            unityBuildRunnerContextMock,
            fileSystemServiceMock,
        ).toList()

        adapters shouldHaveSize 2
    }

    @Test
    fun `createAdapters returns prewarm service when buildProfile set without buildTarget`() {
        every { unityBuildRunnerContextMock.runnerParameters } returns mapOf(
            UnityConstants.PARAM_BUILD_PROFILE to "Assets/Settings/Build Profiles/Android.asset",
        )
        every { unityBuildRunnerContextMock.unityProject } returns createUnityProject()

        val adapters = UnityRunnerBuildService.createAdapters(
            defaultUnityEnvironment,
            unityBuildRunnerContextMock,
            fileSystemServiceMock,
        ).toList()

        adapters shouldHaveSize 2
    }

    @Test
    fun `buildProfile with Unity version below 6000 logs version warning`() {
        val oldVersion = parseVersion("2021.3.16")!!
        val logMessages = mutableListOf<String>()
        val flowLoggerMock = mockFlowLogger(logMessages)
        every { agentRunningBuildMock.buildLogger } returns flowLoggerMock

        unityBuildRunnerContextMock.apply {
            every { runnerParameters } returns mapOf(
                UnityConstants.PARAM_BUILD_PROFILE to "Assets/Settings/Build Profiles/Android.asset",
            )
        }

        val buildService = UnityRunnerBuildService(
            UnityEnvironment(defaultEditorPath, oldVersion),
            createUnityProject(),
            emptyMap(),
            fileSystemServiceMock,
        )
        buildService.initialize(agentRunningBuildMock, unityBuildRunnerContextMock)
        buildService.makeProgramCommandLine()

        assert(logMessages.any { it.contains("Build Profiles require Unity 6") }) {
            "Expected a warning about Unity 6 requirement, got: $logMessages"
        }
    }

    @Test
    fun `buildProfile with no output path and no execute method logs informational warning`() {
        val unity6Version = parseVersion("6000.0.0")!!
        val logMessages = mutableListOf<String>()
        val flowLoggerMock = mockFlowLogger(logMessages)
        every { agentRunningBuildMock.buildLogger } returns flowLoggerMock

        unityBuildRunnerContextMock.apply {
            every { runnerParameters } returns mapOf(
                UnityConstants.PARAM_BUILD_PROFILE to "Assets/Settings/Build Profiles/Android.asset",
            )
        }

        val buildService = UnityRunnerBuildService(
            UnityEnvironment(defaultEditorPath, unity6Version),
            createUnityProject(),
            emptyMap(),
            fileSystemServiceMock,
        )
        buildService.initialize(agentRunningBuildMock, unityBuildRunnerContextMock)
        buildService.makeProgramCommandLine()

        assert(logMessages.any { it.contains("Build Profile is set but no") }) {
            "Expected an informational warning about missing output path, got: $logMessages"
        }
    }

    private fun mockFlowLogger(logMessages: MutableList<String>): FlowLogger {
        val mock = mockk<FlowLogger>(relaxed = true)
        every { mock.message(any()) } answers { logMessages.add(firstArg()) }
        every { mock.getFlowLogger(any()) } returns mock
        return mock
    }

    private fun createUnityProject() = UnityProject(mockk())

    private inner class FakeUnityBuildFeature(
        init: Map<String, String> = mapOf(),
    ) : AgentBuildFeature {
        private val parameters: MutableMap<String, String> = (
            mapOf(
                UnityConstants.PARAM_UNITY_VERSION to defaultUnityVersion.toString(),
            ) + init
            ).toMutableMap()

        override fun getType() = UnityConstants.BUILD_FEATURE_TYPE
        override fun getParameters(): MutableMap<String, String> = parameters
    }
}
