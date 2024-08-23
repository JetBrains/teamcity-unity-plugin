package jetbrains.buildServer.unity.license

import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.sequences.shouldContainExactly
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import jetbrains.buildServer.ExecResult
import jetbrains.buildServer.agent.AgentBuildFeature
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildParametersMap
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.agent.runner.TerminationAction
import jetbrains.buildServer.unity.UnityEnvironment
import jetbrains.buildServer.unity.UnityVersion
import jetbrains.buildServer.unity.license.commands.ActivatePersonalLicenseCommand
import jetbrains.buildServer.unity.license.commands.ActivateProLicenseCommand
import jetbrains.buildServer.unity.license.commands.ReturnProLicenseCommand
import jetbrains.buildServer.unity.util.CommandLineRunner
import jetbrains.buildServer.unity.util.FileSystemService
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class UnityBuildStepScopeLicenseActivatorTest {
    private val fileSystemService = mockk<FileSystemService>()
    private val commandLineRunner = mockk<CommandLineRunner>()
    private val runnerContext = mockk<BuildRunnerContext>()
    private val build = mockk<AgentRunningBuild>()
    private val buildFeature = mockk<AgentBuildFeature>()

    @BeforeMethod
    fun setUp() {
        clearAllMocks()

        every { buildFeature.parameters } returns emptyMap()

        with(runnerContext) {
            every { build } returns this@UnityBuildStepScopeLicenseActivatorTest.build
            every { buildParameters } returns object : BuildParametersMap {
                override fun getEnvironmentVariables() = mapOf<String, String>()
                override fun getSystemProperties() = mapOf<String, String>()
                override fun getAllParameters() = mapOf<String, String>()
            }
            every { workingDirectory } returns mockk(relaxed = true)
            every { virtualContext } returns mockk(relaxed = true)
        }
        with(build) {
            every { getBuildFeaturesOfType(any()) } returns listOf(buildFeature)
            every { buildLogger } returns mockk(relaxed = true)
        }
    }

    @DataProvider
    fun `should issue professional license activation & return commands`(): Array<Array<Any>> = arrayOf(
        arrayOf(mapOf("unityLicenseType" to "professionalLicense")),
        arrayOf(mapOf("activateLicense" to "true")),
    )

    @Test(dataProvider = "should issue professional license activation & return commands")
    fun `should issue professional license activation & return commands`(params: Map<String, String>) {
        // arrange
        every { buildFeature.parameters } returns params
        val buildCommand = commandExecutionStub()

        // act
        val commands = act(sequenceOf(buildCommand))

        // assert
        commands shouldHaveSize 3

        val activateLicense = commands.first()
        activateLicense shouldNotBe null
        activateLicense.shouldBeInstanceOf<ActivateProLicenseCommand>()

        val returnLicense = commands.last()
        returnLicense shouldNotBe null
        returnLicense.shouldBeInstanceOf<ReturnProLicenseCommand>()
    }

    @Test
    fun `should issue personal license activation command`() {
        // arrange
        every { buildFeature.parameters } returns mapOf("unityLicenseType" to "personalLicense")

        // act
        val commands = act()

        // assert
        commands.shouldBeSingleton()
        commands.first().shouldBeInstanceOf<ActivatePersonalLicenseCommand>()
    }

    @DataProvider
    fun `does not modify the original commands when activation isn't enabled`(): Array<Array<Any>> = arrayOf(
        arrayOf(emptyMap<String, String>()),
        arrayOf(mapOf("unityLicenseType" to "invalid license type")),
        arrayOf(mapOf("activateLicense" to "false")),
        arrayOf(mapOf("activateLicense" to "invalid boolean")),
        arrayOf(mapOf("notRelevantParam" to "some value")),
        arrayOf(
            mapOf(
                "activateLicense" to "true",
                "unityLicenseScope" to "buildConfiguration",
            ),
        ),
    )

    @Test(dataProvider = "does not modify the original commands when activation isn't enabled")
    fun `does not modify the original commands when activation isn't enabled`(params: Map<String, String>) {
        // arrange
        every { buildFeature.parameters } returns params

        val buildCommands = sequenceOf(commandExecutionStub())

        // act
        val commands = act(buildCommands)

        // assert
        commands.shouldContainExactly(buildCommands.toList())
    }

    @Test
    fun `should return pro license on interruption of the original command if it was activated`() {
        // arrange
        every { buildFeature.parameters } returns mapOf("unityLicenseType" to "professionalLicense")

        var originalCommandHookWasCalled = false
        val buildCommand = object : CommandExecution by commandExecutionStub() {
            override fun interruptRequested(): TerminationAction {
                originalCommandHookWasCalled = true
                return TerminationAction.KILL_PROCESS_TREE
            }
        }

        val runnerExecutedCommandSlot = slot<CommandExecution>()
        mockkStatic(CommandLineRunner::execute)
        every { commandLineRunner.run(any()) } returns ExecResult()
        every { commandLineRunner.execute(capture(runnerExecutedCommandSlot), any()) } just Runs

        // act
        val commands = act(sequenceOf(buildCommand))
        commands.firstOrNull()?.processFinished(0)
        commands.getOrNull(1)?.interruptRequested()

        // assert
        assertTrue(originalCommandHookWasCalled)
        val command = runnerExecutedCommandSlot.captured
        command shouldNotBe null
        command.shouldBeInstanceOf<ReturnProLicenseCommand>()
    }

    private fun act(buildCommands: Sequence<CommandExecution> = emptySequence()) =
        createInstance().withLicense(anUnityEnvironment()) { buildCommands }.toList()

    private fun anUnityEnvironment(): UnityEnvironment {
        val unityPath = "/path/to/unity"
        val unityVersion = UnityVersion(2023, 1, 1)
        return UnityEnvironment(unityPath, unityVersion)
    }

    private fun createInstance() = UnityBuildStepScopeLicenseActivator(
        fileSystemService,
        runnerContext,
        commandLineRunner,
    )

    private fun commandExecutionStub() = object : CommandExecution {
        override fun onStandardOutput(p0: String) {}
        override fun onErrorOutput(p0: String) {}
        override fun processStarted(p0: String, p1: File) {}
        override fun processFinished(p0: Int) {}
        override fun makeProgramCommandLine() = mockk<ProgramCommandLine>(relaxed = true)
        override fun beforeProcessStarted() {}
        override fun interruptRequested() = TerminationAction.KILL_PROCESS_TREE
        override fun isCommandLineLoggingEnabled() = true
    }
}
