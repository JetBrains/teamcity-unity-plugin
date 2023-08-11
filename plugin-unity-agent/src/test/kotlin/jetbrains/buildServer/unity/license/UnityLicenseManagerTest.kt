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
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class UnityLicenseManagerTest {

    private val activatePersonalCommand = mockk<ActivatePersonalLicenseCommand>()
    private val activateProCommand = mockk<ActivateProLicenseCommand>()
    private val returnProCommand = mockk<ReturnProLicenseCommand>()

    private val runnerContext = mockk<BuildRunnerContext>()
    private val build = mockk<AgentRunningBuild>()
    private val buildFeature = mockk<AgentBuildFeature>()

    @BeforeMethod
    fun setUp() {
        clearAllMocks()

        every { runnerContext.build } returns build
        every { build.getBuildFeaturesOfType(any()) } returns listOf(buildFeature)
    }

    @DataProvider
    fun `should manage professional license params`(): Array<Array<Any>> = arrayOf(
        arrayOf(mapOf("unityLicenseType" to "professionalLicense")),
        arrayOf(mapOf("activateLicense" to "true")),
    )

    @Test(dataProvider = "should manage professional license params")
    fun `should activate professional license`(params: Map<String, String>) {
        // given
        val manager = createInstance()
        val unityEnvironment = anUnityEnvironment()

        every { buildFeature.parameters } returns params
        every { activateProCommand.withUnityEnvironment(unityEnvironment) } returns activateProCommand

        // when
        val commands = manager.activateLicense(unityEnvironment, runnerContext).toList()

        // then
        commands.shouldNotBeEmpty()
        commands.shouldContainExactly(activateProCommand)

        verify(exactly = 1) { activateProCommand.withUnityEnvironment(unityEnvironment) }
        verify { activatePersonalCommand wasNot Called }
        verify { returnProCommand wasNot Called }
    }

    @Test(dataProvider = "should manage professional license params")
    fun `should return professional license`(params: Map<String, String>) {
        // given
        val manager = createInstance()
        val unityEnvironment = anUnityEnvironment()

        every { buildFeature.parameters } returns params
        every { returnProCommand.withUnityEnvironment(unityEnvironment) } returns returnProCommand

        // when
        val commands = manager.returnLicense(unityEnvironment, runnerContext).toList()

        // then
        commands.shouldNotBeEmpty()
        commands.shouldContainExactly(returnProCommand)

        verify(exactly = 1) { returnProCommand.withUnityEnvironment(unityEnvironment) }
        verify { activatePersonalCommand wasNot Called }
        verify { activateProCommand wasNot Called }
    }

    @Test
    fun `should activate personal license`() {
        // given
        val manager = createInstance()
        val unityEnvironment = anUnityEnvironment()

        every { buildFeature.parameters } returns mapOf("unityLicenseType" to "personalLicense")
        every { activatePersonalCommand.withUnityEnvironment(unityEnvironment) } returns activatePersonalCommand

        // when
        val commands = manager.activateLicense(unityEnvironment, runnerContext).toList()

        // then
        commands.shouldNotBeEmpty()
        commands.shouldContainExactly(activatePersonalCommand)

        verify(exactly = 1) { activatePersonalCommand.withUnityEnvironment(unityEnvironment) }
        verify { activateProCommand wasNot Called }
        verify { returnProCommand wasNot Called }
    }

    @Test
    fun `should not return personal license`() {
        // given
        val manager = createInstance()
        val unityEnvironment = anUnityEnvironment()

        every { buildFeature.parameters } returns mapOf("unityLicenseType" to "personalLicense")

        // when
        val commands = manager.returnLicense(unityEnvironment, runnerContext).toList()

        // then
        commands.shouldBeEmpty()

        verify { activatePersonalCommand wasNot Called }
        verify { activateProCommand wasNot Called }
        verify { returnProCommand wasNot Called }
    }

    @DataProvider
    fun `should not manage licenses params`(): Array<Array<Any>> = arrayOf(
        arrayOf(emptyMap<String, String>()),
        arrayOf(mapOf("unityLicenseType" to "invalid license type")),
        arrayOf(mapOf("activateLicense" to "false")),
        arrayOf(mapOf("activateLicense" to "invalid boolean")),
        arrayOf(mapOf("notRelevantParam" to "some value")),
    )

    @Test(dataProvider = "should not manage licenses params")
    fun `should not manage licenses`(params: Map<String, String>) {
        // given
        val manager = createInstance()
        val unityEnvironment = anUnityEnvironment()

        every { buildFeature.parameters } returns params

        // when
        val activateCommands = manager.activateLicense(unityEnvironment, runnerContext).toList()
        val returnCommands = manager.returnLicense(unityEnvironment, runnerContext).toList()

        // then
        activateCommands.shouldBeEmpty()
        returnCommands.shouldBeEmpty()

        verify { activatePersonalCommand wasNot Called }
        verify { activateProCommand wasNot Called }
        verify { returnProCommand wasNot Called }
    }

    private fun anUnityEnvironment(): UnityEnvironment {
        val unityPath = "/path/to/unity"
        val unityVersion = UnityVersion(2023, 1, 1)
        return UnityEnvironment(unityPath, unityVersion)
    }

    private fun createInstance() = UnityLicenseManager(
        activatePersonalCommand,
        activateProCommand,
        returnProCommand,
    )
}