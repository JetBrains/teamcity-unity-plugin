/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.unity

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.agent.AgentBuildFeature
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.unity.UnityVersion.Companion.parseVersion
import org.testng.annotations.DataProvider
import java.io.File
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test

class UnityRunnerBuildServiceTest {
    private val defaultEditorPath = "somePath"
    private val defaultUnityVersion = parseVersion("2021.3.16")
    private val defaultUnityEnvironment = UnityEnvironment(defaultEditorPath, defaultUnityVersion)

    private val buildRunnerContextMock = mockk<BuildRunnerContext>()
    private val agentRunningBuildMock = mockk<AgentRunningBuild>()

    @BeforeTest
    fun setUp() {
        clearAllMocks()
        defaultMockSetup()
    }

    private fun defaultMockSetup() {
        buildRunnerContextMock.apply {
            every { runnerParameters } returns mapOf(
                "noGraphics" to true.toString()
            )
            every { buildParameters } returns mockk(relaxed = true)
            every { workingDirectory } returns File("dir")
            every { isVirtualContext } returns false
        }

        agentRunningBuildMock.apply {
            every { buildLogger } returns mockk(relaxed = true)
            every { buildId } returns Random.nextLong(0, Long.MAX_VALUE)
            every { getBuildFeaturesOfType(UnityConstants.BUILD_FEATURE_TYPE) } returns listOf(FakeUnityBuildFeature())
        }
    }

    data class LogArgumentTestCase(
        val system: String,
        val unityVersion: UnityVersion
    )

    @DataProvider(name = "consoleLogOutput")
    fun logArgumentTestData(): Array<LogArgumentTestCase> {
        return arrayOf(
            LogArgumentTestCase("windows", defaultUnityVersion),
            LogArgumentTestCase("linux", defaultUnityVersion),
            LogArgumentTestCase("mac", defaultUnityVersion)
        )
    }

    @Test(dataProvider = "consoleLogOutput")
    fun makeProgramCommandLine_consoleLogOutput_correctLogArgumentGenerated(case: LogArgumentTestCase) {
        // arrange
        val sut = UnityRunnerBuildService(defaultUnityEnvironment, emptyMap())
        sut.initialize(agentRunningBuildMock, buildRunnerContextMock)

        System.setProperty("os.name", case.system)

        // act
        val commandLine = sut.makeProgramCommandLine()

        // assert
        commandLine shouldNotBe null
        val commandString = commandLine.arguments.joinToString(" ")
        commandString shouldContain "-logFile -"
    }

    @Test
    fun makeProgramCommandLine_virtualContext_correctCommandLineGenerated() {
        // arrange
        val virtualContext = mockk<VirtualContext>()
        virtualContext.apply {
            every { resolvePath("./") } returns "/converted/project"
            every { resolvePath(File("/logs").absolutePath) } returns "/converted/logs"
            every { resolvePath(File("/player").absolutePath) } returns "/converted/player"
        }

        buildRunnerContextMock.apply {
            every { isVirtualContext } returns true
            every { getVirtualContext() } returns virtualContext
            every { runnerParameters } returns mapOf(
                "noGraphics" to true.toString(),
                "logFilePath" to File("/logs").absolutePath,
                "buildPlayer" to "player",
                "buildPlayerPath" to File("/player").absolutePath
            )
        }

        val sut = UnityRunnerBuildService(defaultUnityEnvironment, emptyMap())
        sut.initialize(agentRunningBuildMock, buildRunnerContextMock)

        System.setProperty("os.name", "linux")

        // act
        val commandLine = sut.makeProgramCommandLine()

        // assert
        commandLine shouldNotBe null
        val commandString = commandLine.arguments.joinToString(" ")
        commandString shouldBeEqual "-batchmode -projectPath /converted/project " +
                "-player /converted/player -nographics -logFile /converted/logs"
    }

    private inner class FakeUnityBuildFeature(
        init: Map<String, String> = mapOf()
    ) : AgentBuildFeature {
        private val parameters: MutableMap<String, String>

        init {
            parameters = (mapOf(
                UnityConstants.PARAM_UNITY_VERSION to defaultUnityVersion.toString(),
            ) + init).toMutableMap()
        }

        override fun getType() = UnityConstants.BUILD_FEATURE_TYPE
        override fun getParameters(): MutableMap<String, String> = parameters
    }
}