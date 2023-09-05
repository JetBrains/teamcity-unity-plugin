package jetbrains.buildServer.unity.detectors

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import jetbrains.buildServer.ExtensionHolder
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.unity.UnityEnvironment
import jetbrains.buildServer.unity.UnityVersion
import jetbrains.buildServer.unity.util.unityRootParam
import jetbrains.buildServer.unity.util.unityVersionParam
import jetbrains.buildServer.util.EventDispatcher
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

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
    fun `should throw exception when unity detector was not created`() {
        // arrange
        every { unityDetectorFactory.unityDetector() } returns null
        val provider = createInstance()

        // act // assert
        shouldThrowExactly<ToolCannotBeFoundException> { provider.getUnity("unity") }
            .message?.shouldBeEqual("unity")
    }

    @Test
    fun `should detect unity environment when unity root param is provided`() {
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

    private fun createInstance() = UnityToolProvider(
        agentConfiguration,
        unityDetectorFactory,
        toolsRegistry,
        extensionHolder,
        eventDispatcher,
    )
}