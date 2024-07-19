package jetbrains.buildServer.unity

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.agent.runner.TerminationAction
import jetbrains.buildServer.unity.UnityConstants.PARAM_TEST_PLATFORM
import jetbrains.buildServer.unity.detectors.DetectVirtualUnityEnvironmentCommand
import jetbrains.buildServer.unity.license.UnityBuildStepScopeLicenseActivator
import jetbrains.buildServer.unity.util.FileSystemService
import org.testng.annotations.BeforeMethod
import java.io.File
import kotlin.test.Test

class UnityCommandBuildSessionTest {

    private val unityBuildRunnerContextMock = mockk<UnityBuildRunnerContext>()
    private val fileSystemServiceMock = mockk<FileSystemService>()
    private val envProviderMock = mockk<UnityEnvironmentProvider>()
    private val licenceActivatorMock = mockk<UnityBuildStepScopeLicenseActivator>()
    private val unityEnvironmentMock = mockk<UnityEnvironment>()

    @BeforeMethod
    fun setUp() {
        clearAllMocks()

        every { envProviderMock.unityEnvironment() } returns unityEnvironmentMock
        with(unityBuildRunnerContextMock) {
            every { runnerParameters } returns emptyMap()
            every { workingDirectory } returns mockk(relaxed = true)
            every { unityProjectPath } returns "foo/bar"
            every { unityProject } returns mockk(relaxed = true)
            every { build } returns mockk(relaxed = true)
        }
    }

    @Test
    fun `should detect virtual unity environment as the 1st command`() {
        // arrange
        val session = startedSession()
        val detectCommand = givenDetectVirtualUnityEnvironmentCommand()

        // act
        val command = session.toCommandSequence().iterator().next()

        // assert
        command shouldNotBe null
        command.shouldBeEqual(detectCommand)

        verify(exactly = 1) { envProviderMock.provide(unityBuildRunnerContextMock) }
        verify(exactly = 0) { envProviderMock.unityEnvironment() }
        verify { licenceActivatorMock wasNot Called }
    }

    @Test
    fun `should return whatever commands produced by license activator`() {
        // arrange
        val session = startedSession()
        givenNoDetectVirtualUnityEnvironmentCommand()

        val activateLicenseCommandStub = commandExecutionStub()
        val returnLicenseCommandStub = commandExecutionStub()
        every {
            licenceActivatorMock.withLicense(envProviderMock.unityEnvironment(), any())
        } answers {
            sequence {
                yield(activateLicenseCommandStub)
                yield(returnLicenseCommandStub)
            }
        }

        // act
        val commands = session.toCommandSequence().toList()

        // assert
        commands shouldContainExactly listOf(activateLicenseCommandStub, returnLicenseCommandStub)
    }

    @Test
    fun `should use exactly one command for the actual build`() {
        // arrange
        val session = startedSession()
        givenNoDetectVirtualUnityEnvironmentCommand()
        givenNoLicenseCommands()

        // act
        val commands = session.toCommandSequence()

        // assert
        val buildCommand = commands.singleOrNull()
        buildCommand shouldNotBe null
        buildCommand.shouldBeInstanceOf<BuildCommandExecutionAdapter>()
    }

    @Test
    fun `should use exactly 2 commands for the actual build when all test platforms specified`() {
        // arrange
        val session = startedSession()
        every { unityBuildRunnerContextMock.runnerParameters } returns mapOf(PARAM_TEST_PLATFORM to "all")
        every { unityBuildRunnerContextMock.build } returns mockk(relaxed = true)

        givenNoDetectVirtualUnityEnvironmentCommand()
        givenNoLicenseCommands()

        // act
        val firstCommand = session.nextCommand
        val secondCommand = session.nextCommand

        // assert
        firstCommand shouldNotBe null
        firstCommand?.shouldBeInstanceOf<BuildCommandExecutionAdapter>()
        secondCommand shouldNotBe null
        secondCommand?.shouldBeInstanceOf<BuildCommandExecutionAdapter>()

        verify { envProviderMock.provide(unityBuildRunnerContextMock) }
        verify { envProviderMock.unityEnvironment() }
        verify { licenceActivatorMock.withLicense(envProviderMock.unityEnvironment(), any()) }
    }

    @Test
    fun `should do nothing on nextCommand if sessionStarted was not called (should not happen)`() {
        // arrange
        val session = createInstance()

        // act
        val nextCommand = session.nextCommand

        // assert
        nextCommand shouldBe null

        verify { envProviderMock wasNot Called }
        verify { licenceActivatorMock wasNot Called }
    }

    private fun startedSession(): UnityCommandBuildSession {
        val session = createInstance()
        session.sessionStarted()
        return session
    }

    private fun givenDetectVirtualUnityEnvironmentCommand(): DetectVirtualUnityEnvironmentCommand {
        val command = mockk<DetectVirtualUnityEnvironmentCommand>()
        every { envProviderMock.provide(unityBuildRunnerContextMock) } returns sequenceOf(command)
        return command
    }

    private fun givenNoDetectVirtualUnityEnvironmentCommand() {
        every { envProviderMock.provide(unityBuildRunnerContextMock) } returns emptySequence()
    }

    private fun givenNoLicenseCommands() {
        val passedCommands = slot<() -> Sequence<CommandExecution>>()
        every {
            licenceActivatorMock.withLicense(envProviderMock.unityEnvironment(), capture(passedCommands))
        } answers {
            passedCommands.captured()
        }
    }

    private fun MultiCommandBuildSession.toCommandSequence(): Sequence<CommandExecution> = sequence {
        var command: CommandExecution?

        do {
            command = nextCommand
            if (command != null) {
                yield(command)
            }
        } while (command != null)
    }

    private fun createInstance() = UnityCommandBuildSession(
        unityBuildRunnerContextMock,
        fileSystemServiceMock,
        envProviderMock,
        licenceActivatorMock,
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
