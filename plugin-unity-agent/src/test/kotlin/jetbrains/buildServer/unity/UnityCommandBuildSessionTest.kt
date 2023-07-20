package jetbrains.buildServer.unity

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.unity.UnityConstants.PARAM_TEST_PLATFORM
import jetbrains.buildServer.unity.detectors.DetectVirtualUnityEnvironmentCommand
import jetbrains.buildServer.unity.license.ActivateUnityLicenseCommand
import jetbrains.buildServer.unity.license.ReturnUnityLicenseCommand
import jetbrains.buildServer.unity.license.UnityProfessionalLicenceManager
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class UnityCommandBuildSessionTest {

    private val runnerContext = mockk<BuildRunnerContext>()
    private val envProvider = mockk<UnityEnvironmentProvider>()
    private val proLicenceManager = mockk<UnityProfessionalLicenceManager>()

    private val unityEnvironment = mockk<UnityEnvironment>()

    @BeforeMethod
    fun setUp() {
        clearAllMocks()

        every { envProvider.unityEnvironment() } returns unityEnvironment
    }

    @Test
    fun `should return DetectVirtualUnityEnvironmentCommand as the 1st command`() {
        // given
        val session = startedSession()
        val detectCommand = givenDetectVirtualUnityEnvironmentCommand()

        // when
        val command = getNthCommand(1, session)

        // then
        command shouldNotBe null
        command!! shouldBeEqual detectCommand

        verify(exactly = 1) { envProvider.provide(runnerContext) }
        verify { envProvider.unityEnvironment() wasNot Called }
        verify { proLicenceManager wasNot Called }
    }

    @Test
    fun `should return ActivateUnityLicenseCommand as the 1st command`() {
        // given
        val session = startedSession()
        givenNoDetectVirtualUnityEnvironmentCommand()
        val activateUnityLicenseCommand = givenActivateUnityLicenseCommand()

        // when
        val command = getNthCommand(1, session)

        // then
        command shouldNotBe null
        command!! shouldBeEqual activateUnityLicenseCommand

        verify(exactly = 1) { envProvider.provide(runnerContext) }
        verify(exactly = 1) { envProvider.unityEnvironment() }
        verify(exactly = 1) { proLicenceManager.activateLicence(envProvider.unityEnvironment(), runnerContext) }
        verify { proLicenceManager.returnLicence(any(), any()) wasNot Called }
    }

    @Test
    fun `should return ActivateUnityLicenseCommand as the 2nd command`() {
        // given
        val session = startedSession()
        givenDetectVirtualUnityEnvironmentCommand()
        val activateUnityLicenseCommand = givenActivateUnityLicenseCommand()

        // when
        val command = getNthCommand(2, session)

        // then
        command shouldNotBe null
        command!! shouldBeEqual activateUnityLicenseCommand

        verify(exactly = 1) { envProvider.provide(runnerContext) }
        verify(exactly = 1) { envProvider.unityEnvironment() }
        verify(exactly = 1) { proLicenceManager.activateLicence(envProvider.unityEnvironment(), runnerContext) }
        verify { proLicenceManager.returnLicence(any(), any()) wasNot Called }
    }

    @Test
    fun `should return BuildCommandExecutionAdapter as the 1st command`() {
        // given
        val session = startedSession()
        every { runnerContext.runnerParameters } returns emptyMap()
        every { runnerContext.build } returns mockk(relaxed = true)

        givenNoDetectVirtualUnityEnvironmentCommand()
        givenNoProfessionalLicenseCommands()

        // when
        val command = getNthCommand(1, session)

        // then
        command shouldNotBe null
        command?.shouldBeInstanceOf<BuildCommandExecutionAdapter>()

        verify { envProvider.provide(runnerContext) }
        verify { envProvider.unityEnvironment() }
        verify { proLicenceManager.activateLicence(envProvider.unityEnvironment(), runnerContext) }
        verify { proLicenceManager.returnLicence(any(), any()) wasNot Called }
    }

    @Test
    fun `should return BuildCommandExecutionAdapter as the 2nd command`() {
        // given
        val session = startedSession()
        every { runnerContext.runnerParameters } returns emptyMap()
        every { runnerContext.build } returns mockk(relaxed = true)

        givenNoDetectVirtualUnityEnvironmentCommand()
        givenActivateUnityLicenseCommand()

        // when
        val command = getNthCommand(2, session)

        // then
        command shouldNotBe null
        command?.shouldBeInstanceOf<BuildCommandExecutionAdapter>()

        verify { envProvider.provide(runnerContext) }
        verify { envProvider.unityEnvironment() }
        verify { proLicenceManager.activateLicence(envProvider.unityEnvironment(), runnerContext) }
        verify { proLicenceManager.returnLicence(any(), any()) wasNot Called }
    }

    @Test
    fun `should return BuildCommandExecutionAdapter as the 3d command`() {
        // given
        val session = startedSession()
        every { runnerContext.runnerParameters } returns emptyMap()
        every { runnerContext.build } returns mockk(relaxed = true)

        givenDetectVirtualUnityEnvironmentCommand()
        givenActivateUnityLicenseCommand()

        // when
        val command = getNthCommand(3, session)

        // then
        command shouldNotBe null
        command?.shouldBeInstanceOf<BuildCommandExecutionAdapter>()

        verify { envProvider.provide(runnerContext) }
        verify { envProvider.unityEnvironment() }
        verify { proLicenceManager.activateLicence(envProvider.unityEnvironment(), runnerContext) }
        verify { proLicenceManager.returnLicence(any(), any()) wasNot Called }
    }

    @Test
    fun `should return BuildCommandExecutionAdapter as the 1st and the 2nd commands when all test platforms specified`() {
        // given
        val session = startedSession()
        every { runnerContext.runnerParameters } returns mapOf(PARAM_TEST_PLATFORM to "all")
        every { runnerContext.build } returns mockk(relaxed = true)

        givenNoDetectVirtualUnityEnvironmentCommand()
        givenNoProfessionalLicenseCommands()

        // when
        val firstCommand = session.nextCommand
        val secondCommand = session.nextCommand

        // then
        firstCommand shouldNotBe null
        firstCommand?.shouldBeInstanceOf<BuildCommandExecutionAdapter>()
        secondCommand shouldNotBe null
        secondCommand?.shouldBeInstanceOf<BuildCommandExecutionAdapter>()

        verify { envProvider.provide(runnerContext) }
        verify { envProvider.unityEnvironment() }
        verify { proLicenceManager.activateLicence(envProvider.unityEnvironment(), runnerContext) }
        verify { proLicenceManager.returnLicence(any(), any()) wasNot Called }
    }

    @Test
    fun `should return BuildCommandExecutionAdapter as the 3d and the 4th commands when all test platforms specified`() {
        // given
        val session = startedSession()
        every { runnerContext.runnerParameters } returns mapOf(PARAM_TEST_PLATFORM to "all")
        every { runnerContext.build } returns mockk(relaxed = true)

        givenDetectVirtualUnityEnvironmentCommand()
        givenActivateUnityLicenseCommand()

        // when
        val thirdCommand = getNthCommand(3, session)
        val fourthCommand = session.nextCommand

        // then
        thirdCommand shouldNotBe null
        thirdCommand?.shouldBeInstanceOf<BuildCommandExecutionAdapter>()
        fourthCommand shouldNotBe null
        fourthCommand?.shouldBeInstanceOf<BuildCommandExecutionAdapter>()

        verify { envProvider.provide(runnerContext) }
        verify { envProvider.unityEnvironment() }
        verify { proLicenceManager.activateLicence(envProvider.unityEnvironment(), runnerContext) }
        verify { proLicenceManager.returnLicence(any(), any()) wasNot Called }
    }

    @Test
    fun `should return ReturnUnityLicenseCommand as the last command`() {
        // given
        val session = startedSession()
        every { runnerContext.runnerParameters } returns mapOf(PARAM_TEST_PLATFORM to "all")
        every { runnerContext.build } returns mockk(relaxed = true)
        givenDetectVirtualUnityEnvironmentCommand()
        givenActivateUnityLicenseCommand()
        val returnUnityLicenseCommand = givenReturnUnityLicenseCommand()

        // when
        val command = getNthCommand(5, session)

        // then
        command shouldNotBe null
        command!! shouldBeEqual returnUnityLicenseCommand

        verify { envProvider.provide(runnerContext) }
        verify { envProvider.unityEnvironment() }
        verify { proLicenceManager.activateLicence(envProvider.unityEnvironment(), runnerContext) }
        verify { proLicenceManager.returnLicence(envProvider.unityEnvironment(), runnerContext) }
    }

    @Test
    fun `should do nothing on nextCommand if sessionStarted was not called (should not happen)`() {
        // given
        val session = createInstance()

        // when
        val nextCommand = session.nextCommand

        // then
        nextCommand shouldBe null

        verify { envProvider wasNot Called }
        verify { proLicenceManager wasNot Called }
    }

    private fun startedSession(): UnityCommandBuildSession {
        val session = createInstance()
        session.sessionStarted()
        return session
    }

    private fun givenDetectVirtualUnityEnvironmentCommand(): DetectVirtualUnityEnvironmentCommand {
        val command = mockk<DetectVirtualUnityEnvironmentCommand>()
        every { envProvider.provide(runnerContext) } returns sequenceOf(command)
        return command
    }

    private fun givenNoDetectVirtualUnityEnvironmentCommand() {
        every { envProvider.provide(runnerContext) } returns emptySequence()
    }

    private fun givenActivateUnityLicenseCommand(): ActivateUnityLicenseCommand {
        val command = mockk<ActivateUnityLicenseCommand>()
        every {
            proLicenceManager.activateLicence(envProvider.unityEnvironment(), runnerContext)
        } returns sequenceOf(command)
        return command
    }

    private fun givenReturnUnityLicenseCommand(): ReturnUnityLicenseCommand {
        val command = mockk<ReturnUnityLicenseCommand>()
        every {
            proLicenceManager.returnLicence(envProvider.unityEnvironment(), runnerContext)
        } returns sequenceOf(command)
        return command
    }

    private fun givenNoProfessionalLicenseCommands() {
        every {
            proLicenceManager.activateLicence(envProvider.unityEnvironment(), runnerContext)
        } returns emptySequence()
        every {
            proLicenceManager.returnLicence(envProvider.unityEnvironment(), runnerContext)
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
        runnerContext,
        envProvider,
        proLicenceManager,
    )
}