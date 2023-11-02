package jetbrains.buildServer.unity

import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject
import jetbrains.buildServer.util.browser.Element
import kotlin.test.Test
import kotlin.test.assertNotNull

class UnityRunnerDiscoveryExtensionTests {
    @Test
    fun `should detect Unity project`() {
        // arrange
        val discoverer = UnityRunnerDiscoveryExtension()

        // act
        val result = discoverer.discover(
            mockk(),
            mockk {
                every { root } returns createUnityProjectMock("", "UnityGame", "2021.3.16f1")
            }
        )

        // assert
        assertNotNull(result)
        result.size shouldBeExactly 1
        result.first().shouldBeUnityProject("UnityGame", "2021.3.16")
    }

    @Test
    fun `should detect many Unity projects deeper in hierarchy`() {
        // arrange
        val discoverer = UnityRunnerDiscoveryExtension()

        // act
        val result = discoverer.discover(
            mockk(),
            mockk {
                every { root } returns mockk {
                    every { name } returns "projects"
                    every { fullName } returns "projects"
                    every { isLeaf } returns false
                    every { children } returns listOf(
                        createUnityProjectMock("projects", "FooUnityGame", "2023.1.17"),
                        createUnityProjectMock("projects", "BarUnityGame", "2021.3.30"),
                    )
                }
            }
        )

        // assert
        assertNotNull(result)
        result.size shouldBeExactly 2
        result.first().shouldBeUnityProject("projects/FooUnityGame", "2023.1.17")
        result.last().shouldBeUnityProject("projects/BarUnityGame", "2021.3.30")
    }

    private fun DiscoveredObject.shouldBeUnityProject(path: String, associatedVersion: String) {
        type shouldBe UnityConstants.RUNNER_TYPE
        parameters shouldContain (UnityConstants.PARAM_PROJECT_PATH to path)
        parameters shouldContain (UnityConstants.PARAM_UNITY_VERSION to associatedVersion)
    }

    private fun createUnityProjectMock(
        containingDirectory: String,
        projectName: String,
        associatedUnityVersionString: String,
    ): Element {
        return mockk {
            every { fullName } returns sequenceOf(containingDirectory, projectName)
                .filter { it.isNotEmpty() }
                .joinToString(separator = "/")
            every { name } returns projectName
            every { isLeaf } returns false
            every { children } returns listOf(
                mockk {
                    every { name } returns "Assets"
                    every { isLeaf } returns false
                },
                mockk {
                    every { name } returns "ProjectSettings"
                    every { children } returns listOf(
                        mockk {
                            every { name } returns "ProjectVersion.txt"
                            every { isContentAvailable } returns true
                            every { inputStream } returns """
                                        m_EditorVersion: $associatedUnityVersionString
                                        m_EditorVersionWithRevision: $associatedUnityVersionString (4016570cf34f)
                                    """.trimIndent().byteInputStream()
                        }
                    )
                    every { isLeaf } returns false
                }
            )
        }
    }
}