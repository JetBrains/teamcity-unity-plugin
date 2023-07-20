package jetbrains.buildServer.unity.util

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import jetbrains.buildServer.agent.AgentBuildFeature
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.unity.UnityVersion
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class UnityParametersExtractorTest {

    private val runnerContext = mockk<BuildRunnerContext>()
    private val build = mockk<AgentRunningBuild>()
    private val buildFeature = mockk<AgentBuildFeature>()

    @BeforeMethod
    fun setUp() {
        clearAllMocks()

        every { runnerContext.build } returns build
        every { build.getBuildFeaturesOfType(any()) } returns setOf(buildFeature)
    }

    @Test
    fun `should return unityRoot from runner parameters`() {
        // given
        every { runnerContext.runnerParameters } returns mapOf("unityRoot" to "/path/to/unity")

        // when
        val result = runnerContext.unityRootParam()

        // then
        result shouldNotBe null
        result?.shouldBeEqual("/path/to/unity")

        verify { build wasNot Called }
        verify { buildFeature wasNot Called }
    }

    @Test
    fun `should return unityRoot from build feature parameters`() {
        // given
        every { runnerContext.runnerParameters } returns mapOf()
        every { buildFeature.parameters } returns mapOf("unityRoot" to "/path/to/unity")

        // when
        val result = runnerContext.unityRootParam()

        // then
        result shouldNotBe null
        result?.shouldBeEqual("/path/to/unity")
    }

    @Test
    fun `should return null if unityRoot parameter is not found`() {
        // given
        every { runnerContext.runnerParameters } returns mapOf()
        every { buildFeature.parameters } returns mapOf()

        // when
        val result = runnerContext.unityRootParam()

        // then
        result shouldBe null
    }

    @Test
    fun `should return unity version from runner parameters`() {
        // given
        every { runnerContext.runnerParameters } returns mapOf("unityVersion" to "2023.1.1")

        // when
        val result = runnerContext.unityVersionParam()

        // then
        result shouldNotBe null
        result?.shouldBeEqual(UnityVersion(2023, 1, 1))

        verify { build wasNot Called }
        verify { buildFeature wasNot Called }
    }

    @Test
    fun `should return unity version from build feature parameters`() {
        // given
        every { runnerContext.runnerParameters } returns mapOf()
        every { buildFeature.parameters } returns mapOf("unityVersion" to "2023.1.1")

        // when
        val result = runnerContext.unityVersionParam()

        // then
        result shouldNotBe null
        result?.shouldBeEqual(UnityVersion(2023, 1, 1))
    }

    @Test
    fun `should return unity version when it is surrounded with spaces`() {
        // given
        every { runnerContext.runnerParameters } returns mapOf("unityVersion" to " 2023.1.1 ")

        // when
        val result = runnerContext.unityVersionParam()

        // then
        result shouldNotBe null
        result?.shouldBeEqual(UnityVersion(2023, 1, 1))

        verify { build wasNot Called }
        verify { buildFeature wasNot Called }
    }

    @Test
    fun `should return null if unityVersion parameter is not found`() {
        // given
        every { runnerContext.runnerParameters } returns mapOf()
        every { buildFeature.parameters } returns mapOf()

        // when
        val result = runnerContext.unityVersionParam()

        // then
        result shouldBe null
    }

    @Test
    fun `should return null if unityVersion parameter contains not valid version`() {
        // given
        every { runnerContext.runnerParameters } returns mapOf("unityVersion" to "invalid version string")

        // when
        val result = runnerContext.unityVersionParam()

        // then
        result shouldBe null
    }
}