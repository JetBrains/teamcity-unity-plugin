package jetbrains.buildServer.unity.detectors

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.unity.UnityConstants.PARAM_UNITY_VERSION
import jetbrains.buildServer.unity.UnityEnvironment
import jetbrains.buildServer.unity.UnityVersion.Companion.parseVersion
import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DetectVirtualUnityEnvironmentCommandTest {

    @MockK
    private lateinit var runnerContext: BuildRunnerContext

    @MockK
    private lateinit var virtualContext: VirtualContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { runnerContext.isVirtualContext } returns true
        every { runnerContext.virtualContext } returns virtualContext
        every { runnerContext.runnerParameters } returns emptyMap()
    }

    @DataProvider
    fun `correct standard output`(): Array<Array<out Any?>> = arrayOf(
        arrayOf(
            "path=/path/to/Unity;version=2023.3.4",
            linkedSetOf(UnityEnvironment("/path/to/Unity", parseVersion("2023.3.4"), true))
        ),
        arrayOf(
            "path=/path/to/Unity;version=2023.3.4\n",
            linkedSetOf(UnityEnvironment("/path/to/Unity", parseVersion("2023.3.4"), true))
        ),
        arrayOf(
            "path=/path/to/Unity;version=2023.3.4\r",
            linkedSetOf(UnityEnvironment("/path/to/Unity", parseVersion("2023.3.4"), true))
        ),
        arrayOf(
            "path=/path/to/Unity;version=2023.3.4\r\n",
            linkedSetOf(UnityEnvironment("/path/to/Unity", parseVersion("2023.3.4"), true))
        ),
        arrayOf(
            "path=/path/to/1/Unity;version=2023.0.1\npath=/path/to/2/Unity;version=2023.0.2\n",
            linkedSetOf(
                UnityEnvironment("/path/to/1/Unity", parseVersion("2023.0.1"), true),
                UnityEnvironment("/path/to/2/Unity", parseVersion("2023.0.2"), true),
            )
        ),
        arrayOf(
            "path=/path/to/1/Unity;version=2023.0.1\rpath=/path/to/2/Unity;version=2023.0.2\r",
            linkedSetOf(
                UnityEnvironment("/path/to/1/Unity", parseVersion("2023.0.1"), true),
                UnityEnvironment("/path/to/2/Unity", parseVersion("2023.0.2"), true),
            )
        ),
        arrayOf(
            "path=/path/to/1/Unity;version=2023.0.1\r\npath=/path/to/2/Unity;version=2023.0.2\r\n",
            linkedSetOf(
                UnityEnvironment("/path/to/1/Unity", parseVersion("2023.0.1"), true),
                UnityEnvironment("/path/to/2/Unity", parseVersion("2023.0.2"), true),
            )
        ),
        arrayOf(
            "path=/path/to/1/Unity;version=2023.0.1\npath=/path/to/2/Unity;version=2023.0.2\npath=/path/to/3/Unity;version=2023.0.3\n",
            linkedSetOf(
                UnityEnvironment("/path/to/1/Unity", parseVersion("2023.0.1"), true),
                UnityEnvironment("/path/to/2/Unity", parseVersion("2023.0.2"), true),
                UnityEnvironment("/path/to/3/Unity", parseVersion("2023.0.3"), true),
            )
        ),
    )

    @Test(dataProvider = "correct standard output")
    fun `should parse correct standard output`(stdout: String, expectedEnvironment: Set<UnityEnvironment>) {
        // given
        val command = createInstance()

        // when
        command.onStandardOutput(stdout)

        // then
        assertEquals(command.results, expectedEnvironment)
    }

    @DataProvider
    fun `wrong standard output`(): Array<Array<out Any?>> = arrayOf(
        arrayOf(""),
        arrayOf("\n"),
        arrayOf("\r"),
        arrayOf("\r\n"),
        arrayOf("path=/path/to/Unity"),
        arrayOf("version=2023.0.01"),
        arrayOf("path=/path/to/Unityversion=2023.0.01"),
        arrayOf("path=/path/to/Unity;version=wrong.version.format"),
    )

    @Test(dataProvider = "wrong standard output")
    fun `should not fail on wrong standard output`(stdout: String) {
        // given
        val command = createInstance()

        // when
        command.onStandardOutput(stdout)

        // then
        assertEquals(command.results, emptySet<UnityEnvironment>())
    }

    @Test
    fun `should skip detected Unity environment if an expected Unity version is different`() {
        // given
        val command = createInstance()
        val stdout = "path=/path/to/1/Unity;version=2023.0.1\npath=/path/to/2/Unity;version=2023.0.2\n"
        every { runnerContext.runnerParameters } returns mapOf(PARAM_UNITY_VERSION to "2023.0.1")


        // when
        command.onStandardOutput(stdout)

        // then
        assertEquals(
            command.results,
            linkedSetOf(UnityEnvironment("/path/to/1/Unity", parseVersion("2023.0.1"), true))
        )
    }

    @Test
    fun `should not create duplicate Unity environment in case of duplicate stdout`() {
        // given
        val command = createInstance()
        val stdout = "path=/path/to/Unity;version=2023.0.1\npath=/path/to/Unity;version=2023.0.1\n"

        // when
        command.onStandardOutput(stdout)

        // then
        assertEquals(
            command.results,
            linkedSetOf(UnityEnvironment("/path/to/Unity", parseVersion("2023.0.1"), true))
        )
    }

    private fun createInstance() = DetectVirtualUnityEnvironmentCommand(runnerContext)
}