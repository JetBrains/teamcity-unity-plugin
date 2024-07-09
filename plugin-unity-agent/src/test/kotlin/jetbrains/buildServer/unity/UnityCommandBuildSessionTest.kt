package jetbrains.buildServer.unity

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.unity.UnityConstants.PARAM_TEST_PLATFORM
import jetbrains.buildServer.unity.detectors.DetectVirtualUnityEnvironmentCommand
import jetbrains.buildServer.unity.license.UnityBuildStepScopeLicenseActivator
import jetbrains.buildServer.unity.license.commands.ActivateProLicenseCommand
import jetbrains.buildServer.unity.license.commands.ReturnProLicenseCommand
import jetbrains.buildServer.unity.util.FileSystemService
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class UnityCommandBuildSessionTest {

    private val unityBuildRunnerContext = mockk<UnityBuildRunnerContext>()
    private val fileSystemServiceMock = mockk<FileSystemService>()
    private val envProviderMock = mockk<UnityEnvironmentProvider>()
    private val licenceActivatorMock = mockk<UnityBuildStepScopeLicenseActivator>()

    private val unityEnvironmentMock = mockk<UnityEnvironment>()

    @BeforeMethod
    fun setUp() {
        clearAllMocks()

        every { envProviderMock.unityEnvironment() } returns unityEnvironmentMock
        every { unityBuildRunnerContext.workingDirectory } returns mockk(relaxed = true)
        every { unityBuildRunnerContext.unityProjectPath } returns "foo/bar"
        every { unityBuildRunnerContext.unityProject } returns mockk(relaxed = true)
    }

    @Test
    fun `should detect virtual unity environment as the 1st command`() {
        // arrange
        val session = startedSession()
        val detectCommand = givenDetectVirtualUnityEnvironmentCommand()

        // act
        val command = getNthCommand(1, session)

        // assert
        command shouldNotBe null
        command!! shouldBeEqual detectCommand

        verify(exactly = 1) { envProviderMock.provide(unityBuildRunnerContext) }
        verify(exactly = 0) { envProviderMock.unityEnvironment() }
        verify { licenceActivatorMock wasNot Called }
    }

    @Test
    fun `should activate license as the 1st command`() {
        // arrange
        val session = startedSession()
        givenNoDetectVirtualUnityEnvironmentCommand()
        val activateLicenseCommand = givenActivateLicenseCommand()

        // act
        val command = getNthCommand(1, session)

        // assert
        command shouldNotBe null
        command!! shouldBeEqual activateLicenseCommand

        verify(exactly = 1) { envProviderMock.provide(unityBuildRunnerContext) }
        verify(exactly = 1) { envProviderMock.unityEnvironment() }
        verify(exactly = 1) {
            licenceActivatorMock.activateLicense(
                envProviderMock.unityEnvironment(),
                unityBuildRunnerContext,
            )
        }

        verify(exactly = 0) { licenceActivatorMock.returnLicense(any(), any()) }
    }

    @Test
    fun `should activate license as the 2nd command`() {
        // arrange
        val session = startedSession()
        givenDetectVirtualUnityEnvironmentCommand()
        val activateLicenseCommand = givenActivateLicenseCommand()

        // act
        val command = getNthCommand(2, session)

        // assert
        command shouldNotBe null
        command!! shouldBeEqual activateLicenseCommand

        verify(exactly = 1) { envProviderMock.provide(unityBuildRunnerContext) }
        verify(exactly = 1) { envProviderMock.unityEnvironment() }
        verify(exactly = 1) {
            licenceActivatorMock.activateLicense(
                envProviderMock.unityEnvironment(),
                unityBuildRunnerContext,
            )
        }
        verify(exactly = 0) { licenceActivatorMock.returnLicense(any(), any()) }
    }

    @Test
    fun `should execute build as the 1st command`() {
        // arrange
        val session = startedSession()
        every { unityBuildRunnerContext.runnerParameters } returns emptyMap()
        every { unityBuildRunnerContext.build } returns mockk(relaxed = true)

        givenNoDetectVirtualUnityEnvironmentCommand()
        givenNoLicenseCommands()

        // act
        val command = getNthCommand(1, session)

        // assert
        command shouldNotBe null
        command?.shouldBeInstanceOf<BuildCommandExecutionAdapter>()

        verify { envProviderMock.provide(unityBuildRunnerContext) }
        verify { envProviderMock.unityEnvironment() }
        verify { licenceActivatorMock.activateLicense(envProviderMock.unityEnvironment(), unityBuildRunnerContext) }
        verify(exactly = 0) { licenceActivatorMock.returnLicense(any(), any()) }
    }

    @Test
    fun `should execute build as the 2nd command`() {
        // arrange
        val session = startedSession()
        every { unityBuildRunnerContext.runnerParameters } returns emptyMap()
        every { unityBuildRunnerContext.build } returns mockk(relaxed = true)

        givenNoDetectVirtualUnityEnvironmentCommand()
        givenActivateLicenseCommand()

        // act
        val command = getNthCommand(2, session)

        // assert
        command shouldNotBe null
        command?.shouldBeInstanceOf<BuildCommandExecutionAdapter>()

        verify { envProviderMock.provide(unityBuildRunnerContext) }
        verify { envProviderMock.unityEnvironment() }
        verify { licenceActivatorMock.activateLicense(envProviderMock.unityEnvironment(), unityBuildRunnerContext) }
        verify(exactly = 0) { licenceActivatorMock.returnLicense(any(), any()) }
    }

    @Test
    fun `should execute build as the 3d command`() {
        // arrange
        val session = startedSession()
        every { unityBuildRunnerContext.runnerParameters } returns emptyMap()
        every { unityBuildRunnerContext.build } returns mockk(relaxed = true)

        givenDetectVirtualUnityEnvironmentCommand()
        givenActivateLicenseCommand()

        // act
        val command = getNthCommand(3, session)

        // assert
        command shouldNotBe null
        command?.shouldBeInstanceOf<BuildCommandExecutionAdapter>()

        verify { envProviderMock.provide(unityBuildRunnerContext) }
        verify { envProviderMock.unityEnvironment() }
        verify { licenceActivatorMock.activateLicense(envProviderMock.unityEnvironment(), unityBuildRunnerContext) }
        verify(exactly = 0) { licenceActivatorMock.returnLicense(any(), any()) }
    }

    @Test
    fun `should execute build as the 1st and the 2nd commands when all test platforms specified`() {
        // arrange
        val session = startedSession()
        every { unityBuildRunnerContext.runnerParameters } returns mapOf(PARAM_TEST_PLATFORM to "all")
        every { unityBuildRunnerContext.build } returns mockk(relaxed = true)

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

        verify { envProviderMock.provide(unityBuildRunnerContext) }
        verify { envProviderMock.unityEnvironment() }
        verify { licenceActivatorMock.activateLicense(envProviderMock.unityEnvironment(), unityBuildRunnerContext) }
        verify(exactly = 0) { licenceActivatorMock.returnLicense(any(), any()) }
    }

    @Test
    fun `should execute build as the 3d and the 4th commands when all test platforms specified`() {
        // arrange
        val session = startedSession()
        every { unityBuildRunnerContext.runnerParameters } returns mapOf(PARAM_TEST_PLATFORM to "all")
        every { unityBuildRunnerContext.build } returns mockk(relaxed = true)

        givenDetectVirtualUnityEnvironmentCommand()
        givenActivateLicenseCommand()

        // act
        val thirdCommand = getNthCommand(3, session)
        val fourthCommand = session.nextCommand

        // assert
        thirdCommand shouldNotBe null
        thirdCommand?.shouldBeInstanceOf<BuildCommandExecutionAdapter>()
        fourthCommand shouldNotBe null
        fourthCommand?.shouldBeInstanceOf<BuildCommandExecutionAdapter>()

        verify { envProviderMock.provide(unityBuildRunnerContext) }
        verify { envProviderMock.unityEnvironment() }
        verify { licenceActivatorMock.activateLicense(envProviderMock.unityEnvironment(), unityBuildRunnerContext) }
        verify(exactly = 0) { licenceActivatorMock.returnLicense(any(), any()) }
    }

    @Test
    fun `should return unity license as the last command`() {
        // arrange
        val session = startedSession()
        every { unityBuildRunnerContext.runnerParameters } returns mapOf(PARAM_TEST_PLATFORM to "all")
        every { unityBuildRunnerContext.build } returns mockk(relaxed = true)
        givenDetectVirtualUnityEnvironmentCommand()
        givenActivateLicenseCommand()
        val returnLicenseCommand = givenReturnLicenseCommand()

        // act
        val command = getNthCommand(5, session)

        // assert
        command shouldNotBe null
        command!! shouldBeEqual returnLicenseCommand

        verify { envProviderMock.provide(unityBuildRunnerContext) }
        verify { envProviderMock.unityEnvironment() }
        verify { licenceActivatorMock.activateLicense(envProviderMock.unityEnvironment(), unityBuildRunnerContext) }
        verify { licenceActivatorMock.returnLicense(envProviderMock.unityEnvironment(), unityBuildRunnerContext) }
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
        every { envProviderMock.provide(unityBuildRunnerContext) } returns sequenceOf(command)
        return command
    }

    private fun givenNoDetectVirtualUnityEnvironmentCommand() {
        every { envProviderMock.provide(unityBuildRunnerContext) } returns emptySequence()
    }

    private fun givenActivateLicenseCommand(): ActivateProLicenseCommand {
        val command = mockk<ActivateProLicenseCommand>()
        every {
            licenceActivatorMock.activateLicense(envProviderMock.unityEnvironment(), unityBuildRunnerContext)
        } returns sequenceOf(command)
        return command
    }

    private fun givenReturnLicenseCommand(): ReturnProLicenseCommand {
        val command = mockk<ReturnProLicenseCommand>()
        every {
            licenceActivatorMock.returnLicense(envProviderMock.unityEnvironment(), unityBuildRunnerContext)
        } returns sequenceOf(command)
        return command
    }

    private fun givenNoLicenseCommands() {
        every {
            licenceActivatorMock.activateLicense(envProviderMock.unityEnvironment(), unityBuildRunnerContext)
        } returns emptySequence()
        every {
            licenceActivatorMock.returnLicense(envProviderMock.unityEnvironment(), unityBuildRunnerContext)
        } returns emptySequence()
    }

    private fun getNthCommand(n: Int, session: UnityCommandBuildSession): CommandExecution? {
        var command: CommandExecution? = null
        repeat(n) {
            command = session.nextCommand
        }
        return command
    }

    private fun createInstance() = UnityCommandBuildSession(
        unityBuildRunnerContext,
        fileSystemServiceMock,
        envProviderMock,
        licenceActivatorMock,
    )
}
