package jetbrains.buildServer.unity

import com.intellij.execution.configurations.GeneralCommandLine
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import jetbrains.buildServer.ExecResult
import jetbrains.buildServer.agent.AgentBuildFeature
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildRunnerSettings
import jetbrains.buildServer.agent.impl.AgentEventDispatcher
import jetbrains.buildServer.unity.util.CommandLineRunner
import java.nio.file.Files
import java.nio.file.Path
import kotlin.random.Random
import kotlin.test.*

class UnityLicenseManagerTests {
    private val editorPath = "somePath"
    private var tmpDir: Path? = null

    private val toolProviderMock = mockk<UnityToolProvider> {
        every { getUnityPath(any(), any()) } returns editorPath
    }
    private val commandLineRunnerMock = mockk<CommandLineRunner>()
    private val eventDispatcherMock = mockk<AgentEventDispatcher> {
        every { addListener(any()) } returns Unit
    }

    @BeforeTest
    fun setUp() {
        clearMocks(commandLineRunnerMock)
        tmpDir = Files.createTempDirectory(null)
    }

    @AfterTest
    fun tearDown() {
        if (tmpDir != null) {
            Files.walk(tmpDir!!)
                .sorted(Comparator.reverseOrder())
                .forEach {
                    Files.deleteIfExists(it)
                }
        }
    }

    @Test
    fun buildStarted_happyPath_activateLicenseCommandIsGenerated() {
        // arrange
        val commandCapturingSlot = slot<GeneralCommandLine>()
        every { commandLineRunnerMock.run(capture(commandCapturingSlot)) } returns ExecResult()
        val fakeUnityBuildFeature = FakeUnityBuildFeature()
        val buildMock = generateBuildMock(fakeUnityBuildFeature, tmpDir!!)

        // act
        UnityLicenseManager(toolProviderMock, commandLineRunnerMock, eventDispatcherMock).buildStarted(buildMock)

        // assert
        assertNotNull(commandCapturingSlot.captured)
        val command = commandCapturingSlot.captured
        assertEquals(editorPath, command.exePath)
        val parameterString = command.parametersList.parametersString
        assertContains(parameterString, "-serial ${fakeUnityBuildFeature.parameters[UnityConstants.PARAM_SERIAL_NUMBER]}")
        assertContains(parameterString, "-username ${fakeUnityBuildFeature.parameters[UnityConstants.PARAM_USERNAME]}")
        assertContains(parameterString, "-password ${fakeUnityBuildFeature.parameters[UnityConstants.PARAM_PASSWORD]}")
    }

    @Test
    fun buildFinished_happyPath_returnLicenseCommandIsGenerated() {
        // arrange
        val commandCapturingSlot = slot<GeneralCommandLine>()
        every { commandLineRunnerMock.run(capture(commandCapturingSlot)) } returns ExecResult()
        val fakeUnityBuildFeature = FakeUnityBuildFeature()
        val buildMock = generateBuildMock(fakeUnityBuildFeature, tmpDir!!)

        val sut = UnityLicenseManager(toolProviderMock, commandLineRunnerMock, eventDispatcherMock)
        sut.buildStarted(buildMock)
        // act
        sut.buildFinished(buildMock, mockk())

        // assert
        assertNotNull(commandCapturingSlot.captured)
        val command = commandCapturingSlot.captured
        assertEquals(editorPath, command.exePath)
        val parameterString = command.parametersList.parametersString
        assertContains(parameterString, "-returnlicense")
        assertContains(parameterString, "-username ${fakeUnityBuildFeature.parameters[UnityConstants.PARAM_USERNAME]}")
        assertContains(parameterString, "-password ${fakeUnityBuildFeature.parameters[UnityConstants.PARAM_PASSWORD]}")
    }
}

class FakeUnityBuildFeature(
    init: Map<String, String> = mapOf()
) : AgentBuildFeature {
    private val parameters: MutableMap<String, String>
    init {
        parameters = (mapOf(
            UnityConstants.PARAM_ACTIVATE_LICENSE to true.toString(),
            UnityConstants.PARAM_UNITY_VERSION to "2021.3.16",
            UnityConstants.PARAM_SERIAL_NUMBER to "some-serial-number",
            UnityConstants.PARAM_USERNAME to "some-username",
            UnityConstants.PARAM_PASSWORD to "some-password",
        ) + init).toMutableMap()
    }
    override fun getType() = UnityConstants.BUILD_FEATURE_TYPE
    override fun getParameters(): MutableMap<String, String> = parameters
}

fun generateBuildMock(
    fakeUnityBuildFeature: FakeUnityBuildFeature,
    tmpDir: Path
) = mockk<AgentRunningBuild> {
    every { getBuildFeaturesOfType(UnityConstants.BUILD_FEATURE_TYPE) } returns listOf(fakeUnityBuildFeature)
    every { buildRunners } returns listOf(generateUnityBuildRunnerMock())
    every { buildId } returns Random.nextLong(0, Long.MAX_VALUE)
    every { agentTempDirectory } returns tmpDir.toFile()
    every { buildLogger } returns mockk(relaxed = true)
}

fun generateUnityBuildRunnerMock() = mockk<BuildRunnerSettings> {
    every { isEnabled } returns true
    every { runType } returns UnityConstants.RUNNER_TYPE
    every { runnerParameters["plugin.docker.imageId"] } returns null
}