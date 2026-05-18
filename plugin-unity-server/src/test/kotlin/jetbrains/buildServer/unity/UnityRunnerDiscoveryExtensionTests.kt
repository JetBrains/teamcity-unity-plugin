package jetbrains.buildServer.unity

import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject
import jetbrains.buildServer.util.browser.Browser
import jetbrains.buildServer.util.browser.Element
import kotlin.test.Test
import kotlin.test.assertNotNull

class UnityRunnerDiscoveryExtensionTests {
    @Test
    fun `should detect Unity project`() {
        // arrange
        val discoverer = UnityRunnerDiscoveryExtension()
        val unityVersion = "2021.3.16"

        // act
        val result = discoverer.discover(
            mockk(),
            mockk {
                every { root } returns createUnityProjectMock("", "UnityGame", unityVersion)
            },
        )

        // assert
        assertNotNull(result)
        result.size shouldBeExactly 1
        result.first().shouldBeUnityProject("UnityGame", unityVersion)
    }

    @Test
    fun `should detect many Unity projects deeper in hierarchy`() {
        // arrange
        val discoverer = UnityRunnerDiscoveryExtension()
        val firstProjectUnityVersion = "2023.1.17"
        val secondProjectUnityVersion = "2021.3.30"

        // act
        val result = discoverer.discover(
            mockk(),
            mockk {
                every { root } returns mockk {
                    every { name } returns "projects"
                    every { fullName } returns "projects"
                    every { isLeaf } returns false
                    every { getBrowser() } returns mockk { every { getElement(any()) } returns null }
                    every { children } returns listOf(
                        createUnityProjectMock("projects", "FooUnityGame", firstProjectUnityVersion),
                        createUnityProjectMock("projects", "BarUnityGame", secondProjectUnityVersion),
                    )
                }
            },
        )

        // assert
        assertNotNull(result)
        result.size shouldBeExactly 2
        result.first().shouldBeUnityProject("projects/FooUnityGame", firstProjectUnityVersion)
        result.last().shouldBeUnityProject("projects/BarUnityGame", secondProjectUnityVersion)
    }

    @Test
    fun `should pre-populate buildProfile when exactly one profile found`() {
        val discoverer = UnityRunnerDiscoveryExtension()
        val unityVersion = "6000.0.0"
        val profilePath = "Assets/Settings/Build Profiles/Android.asset"

        val result = discoverer.discover(
            mockk(),
            mockk {
                every { root } returns createUnityProjectMock("", "UnityGame", unityVersion, listOf(profilePath))
            },
        )

        assertNotNull(result)
        result.size shouldBeExactly 1
        result.first().parameters shouldContain (UnityConstants.PARAM_BUILD_PROFILE to profilePath)
    }

    @Test
    fun `should leave buildProfile empty when multiple profiles found`() {
        val discoverer = UnityRunnerDiscoveryExtension()
        val unityVersion = "6000.0.0"
        val profiles = listOf(
            "Assets/Settings/Build Profiles/Android.asset",
            "Assets/Settings/Build Profiles/iOS.asset",
        )

        val result = discoverer.discover(
            mockk(),
            mockk {
                every { root } returns createUnityProjectMock("", "UnityGame", unityVersion, profiles)
            },
        )

        assertNotNull(result)
        result.size shouldBeExactly 1
        result.first().parameters shouldNotContainKey UnityConstants.PARAM_BUILD_PROFILE
    }

    @Test
    fun `should not set buildProfile when no profiles found`() {
        val discoverer = UnityRunnerDiscoveryExtension()
        val unityVersion = "6000.0.0"

        val result = discoverer.discover(
            mockk(),
            mockk {
                every { root } returns createUnityProjectMock("", "UnityGame", unityVersion, emptyList())
            },
        )

        assertNotNull(result)
        result.size shouldBeExactly 1
        result.first().parameters shouldNotContainKey UnityConstants.PARAM_BUILD_PROFILE
    }

    private fun DiscoveredObject.shouldBeUnityProject(path: String, associatedVersion: String) {
        type shouldBe UnityConstants.RUNNER_TYPE
        parameters shouldContain (UnityConstants.PARAM_PROJECT_PATH to path)
        parameters shouldContain (UnityConstants.PARAM_UNITY_VERSION to associatedVersion)
    }

    private fun createUnityProjectMock(
        containingDirectory: String,
        projectName: String,
        unityVersion: String,
        buildProfilePaths: List<String> = emptyList(),
    ): Element {
        val projectFullName = sequenceOf(containingDirectory, projectName)
            .filter { it.isNotEmpty() }
            .joinToString(separator = "/")

        val browser = mockk<Browser> {
            every { getElement(any()) } returns null
        }

        if (buildProfilePaths.isNotEmpty()) {
            val profileElements = buildProfilePaths.map { profilePath ->
                val profileName = profilePath.substringAfterLast("/")
                mockk<Element> {
                    every { name } returns profileName
                    every { isLeaf } returns true
                    every { isContentAvailable } returns true
                    every { inputStream } returns "BuildProfile:\n  data: test".byteInputStream()
                    every { getBrowser() } returns browser
                }
            }
            val conventionalElement = mockk<Element> {
                every { isLeaf } returns false
                every { children } returns profileElements
                every { getBrowser() } returns browser
            }
            val conventionalPath = if (projectFullName.isEmpty()) {
                "Assets/Settings/Build Profiles"
            } else {
                "$projectFullName/Assets/Settings/Build Profiles"
            }
            every { browser.getElement(conventionalPath) } returns conventionalElement
        }

        return mockk {
            every { fullName } returns projectFullName
            every { name } returns projectName
            every { isLeaf } returns false
            every { getBrowser() } returns browser
            every { children } returns listOf(
                mockk {
                    every { name } returns "Assets"
                    every { isLeaf } returns false
                    every { getBrowser() } returns browser
                    every { children } returns emptyList()
                },
                mockk {
                    every { name } returns "ProjectSettings"
                    every { isLeaf } returns false
                    every { getBrowser() } returns browser
                    every { children } returns listOf(
                        mockk {
                            every { name } returns "ProjectVersion.txt"
                            every { inputStream } returns """
                                m_EditorVersion: $unityVersion
                            """.trimIndent().byteInputStream()
                            every { isContentAvailable } returns true
                            every { isLeaf } returns true
                            every { getBrowser() } returns browser
                        },
                    )
                },
            )
        }
    }
}
