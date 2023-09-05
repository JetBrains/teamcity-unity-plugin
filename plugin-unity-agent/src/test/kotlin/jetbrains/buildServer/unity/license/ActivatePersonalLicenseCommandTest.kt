package jetbrains.buildServer.unity.license

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.*
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.messages.BlockData
import jetbrains.buildServer.unity.UnityEnvironment
import jetbrains.buildServer.unity.UnityVersion
import jetbrains.buildServer.unity.util.FileSystemService
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class ActivatePersonalLicenseCommandTest {

    private val runnerContext = mockk<BuildRunnerContext>()
    private val virtualContext = mockk<VirtualContext>()
    private val build = mockk<AgentRunningBuild>()
    private val buildLogger = mockk<BuildProgressLogger>()
    private val buildParameters = mockk<BuildParametersMap>()
    private val buildFeature = mockk<AgentBuildFeature>()

    private val fileSystemService = mockk<FileSystemService>()
    private val workingDirectory = mockk<File>()
    private val workingDirectoryPath = "/path/to/workingDir"
    private val agentTempDirectory = mockk<File>()
    private val buildTempDirectory = mockk<File>()

    @BeforeMethod
    fun setUp() {
        clearAllMocks()

        every { runnerContext.virtualContext } returns virtualContext
        every { runnerContext.build } returns build
        every { runnerContext.buildParameters } returns buildParameters
        every { runnerContext.workingDirectory } returns workingDirectory

        every { virtualContext.resolvePath(any()) } returnsArgument 0
        every { buildFeature.parameters } returns mapOf()
        every { buildParameters.environmentVariables } returns mapOf()
        every { workingDirectory.path } returns workingDirectoryPath

        every { build.getBuildFeaturesOfType(any()) } returns listOf(buildFeature)
        every { build.buildLogger } returns buildLogger
        every { build.agentTempDirectory } returns agentTempDirectory
        every { build.buildTempDirectory } returns buildTempDirectory
        every { build.buildId } returns 1
    }

    @Test
    fun `should make program command line`() {
        // arrange
        val command = createInstance()
        val licenseFilePath = "/path/to/license/file.ulf"
        val logFilePath = "/path/to/logs.txt"
        val unityEnvironment = anUnityEnvironment()
        command.withUnityEnvironment(unityEnvironment)

        mockFiles(licenseFilePath, logFilePath)

        every { buildFeature.parameters } returns mapOf(
            "secure:unityPersonalLicenseContent" to "personalLicenseContent",
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
                "-quit", "-batchmode", "-nographics",
                "-manualLicenseFile", licenseFilePath,
                "-logFile", logFilePath,
            )
        )
    }

    @Test
    fun `should fail when license content is not provided`() {
        // arrange
        val command = createInstance()
        val licenseFilePath = "/path/to/license/file.ulf"
        val logFilePath = "/path/to/logs.txt"
        val unityEnvironment = anUnityEnvironment()
        command.withUnityEnvironment(unityEnvironment)

        mockFiles(licenseFilePath, logFilePath)

        every { buildFeature.parameters } returns emptyMap()
        command.beforeProcessStarted()

        // act // assert
        shouldThrow<IllegalStateException> {
            command.makeProgramCommandLine()
        }.apply {
            message shouldBe "Personal license content is not provided"
        }
    }

    @Test
    fun `should open a log block when process is started`() {
        // arrange
        val commandLine = aCommandLine()
        val command = createInstance()

        every { buildLogger.logMessage(any()) } returns Unit
        every { buildLogger.message(any()) } returns Unit

        // act
        command.processStarted(commandLine, workingDirectory)

        // assert
        verify(exactly = 1) {
            buildLogger.logMessage(withArg {
                it.value shouldBe BlockData("Activate Unity license", "unity")
                it.typeId shouldBe "BlockStart"
            })
        }
        verify(exactly = 1) {
            buildLogger.message("Starting: $commandLine")
        }
    }

    @Suppress("SameParameterValue")
    private fun mockFiles(licenseFilePath: String, logFilePath: String) {
        val licenseFile = mockk<Path>()
        val logFile = mockk<Path>()
        every {
            fileSystemService.createTempFile(buildTempDirectory.toPath(), "unity-personal-license-", ".ulf")
        } returns licenseFile
        every {
            fileSystemService.writeText(licenseFile, "personalLicenseContent")
        } returns Unit
        every {
            fileSystemService.createTempFile(agentTempDirectory.toPath(), "activate-license-log-", "-1.txt")
        } returns logFile
        every { licenseFile.absolutePathString() } returns licenseFilePath
        every { logFile.absolutePathString() } returns logFilePath
    }

    private fun anUnityEnvironment(): UnityEnvironment {
        val unityPath = "/path/to/unity"
        val unityVersion = UnityVersion(2023, 1, 1)
        return UnityEnvironment(unityPath, unityVersion)
    }

    private fun aCommandLine(): String {
        return """
            /path/to/unity -quit -batchmode -nographics 
            -manualLicenseFile /path/to/license/file.ulf
            -logFile /path/to/logs.txt"
            """.trimIndent()
    }

    private fun createInstance() = ActivatePersonalLicenseCommand(runnerContext, fileSystemService)
}