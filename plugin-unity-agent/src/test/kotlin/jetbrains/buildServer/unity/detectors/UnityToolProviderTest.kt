package jetbrains.buildServer.unity.detectors

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import jetbrains.buildServer.ExtensionHolder
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.BuildAgentConfiguration
import jetbrains.buildServer.agent.ToolProvidersRegistry
import jetbrains.buildServer.unity.DetectionMode
import jetbrains.buildServer.unity.UnityBuildRunnerContext
import jetbrains.buildServer.unity.UnityConstants
import jetbrains.buildServer.unity.UnityEnvironment
import jetbrains.buildServer.unity.UnityVersion
import jetbrains.buildServer.unity.util.unityRootParam
import jetbrains.buildServer.unity.util.unityVersionParam
import jetbrains.buildServer.util.EventDispatcher
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class UnityToolProviderTest {

    private val runnerContext = mockk<UnityBuildRunnerContext>()
    private val agentConfiguration = mockk<BuildAgentConfiguration>()
    private val unityDetectorFactory = mockk<UnityDetectorFactory>()
    private val unityDetector = mockk<UnityDetector>()
    private val toolsRegistry = mockk<ToolProvidersRegistry>()
    private val extensionHolder = mockk<ExtensionHolder>()
    private val eventDispatcher = mockk<EventDispatcher<AgentLifeCycleListener>>()

    @BeforeMethod
    fun setUp() {
        clearAllMocks()

        every { unityDetectorFactory.unityDetector() } returns unityDetector
        every { toolsRegistry.registerToolProvider(any()) } returns Unit
        every { eventDispatcher.addListener(any()) } returns Unit
        every { extensionHolder.registerExtension(any(), any(), any()) } returns Unit

        mockkStatic(runnerContext::unityRootParam)
        mockkStatic(runnerContext::unityVersionParam)
        every { runnerContext.unityRootParam() } returns null
        every { runnerContext.unityVersionParam() } returns null
        every { runnerContext.unityProjectPath } returns "foo/bar"
        every { runnerContext.workingDirectory } returns File("foo")
        every { runnerContext.runnerParameters } returns mapOf()
        every { runnerContext.unityProject } returns mockk(relaxed = true)
    }

    @Test
    fun `should detect Unity environment when Unity root param is provided`() {
        // arrange
        val provider = createInstance()
        val unityRootParam = "/path/to/unity"
        val unityVersion = UnityVersion(2023, 1, 1)
        every { runnerContext.unityRootParam() } returns unityRootParam
        every { unityDetector.getVersionFromInstall(File(unityRootParam)) } returns unityVersion
        every { runnerContext.runnerParameters } returns emptyMap()
        every { unityDetector.getEditorPath(File(unityRootParam)) } returns File("$unityRootParam/Unity")

        // act
        val result = provider.getUnity(runnerContext)

        // assert
        result shouldBeEqual UnityEnvironment(File("$unityRootParam/Unity").absolutePath, unityVersion, false)
    }

    @DataProvider
    fun `should return the latest (up to major or minor) Unity version when an exact version is not available params`(): Array<Array<Any>> = arrayOf(
        arrayOf(
            UnityVersion(2023, 2, 8),
            listOf("2023.3.9", "2023.2.7", "2023.2.12"),
            UnityVersion(2023, 2, 12),
        ),
        arrayOf(
            UnityVersion(2023, null, null),
            listOf("2023.1.2", "2023.3.7", "2023.3.9"),
            UnityVersion(2023, 3, 9),
        ),
    )

    @Test(dataProvider = "should return the latest (up to major or minor) Unity version when an exact version is not available params")
    fun `should return the latest (up to major or minor) Unity version when an exact version is not available`(
        specifiedVersion: UnityVersion,
        versions: List<String>,
        expectedVersion: UnityVersion,
    ) {
        // arrange
        val provider = createInstance()
        every { runnerContext.runnerParameters } returns mapOf(
            UnityConstants.PARAM_DETECTION_MODE to DetectionMode.Auto.id,
        )
        every { runnerContext.unityVersionParam() } returns specifiedVersion
        every { unityDetector.getEditorPath(any()) } returns mockk(relaxed = true)
        every { agentConfiguration.configurationParameters } returns versions.associate {
            "${UnityConstants.UNITY_CONFIG_NAME}$it" to "/foo/$it"
        }
        provider.agentStarted(mockk())

        // act
        val result = provider.getUnity(runnerContext)

        // assert
        result.unityVersion shouldBe expectedVersion
    }

    @Test
    fun `should pick up Unity version from the project settings if no one is specified explicitly`() {
        // arrange
        val provider = createInstance()
        every { runnerContext.runnerParameters } returns mapOf(
            UnityConstants.PARAM_DETECTION_MODE to DetectionMode.Auto.id,
            UnityConstants.PARAM_PROJECT_PATH to "/bar",
        )
        every { runnerContext.unityVersionParam() } returns null
        every { unityDetector.getEditorPath(any()) } returns mockk(relaxed = true)
        val expectedVersion = UnityVersion(2022, 3, 9)
        every { agentConfiguration.configurationParameters } returns mapOf(
            "${UnityConstants.UNITY_CONFIG_NAME}$expectedVersion" to "/foo/$expectedVersion",
            "${UnityConstants.UNITY_CONFIG_NAME}2023.3.9" to "/foo/2023.3.9",
            "${UnityConstants.UNITY_CONFIG_NAME}2021.3.9" to "/foo/2021.3.9",
        )
        provider.agentStarted(mockk())
        every { runnerContext.unityProject } returns mockk {
            every { unityVersion } returns expectedVersion
        }

        // act
        val result = provider.getUnity(runnerContext)

        // assert
        result.unityVersion shouldBeEqual expectedVersion
    }

    @DataProvider
    fun `should return latest Unity version if no one is specified explicitly and project settings unavailable cases`(): Array<Array<Any>> = arrayOf(
        arrayOf(listOf("2020.3.38f1", "2020.3.43f1", "2020.3.32f1"), UnityVersion(2020, 3, 43)),
        arrayOf(listOf("2020.1.0f1", "2020.2.43f1", "2020.10.32f1"), UnityVersion(2020, 10, 32)),
    )

    @Test(dataProvider = "should return latest Unity version if no one is specified explicitly and project settings unavailable cases")
    fun `should return latest Unity version if no one is specified explicitly and project settings unavailable`(versions: List<String>, expectedVersion: UnityVersion) {
        // arrange
        val provider = createInstance()

        every { agentConfiguration.configurationParameters } returns versions
            .associate { "${UnityConstants.UNITY_CONFIG_NAME}$it" to "/Applications/Unity/Hub/Editor/$it" }
        provider.agentStarted(mockk())
        every { runnerContext.unityRootParam() } returns null
        every { unityDetector.getEditorPath(any()) } returns mockk(relaxed = true)

        // act
        val environment = provider.getUnity(runnerContext)

        // assert
        assertEquals(expectedVersion, environment.unityVersion)
    }

    private fun createInstance() = UnityToolProvider(
        agentConfiguration,
        unityDetectorFactory,
        toolsRegistry,
        extensionHolder,
        eventDispatcher,
    )
}
