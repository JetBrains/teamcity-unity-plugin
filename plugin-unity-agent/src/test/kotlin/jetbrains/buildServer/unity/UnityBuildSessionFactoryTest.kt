package jetbrains.buildServer.unity

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.unity.detectors.UnityToolProvider
import jetbrains.buildServer.unity.util.CommandLineRunner
import jetbrains.buildServer.unity.util.FileSystemService
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class UnityBuildSessionFactoryTest {
    private val unityToolProvider = mockk<UnityToolProvider>()
    private val fileSystemService = mockk<FileSystemService>()
    private val commandLineRunner = mockk<CommandLineRunner>()

    @BeforeMethod
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `should create unity command build session`() {
        // arrange
        val factory = createInstance()
        val runnerContext = mockk<BuildRunnerContext> {
            every { build } returns mockk(relaxed = true)
            every { workingDirectory } returns File("working-directory")
            every { virtualContext } returns mockk(relaxed = true)
            every { buildParameters } returns mockk(relaxed = true)
        }

        // act
        val session = factory.createSession(runnerContext)

        // assert
        session.shouldBeInstanceOf<UnityCommandBuildSession>()
    }

    @Test
    fun `should return correct build runner info`() {
        // arrange
        val factory = createInstance()

        // act
        val buildRunnerInfo = factory.buildRunnerInfo

        // assert
        buildRunnerInfo.type shouldBeEqual "unity"
        buildRunnerInfo.canRun(mockk()) shouldBe true
    }

    private fun createInstance() = UnityBuildSessionFactory(
        unityToolProvider,
        fileSystemService,
        commandLineRunner,
    )
}
