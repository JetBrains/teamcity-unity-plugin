package jetbrains.buildServer.unity.license

import com.intellij.execution.configurations.GeneralCommandLine
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jetbrains.buildServer.ExecResult
import jetbrains.buildServer.agent.AgentBuildFeature
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.impl.AgentEventDispatcher
import jetbrains.buildServer.unity.UnityConstants
import jetbrains.buildServer.unity.UnityEnvironment
import jetbrains.buildServer.unity.UnityLicenseScope
import jetbrains.buildServer.unity.UnityLicenseTypeParameter
import jetbrains.buildServer.unity.UnityVersion
import jetbrains.buildServer.unity.detectors.UnityToolProvider
import jetbrains.buildServer.unity.util.CommandLineRunner
import jetbrains.buildServer.unity.util.FileSystemService
import org.testng.annotations.DataProvider
import kotlin.io.path.pathString
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UnityBuildScopeLicenseActivatorTest {
    companion object {
        private const val UNITY_PASSWORD = "password"
        private const val UNITY_USERNAME = "username"
        private const val UNITY_SERIAL_NUMBER = "serial-number"
    }

    private val unityEnvironment = UnityEnvironment(
        "somePath",
        UnityVersion(2022, 3, 31),
    )
    private val toolProviderMock = mockk<UnityToolProvider>()
    private val commandLineRunnerMock = mockk<CommandLineRunner>()
    private val fileSystemServiceMock = mockk<FileSystemService>()
    private val eventDispatcherMock = mockk<AgentEventDispatcher>()

    @BeforeTest
    fun setUp() {
        clearMocks(commandLineRunnerMock)

        every { fileSystemServiceMock.createTempFile(any(), any(), any()) } returns mockk {
            every { toAbsolutePath() } returns mockk {
                every { pathString } returns "tmp/some-path"
            }
        }
        every { fileSystemServiceMock.readText(any()) } returns "some log"
        every { toolProviderMock.getUnity(any<AgentRunningBuild>()) } returns unityEnvironment
        every { eventDispatcherMock.addListener(any()) } returns Unit
    }

    @Test
    fun `should activate license before build`() {
        // arrange
        val commandCapturingSlot = slot<GeneralCommandLine>()
        every { commandLineRunnerMock.run(capture(commandCapturingSlot)) } returns ExecResult()
        val build = generateBuild()

        // act
        licensePerBuildActivator().preparationFinished(build)

        // assert
        val command = commandCapturingSlot.captured
        assertNotNull(command)
        assertEquals(unityEnvironment.unityPath, command.exePath)
        val parameterString = command.parametersList.parametersString
        assertContains(parameterString, "-username $UNITY_USERNAME")
        assertContains(parameterString, "-password $UNITY_PASSWORD")
        assertContains(parameterString, "-serial $UNITY_SERIAL_NUMBER")
    }

    @Test
    fun `should return license after build`() {
        // arrange
        val commandCapturingSlot = slot<GeneralCommandLine>()
        every { commandLineRunnerMock.run(capture(commandCapturingSlot)) } returns ExecResult()
        val build = generateBuild()

        // act
        licensePerBuildActivator().beforeBuildFinish(build, mockk())

        // assert
        assertNotNull(commandCapturingSlot.captured)
        val command = commandCapturingSlot.captured
        assertEquals(unityEnvironment.unityPath, command.exePath)
        val parameterString = command.parametersList.parametersString
        assertContains(parameterString, "-returnlicense")
        assertContains(parameterString, "-username $UNITY_USERNAME")
        assertContains(parameterString, "-password $UNITY_PASSWORD")
    }

    @DataProvider
    fun `skip license activation test cases`(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(
                mapOf(
                    UnityConstants.PARAM_ACTIVATE_LICENSE to null,
                    UnityConstants.PARAM_UNITY_LICENSE_TYPE to UnityLicenseTypeParameter.PERSONAL.toString(),
                ),
            ),
            arrayOf(
                mapOf(
                    UnityConstants.PARAM_UNITY_LICENSE_SCOPE to UnityLicenseScope.BUILD_STEP.id,
                ),
            ),
            arrayOf(
                mapOf(
                    UnityConstants.PARAM_ACTIVATE_LICENSE to false.toString(),
                ),
            ),
            arrayOf(
                mapOf(
                    UnityConstants.PARAM_UNITY_LICENSE_SCOPE to null,
                ),
            ),
        )
    }

    @Test(dataProvider = "skip license activation test cases")
    fun `do not activate license if not specified in the settings`(
        buildFeatureParameters: Map<String, String>,
    ) {
        // arrange
        val build = generateBuild(buildFeatureParameters)
        every { commandLineRunnerMock.run(any()) } returns ExecResult()

        // act
        licensePerBuildActivator().preparationFinished(build)

        // assert
        verify(exactly = 0) { commandLineRunnerMock.run(any()) }
    }

    @Test(dataProvider = "skip license activation test cases")
    fun `do not return license if not specified in the settings`(
        buildFeatureParameters: Map<String, String>,
    ) {
        // arrange
        val build = generateBuild(buildFeatureParameters)
        every { commandLineRunnerMock.run(any()) } returns ExecResult()

        // act
        licensePerBuildActivator().beforeBuildFinish(build, mockk())

        // assert
        verify(exactly = 0) { commandLineRunnerMock.run(any()) }
    }

    private fun licensePerBuildActivator() =
        UnityBuildScopeLicenseActivator(
            toolProviderMock,
            fileSystemServiceMock,
            commandLineRunnerMock,
            eventDispatcherMock,
        )

    private class FakeUnityBuildFeature(
        init: Map<String, String> = mapOf(),
    ) : AgentBuildFeature {
        private val parameters: MutableMap<String, String> = (
            mapOf(
                UnityConstants.PARAM_ACTIVATE_LICENSE to true.toString(),
                UnityConstants.PARAM_UNITY_LICENSE_SCOPE to UnityLicenseScope.BUILD_CONFIGURATION.id,
                UnityConstants.PARAM_UNITY_LICENSE_TYPE to UnityLicenseTypeParameter.PROFESSIONAL.toString(),
                UnityConstants.PARAM_UNITY_VERSION to "2022.3.33",
                UnityConstants.PARAM_SERIAL_NUMBER to UNITY_SERIAL_NUMBER,
                UnityConstants.PARAM_USERNAME to UNITY_USERNAME,
                UnityConstants.PARAM_PASSWORD to UNITY_PASSWORD,
            ) + init
            ).toMutableMap()

        override fun getType() = UnityConstants.BUILD_FEATURE_TYPE
        override fun getParameters(): MutableMap<String, String> = parameters
    }

    private fun generateBuild(
        buildFeatureParameters: Map<String, String> = emptyMap(),
    ) = mockk<AgentRunningBuild> {
        every { getBuildFeaturesOfType(UnityConstants.BUILD_FEATURE_TYPE) } returns listOf(
            FakeUnityBuildFeature(buildFeatureParameters),
        )
        every { buildId } returns Random.nextLong(0, Long.MAX_VALUE)
        every { agentTempDirectory } returns mockk(relaxed = true)
        every { buildLogger } returns mockk(relaxed = true)
        every { sharedBuildParameters } returns mockk {
            every { environmentVariables } returns mapOf()
        }
        every { checkoutDirectory } returns mockk {
            every { path } returns "checkout-dir-path"
        }
    }
}
