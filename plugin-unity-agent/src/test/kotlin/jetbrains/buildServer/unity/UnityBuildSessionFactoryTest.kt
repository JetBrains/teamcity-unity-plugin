package jetbrains.buildServer.unity

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearAllMocks
import io.mockk.mockk
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.unity.detectors.UnityToolProvider
import jetbrains.buildServer.unity.util.FileSystemService
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class UnityBuildSessionFactoryTest {
    private val unityToolProvider = mockk<UnityToolProvider>()
    private val fileSystemService = mockk<FileSystemService>()

    @BeforeMethod
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `should create unity command build session`() {
        // given
        val factory = createInstance()
        val runnerContext = mockk<BuildRunnerContext>()

        // when
        val session = factory.createSession(runnerContext)

        // then
        session.shouldBeInstanceOf<UnityCommandBuildSession>()
    }

    @Test
    fun `should return correct build runner info`() {
        // given
        val factory = createInstance()

        // when
        val buildRunnerInfo = factory.buildRunnerInfo

        // then
        buildRunnerInfo.type shouldBeEqual "unity"
        buildRunnerInfo.canRun(mockk()) shouldBe true
    }

    private fun createInstance() = UnityBuildSessionFactory(
        unityToolProvider,
        fileSystemService,
    )
}