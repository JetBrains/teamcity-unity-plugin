package jetbrains.buildServer.unity

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.unity.detectors.DetectVirtualUnityEnvironmentCommand
import jetbrains.buildServer.unity.detectors.UnityToolProvider
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class UnityEnvironmentProviderTest {

    private val unityToolProvider = mockk<UnityToolProvider>()
    private val detectCommand = mockk<DetectVirtualUnityEnvironmentCommand>()

    @BeforeMethod
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `should initialize unity environment when context is NOT virtual`() {
        // arrange
        val provider = createInstance()
        val context = mockk<UnityBuildRunnerContext>()
        val unityEnvironment = mockk<UnityEnvironment>()
        every { context.isVirtualContext } returns false
        every { unityToolProvider.getUnity(any<UnityBuildRunnerContext>()) } returns unityEnvironment

        // act
        val command = provider.provide(context).firstOrNull()
        val result = provider.unityEnvironment()

        // assert
        command shouldBe null
        result shouldBe unityEnvironment

        verify(exactly = 1) { unityToolProvider.getUnity(context) }
        verify { detectCommand wasNot Called }
    }

    @Test
    fun `should initialize unity environment when context is virtual`() {
        // arrange
        val provider = createInstance()
        val context = mockk<UnityBuildRunnerContext>()
        val unityEnvironment = mockk<UnityEnvironment>()
        every { context.isVirtualContext } returns true
        every { detectCommand.results } returns mutableSetOf(unityEnvironment)

        // act
        val commands = provider.provide(context).toList()
        val result = provider.unityEnvironment()

        // assert
        commands shouldHaveSize 1
        commands shouldContain detectCommand
        result shouldBeEqual unityEnvironment

        verify(exactly = 1) { detectCommand.results }
        verify { unityToolProvider wasNot Called }
    }

    @Test
    fun `should throw exception when unity environment is not initialized`() {
        // arrange
        val provider = createInstance()

        // act // assert
        shouldThrowExactly<ToolCannotBeFoundException> {
            provider.unityEnvironment()
        }.message shouldBe "Unity environment is not initialized yet"
    }

    @Test
    fun `should fail to initialize unity environment when context is virtual and no environment detected`() {
        // arrange
        val provider = createInstance()
        val context = mockk<UnityBuildRunnerContext>()
        every { context.isVirtualContext } returns true
        every { detectCommand.results } returns mutableSetOf()

        // act // assert
        shouldThrowExactly<ToolCannotBeFoundException> {
            provider.provide(context).toList()
        }.message shouldBe "Failed to detect Unity virtual environment"
    }

    private fun createInstance() = UnityEnvironmentProvider(unityToolProvider, detectCommand)
}
