package jetbrains.buildServer.unity.license

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.mockk.*
import jetbrains.buildServer.agent.AgentBuildFeature
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.unity.UnityEnvironment
import jetbrains.buildServer.unity.UnityVersion
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class UnityProfessionalLicenceManagerTest {

    private val activateCommand = mockk<ActivateUnityLicenseCommand>()
    private val returnCommand = mockk<ReturnUnityLicenseCommand>()

    private val runnerContext = mockk<BuildRunnerContext>()
    private val build = mockk<AgentRunningBuild>()
    private val buildFeature = mockk<AgentBuildFeature>()

    @BeforeMethod
    fun setUp() {
        clearAllMocks()

        every { runnerContext.build } returns build
        every { build.getBuildFeaturesOfType(any()) } returns listOf(buildFeature)
    }

    @Test
    fun `should activate licence`() {
        // given
        val manager = createInstance()
        val unityEnvironment = anUnityEnvironment()

        every { buildFeature.parameters } returns mapOf("activateLicense" to "true")
        every { activateCommand.withUnityEnvironment(unityEnvironment) } returns Unit

        // when
        val commands = manager.activateLicence(unityEnvironment, runnerContext).toList()

        // then
        commands.shouldNotBeEmpty()
        commands.shouldContainExactly(activateCommand)

        verify(exactly = 1) { activateCommand.withUnityEnvironment(unityEnvironment) }
        verify { returnCommand wasNot Called }
    }

    @Test
    fun `should return licence`() {
        // given
        val manager = createInstance()
        val unityEnvironment = anUnityEnvironment()

        every { buildFeature.parameters } returns mapOf("activateLicense" to "true")
        every { returnCommand.withUnityEnvironment(unityEnvironment) } returns Unit

        // when
        val commands = manager.returnLicence(unityEnvironment, runnerContext).toList()

        // then
        commands.shouldNotBeEmpty()
        commands.shouldContainExactly(returnCommand)

        verify(exactly = 1) { returnCommand.withUnityEnvironment(unityEnvironment) }
        verify { activateCommand wasNot Called }
    }

    @Test
    fun `should not activate licence when the parameters of build feature are empty`() {
        // given
        val manager = createInstance()
        val unityEnvironment = anUnityEnvironment()

        every { buildFeature.parameters } returns emptyMap()

        // when
        val commands = manager.activateLicence(unityEnvironment, runnerContext).toList()

        // then
        commands.shouldBeEmpty()
        verify { activateCommand wasNot Called }
    }

    @Test
    fun `should not activate licence when the activation is explicitly disabled via the parameter`() {
        // given
        val manager = createInstance()
        val unityEnvironment = anUnityEnvironment()

        every { buildFeature.parameters } returns mapOf("activateLicense" to "false")

        // when
        val commands = manager.activateLicence(unityEnvironment, runnerContext).toList()

        // then
        commands.shouldBeEmpty()
        verify { activateCommand wasNot Called }
    }

    @Test
    fun `should not activate licence when the parameter value is not valid boolean`() {
        // given
        val manager = createInstance()
        val unityEnvironment = anUnityEnvironment()

        every { buildFeature.parameters } returns mapOf("activateLicense" to "some value")

        // when
        val commands = manager.activateLicence(unityEnvironment, runnerContext).toList()

        // then
        commands.shouldBeEmpty()
        verify { activateCommand wasNot Called }
    }

    @Test
    fun `should not return licence when the parameters of build feature are empty`() {
        // given
        val manager = createInstance()
        val unityEnvironment = anUnityEnvironment()

        every { buildFeature.parameters } returns emptyMap()

        // when
        val commands = manager.returnLicence(unityEnvironment, runnerContext).toList()

        // then
        commands.shouldBeEmpty()
        verify { returnCommand wasNot Called }
    }

    @Test
    fun `should not return licence when the activation is explicitly disabled via the parameter`() {
        // given
        val manager = createInstance()
        val unityEnvironment = anUnityEnvironment()

        every { buildFeature.parameters } returns mapOf("activateLicense" to "false")

        // when
        val commands = manager.returnLicence(unityEnvironment, runnerContext).toList()

        // then
        commands.shouldBeEmpty()
        verify { returnCommand wasNot Called }
    }

    @Test
    fun `should not return licence when the parameter value is not valid boolean`() {
        // given
        val manager = createInstance()
        val unityEnvironment = anUnityEnvironment()

        every { buildFeature.parameters } returns mapOf("activateLicense" to "some value")

        // when
        val commands = manager.returnLicence(unityEnvironment, runnerContext).toList()

        // then
        commands.shouldBeEmpty()
        verify { returnCommand wasNot Called }
    }

    private fun anUnityEnvironment(): UnityEnvironment {
        val unityPath = "/path/to/unity"
        val unityVersion = UnityVersion(2023, 1, 1)
        return UnityEnvironment(unityPath, unityVersion)
    }

    private fun createInstance() = UnityProfessionalLicenceManager(activateCommand, returnCommand)
}