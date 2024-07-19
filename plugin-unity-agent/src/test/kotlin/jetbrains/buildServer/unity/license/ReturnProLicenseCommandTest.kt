package jetbrains.buildServer.unity.license

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.messages.BlockData
import jetbrains.buildServer.unity.UnityEnvironment
import jetbrains.buildServer.unity.UnityVersion
import jetbrains.buildServer.unity.license.commands.ReturnProLicenseCommand
import jetbrains.buildServer.unity.util.FileSystemService
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

class ReturnProLicenseCommandTest {

    private val build = mockk<AgentRunningBuild>()
    private val buildLogger = mockk<FlowLogger>(relaxed = true)
    private val buildFeature = mockk<AgentBuildFeature>()

    private val fileSystemService = mockk<FileSystemService>()
    private val workingDirectoryPath = "/path/to/workingDir"
    private val agentTempDirectory = mockk<File>()

    @BeforeMethod
    fun setUp() {
        clearAllMocks()

        every { agentTempDirectory.toPath() } returns Paths.get("temp")

        every { buildFeature.parameters } returns mapOf()

        every { fileSystemService.createTempFile(any(), any(), any()) } returns Paths.get("foo")

        every { buildLogger.getFlowLogger(any()) } returns buildLogger

        every { build.getBuildFeaturesOfType(any()) } returns listOf(buildFeature)
        every { build.buildLogger } returns buildLogger
        every { build.agentTempDirectory } returns agentTempDirectory
        every { build.buildId } returns 1
    }

    @Test
    fun `should make program command line`() {
        // arrange
        val command = createInstance()
        val logFile = mockk<Path>()
        val logPath = "/path/to/logs.txt"
        val unityEnvironment = anUnityEnvironment()
        command.withUnityEnvironment(unityEnvironment)

        every {
            fileSystemService.createTempFile(agentTempDirectory.toPath(), "return-license-log-", "-1.txt")
        } returns logFile
        every { logFile.absolutePathString() } returns logPath
        every { buildFeature.parameters } returns mapOf(
            "username" to "someUsername",
            "secure:password" to "somePassword",
        )

        command.beforeProcessStarted()

        // act
        val result = command.makeProgramCommandLine()

        // assert
        result.executablePath shouldBe unityEnvironment.unityPath
        result.workingDirectory shouldBe workingDirectoryPath
        result.environment shouldBe mapOf()
        result.arguments.shouldContainExactly(
            listOf(
                "-quit", "-batchmode", "-nographics", "-returnlicense",
                "-username", "someUsername", "-password", "somePassword",
                "-logFile", logPath,
            ),
        )
    }

    @Test
    fun `should open a log block before process started`() {
        // arrange
        val command = createInstance()

        every { buildLogger.logMessage(any()) } returns Unit
        every { buildLogger.message(any()) } returns Unit

        // act
        command.beforeProcessStarted()

        // assert
        verify(exactly = 1) {
            buildLogger.logMessage(
                withArg {
                    it.value shouldBe BlockData("Return Unity license", "unity")
                    it.typeId shouldBe "BlockStart"
                },
            )
        }
    }

    @Test
    fun `should close a log block when process is finished`() {
        // arrange
        val command = createInstance()
        every { buildLogger.logMessage(any()) } returns Unit

        // act
        command.processFinished(0)

        // assert
        verify(exactly = 1) {
            buildLogger.logMessage(
                withArg {
                    it.value shouldBe BlockData("Return Unity license", "unity")
                    it.typeId shouldBe "BlockEnd"
                },
            )
        }
    }

    private fun anUnityEnvironment(): UnityEnvironment {
        val unityPath = "/path/to/unity"
        val unityVersion = UnityVersion(2023, 1, 1)
        return UnityEnvironment(unityPath, unityVersion)
    }

    private fun createInstance() = ReturnProLicenseCommand(
        object : LicenseCommandContext {
            override val build = this@ReturnProLicenseCommandTest.build
            override val buildLogger = this@ReturnProLicenseCommandTest.buildLogger
            override val fileSystemService = this@ReturnProLicenseCommandTest.fileSystemService
            override val environmentVariables = mapOf<String, String>()
            override val workingDirectory = workingDirectoryPath

            override fun resolvePath(path: String) = path
        },
    )
}
