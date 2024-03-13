

package jetbrains.buildServer.unity

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
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.unity.UnityVersion.Companion.parseVersion
import jetbrains.buildServer.unity.util.FileSystemService
import org.testng.annotations.DataProvider
import java.io.File
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test

class UnityRunnerBuildServiceTest {
    private val defaultEditorPath = "somePath"
    private val defaultUnityVersion = parseVersion("2021.3.16")
    private val defaultUnityEnvironment = UnityEnvironment(defaultEditorPath, defaultUnityVersion)

    private val buildRunnerContextMock = mockk<BuildRunnerContext>(relaxed = true)
    private val agentRunningBuildMock = mockk<AgentRunningBuild>(relaxed = true)
    private val fileSystemServiceMock = mockk<FileSystemService>(relaxed = true)

    @BeforeTest
    fun setUp() {
        clearAllMocks()
        defaultMockSetup()
    }

    private fun defaultMockSetup() {
        buildRunnerContextMock.apply {
            every { runnerParameters } returns mapOf(
                "noGraphics" to true.toString()
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

    data class LogArgumentTestCase(
        val system: String,
        val unityVersion: UnityVersion
    )

    @DataProvider(name = "consoleLogOutput")
    fun logArgumentTestData(): Array<LogArgumentTestCase> {
        return arrayOf(
            LogArgumentTestCase("windows", defaultUnityVersion),
            LogArgumentTestCase("linux", defaultUnityVersion),
            LogArgumentTestCase("mac", defaultUnityVersion)
        )
    }

    @Test(dataProvider = "consoleLogOutput")
    fun `should add correct log argument`(case: LogArgumentTestCase) {
        // arrange
        val sut = UnityRunnerBuildService(defaultUnityEnvironment, emptyMap(), fileSystemServiceMock)
        sut.initialize(agentRunningBuildMock, buildRunnerContextMock)

        System.setProperty("os.name", case.system)

        // act
        val commandLine = sut.makeProgramCommandLine()

        // assert
        commandLine shouldNotBe null
        val commandString = commandLine.arguments.joinToString(" ")
        commandString shouldContain "-logFile -"
    }


    @Test
    fun `should not add log argument if provided by user`() {
        // arrange
        buildRunnerContextMock.apply {
            every { runnerParameters } returns mapOf(
                "arguments" to "-logFile 42.log",
            )
        }

        val sut = UnityRunnerBuildService(defaultUnityEnvironment, emptyMap(), fileSystemServiceMock)
        sut.initialize(agentRunningBuildMock, buildRunnerContextMock)

        // act
        val commandLine = sut.makeProgramCommandLine()

        // assert
        commandLine shouldNotBe null

        val commandString = commandLine.arguments.joinToString(" ")
        commandString shouldContainOnlyOnce "-logFile"
        commandString shouldContain "logFile 42.log"
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
        val sut = UnityRunnerBuildService(defaultUnityEnvironment, emptyMap(), fileSystemServiceMock)
        buildRunnerContextMock.apply {
            every { runnerParameters } returns mapOf(
                "noQuit" to case.noQuitParam,
                "runEditorTests" to case.runEditorTestsParam,
            )
        }
        sut.initialize(agentRunningBuildMock, buildRunnerContextMock)

        // act
        val commandLine = sut.makeProgramCommandLine()

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

        buildRunnerContextMock.apply {
            every { isVirtualContext } returns true
            every { getVirtualContext() } returns virtualContext
            every { runnerParameters } returns mapOf(
                "noGraphics" to true.toString(),
                "logFilePath" to File("/logs").absolutePath,
                "buildPlayer" to "player",
                "buildPlayerPath" to File("/player").absolutePath
            )
        }

        val sut = UnityRunnerBuildService(defaultUnityEnvironment, emptyMap(), fileSystemServiceMock)
        sut.initialize(agentRunningBuildMock, buildRunnerContextMock)

        System.setProperty("os.name", "linux")

        // act
        val commandLine = sut.makeProgramCommandLine()

        // assert
        commandLine shouldNotBe null
        val commandString = commandLine.arguments.joinToString(" ")
        commandString shouldBeEqual "-batchmode -projectPath /converted/project " +
                "-player /converted/player -nographics -quit -logFile /converted/logs"
    }

    private inner class FakeUnityBuildFeature(
        init: Map<String, String> = mapOf()
    ) : AgentBuildFeature {
        private val parameters: MutableMap<String, String>

        init {
            parameters = (mapOf(
                UnityConstants.PARAM_UNITY_VERSION to defaultUnityVersion.toString(),
            ) + init).toMutableMap()
        }

        override fun getType() = UnityConstants.BUILD_FEATURE_TYPE
        override fun getParameters(): MutableMap<String, String> = parameters
    }
}