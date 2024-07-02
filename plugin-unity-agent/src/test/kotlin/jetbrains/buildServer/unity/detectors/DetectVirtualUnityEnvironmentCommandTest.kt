package jetbrains.buildServer.unity.detectors

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine
import jetbrains.buildServer.unity.UnityEnvironment
import jetbrains.buildServer.unity.UnityVersion.Companion.parseVersion
import jetbrains.buildServer.unity.util.unityRootParam
import jetbrains.buildServer.unity.util.unityVersionParam
import jetbrains.buildServer.util.OSType
import jetbrains.buildServer.util.OSType.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DetectVirtualUnityEnvironmentCommandTest {

    private val runnerContext = mockk<BuildRunnerContext>()
    private val virtualContext = mockk<VirtualContext>()
    private val workingDirectory = mockk<File>()

    @BeforeMethod
    fun setUp() {
        clearAllMocks()

        every { runnerContext.isVirtualContext } returns true
        every { runnerContext.virtualContext } returns virtualContext
        every { runnerContext.workingDirectory } returns workingDirectory

        mockkStatic(runnerContext::unityRootParam)
        mockkStatic(runnerContext::unityVersionParam)
        every { runnerContext.unityRootParam() } returns null
        every { runnerContext.unityVersionParam() } returns null

        every { workingDirectory.path } returns "path"
        every { virtualContext.targetOSType } returns UNIX
        every { virtualContext.resolvePath(any()) } returnsArgument 0
    }

    @DataProvider
    fun `correct standard output`(): Array<Array<out Any?>> = arrayOf(
        arrayOf(
            "path=/path/to/Unity;version=2023.3.4",
            linkedSetOf(UnityEnvironment("/path/to/Unity", parseVersion("2023.3.4"), true)),
        ),
        arrayOf(
            "path=/path/to/Unity;version=2023.3.4\n",
            linkedSetOf(UnityEnvironment("/path/to/Unity", parseVersion("2023.3.4"), true)),
        ),
        arrayOf(
            "path=/path/to/Unity;version=2023.3.4\r",
            linkedSetOf(UnityEnvironment("/path/to/Unity", parseVersion("2023.3.4"), true)),
        ),
        arrayOf(
            "path=/path/to/Unity;version=2023.3.4\r\n",
            linkedSetOf(UnityEnvironment("/path/to/Unity", parseVersion("2023.3.4"), true)),
        ),
        arrayOf(
            "path=/path/to/1/Unity;version=2023.0.1\npath=/path/to/2/Unity;version=2023.0.2\n",
            linkedSetOf(
                UnityEnvironment("/path/to/1/Unity", parseVersion("2023.0.1"), true),
                UnityEnvironment("/path/to/2/Unity", parseVersion("2023.0.2"), true),
            ),
        ),
        arrayOf(
            "path=/path/to/1/Unity;version=2023.0.1\rpath=/path/to/2/Unity;version=2023.0.2\r",
            linkedSetOf(
                UnityEnvironment("/path/to/1/Unity", parseVersion("2023.0.1"), true),
                UnityEnvironment("/path/to/2/Unity", parseVersion("2023.0.2"), true),
            ),
        ),
        arrayOf(
            "path=/path/to/1/Unity;version=2023.0.1\r\npath=/path/to/2/Unity;version=2023.0.2\r\n",
            linkedSetOf(
                UnityEnvironment("/path/to/1/Unity", parseVersion("2023.0.1"), true),
                UnityEnvironment("/path/to/2/Unity", parseVersion("2023.0.2"), true),
            ),
        ),
        arrayOf(
            "path=/path/to/1/Unity;version=2023.0.1\npath=/path/to/2/Unity;version=2023.0.2\npath=/path/to/3/Unity;version=2023.0.3\n",
            linkedSetOf(
                UnityEnvironment("/path/to/1/Unity", parseVersion("2023.0.1"), true),
                UnityEnvironment("/path/to/2/Unity", parseVersion("2023.0.2"), true),
                UnityEnvironment("/path/to/3/Unity", parseVersion("2023.0.3"), true),
            ),
        ),
    )

    @Test(dataProvider = "correct standard output")
    fun `should parse correct standard output`(stdout: String, expectedEnvironment: Set<UnityEnvironment>) {
        // arrange
        val command = createInstance()

        // act
        command.onStandardOutput(stdout)

        // assert
        command.results shouldContainExactly expectedEnvironment
    }

    @DataProvider
    fun `wrong standard output`(): Array<Array<out Any?>> = arrayOf(
        arrayOf(""),
        arrayOf("\n"),
        arrayOf("\r"),
        arrayOf("\r\n"),
        arrayOf("path=/path/to/Unity"),
        arrayOf("version=2023.0.01"),
        arrayOf("path=/path/to/Unityversion=2023.0.01"),
        arrayOf("path=/path/to/Unity;version=wrong.version.format"),
    )

    @Test(dataProvider = "wrong standard output")
    fun `should not fail on wrong standard output`(stdout: String) {
        // arrange
        val command = createInstance()

        // act
        command.onStandardOutput(stdout)

        // assert
        command.results.shouldBeEmpty()
    }

    @Test
    fun `should skip detected Unity environment if an expected Unity version is different in params`() {
        // arrange
        val command = createInstance()
        val stdout = "path=/path/to/1/Unity;version=2023.0.1\npath=/path/to/2/Unity;version=2023.0.2\n"

        every { runnerContext.unityVersionParam() } returns parseVersion("2023.0.1")

        // act
        command.onStandardOutput(stdout)

        // assert
        command.results shouldHaveSize 1
        command.results shouldContain UnityEnvironment("/path/to/1/Unity", parseVersion("2023.0.1"), true)
    }

    @Test
    fun `should not create duplicate Unity environment in case of duplicate stdout`() {
        // arrange
        val command = createInstance()
        val stdout = "path=/path/to/Unity;version=2023.0.1\npath=/path/to/Unity;version=2023.0.1\n"

        // act
        command.onStandardOutput(stdout)

        // assert
        command.results shouldHaveSize 1
        command.results shouldContain UnityEnvironment("/path/to/Unity", parseVersion("2023.0.1"), true)
    }

    @DataProvider
    fun `OS type to expected script name`() = arrayOf(
        arrayOf(UNIX, "unity-environment-detector.sh"),
        arrayOf(MAC, "unity-environment-detector.sh"),
        arrayOf(WINDOWS, "unity-environment-detector.bat"),
    )

    @Test(dataProvider = "OS type to expected script name")
    fun `should make program command line`(osType: OSType, expectedScriptName: String) {
        // arrange
        val command = createInstance()

        every { virtualContext.targetOSType } returns osType
        every { runnerContext.buildParameters.environmentVariables } returns mapOf("ENV" to "VALUE")

        // act
        val commandLine = command.makeProgramCommandLine()

        // assert
        commandLine.shouldBeInstanceOf<SimpleProgramCommandLine>()
        commandLine.environment shouldContainExactly mapOf("ENV" to "VALUE")
        commandLine.workingDirectory shouldBeEqual "path"
        commandLine.arguments.shouldBeEmpty()
        commandLine.executablePath shouldEndWith expectedScriptName
    }

    @Test
    fun `should make program command line passing unity root ENV variable from params`() {
        // arrange
        val command = createInstance()

        every { runnerContext.buildParameters.environmentVariables } returns mapOf("ENV" to "VALUE")
        every { runnerContext.unityRootParam() } returns "/path/to/unity"

        // act
        val commandLine = command.makeProgramCommandLine()

        // assert
        commandLine.environment shouldContainExactly mapOf(
            "ENV" to "VALUE",
            "UNITY_ROOT_PARAMETER" to "/path/to/unity",
        )
    }

    private fun createInstance() = DetectVirtualUnityEnvironmentCommand(runnerContext)
}
