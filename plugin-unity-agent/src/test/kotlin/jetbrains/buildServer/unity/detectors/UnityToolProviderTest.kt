package jetbrains.buildServer.unity.detectors

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import jetbrains.buildServer.ExtensionHolder
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.unity.UnityConstants
import jetbrains.buildServer.unity.UnityEnvironment
import jetbrains.buildServer.unity.UnityVersion
import jetbrains.buildServer.unity.util.unityRootParam
import jetbrains.buildServer.unity.util.unityVersionParam
import jetbrains.buildServer.util.EventDispatcher
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Parameters
import org.testng.annotations.Test
import java.io.File
import kotlin.test.assertEquals

class UnityToolProviderTest {

    private val runnerContext = mockk<BuildRunnerContext>()
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
    }

    @Test
    fun `should throw exception if wrong tool name is provided`() {
        // arrange
        val provider = createInstance()

        // act // assert
        shouldThrowExactly<ToolCannotBeFoundException> { provider.getUnity("wrong tool name") }
            .message?.shouldBeEqual("Unsupported tool wrong tool name")
    }

    @Test
    fun `should throw exception when Unity detector was not created`() {
        // arrange
        every { unityDetectorFactory.unityDetector() } returns null
        val provider = createInstance()

        // act // assert
        shouldThrowExactly<ToolCannotBeFoundException> { provider.getUnity("unity") }
            .message?.shouldBeEqual("unity")
    }

    @Test
    fun `should detect Unity environment when Unity root param is provided`() {
        // arrange
        val provider = createInstance()
        val unityRootParam = "/path/to/unity"
        val unityVersion = UnityVersion(2023, 1, 1)
        every { runnerContext.unityRootParam() } returns unityRootParam
        every { unityDetector.getVersionFromInstall(File(unityRootParam)) } returns unityVersion
        every { unityDetector.getEditorPath(File(unityRootParam)) } returns File("$unityRootParam/Unity")

        // act
        val result = provider.getUnity("unity", runnerContext)

        // assert
        result shouldBeEqual UnityEnvironment(File("$unityRootParam/Unity").absolutePath, unityVersion, false)
    }

    @DataProvider
    fun `should return latest Unity version if no one is specified explicitly params`(): Array<Array<Any>> = arrayOf(
        arrayOf(listOf("2020.3.38f1", "2020.3.43f1", "2020.3.32f1"), UnityVersion(2020, 3, 43)),
        arrayOf(listOf("2020.1.0f1", "2020.2.43f1", "2020.10.32f1"), UnityVersion(2020, 10, 32))
    )

    @Test(dataProvider = "should return latest Unity version if no one is specified explicitly params")
    fun `should return latest Unity version if no one is specified explicitly`(versions: List<String>, expectedVersion: UnityVersion) {
        // arrange
        val provider = createInstance()

        every { agentConfiguration.configurationParameters } returns versions
            .associate { "${UnityConstants.UNITY_CONFIG_NAME}${it}" to "/Applications/Unity/Hub/Editor/${it}" }
        provider.agentStarted(mockk())
        every { runnerContext.unityRootParam() } returns null
        every { unityDetector.getEditorPath(any()) } returns mockk(relaxed = true)

        // act
        val environment = provider.getUnity("unity", runnerContext)

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