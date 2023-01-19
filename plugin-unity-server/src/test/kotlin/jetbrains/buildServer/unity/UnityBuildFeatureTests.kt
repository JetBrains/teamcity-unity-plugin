package jetbrains.buildServer.unity

import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.web.openapi.PluginDescriptor
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UnityBuildFeatureTests {
    private val pluginDescriptorMock = mockk<PluginDescriptor> {
        every { getPluginResourcesPath(any()) } returns "foo"
    }

    @Test
    fun getRequirements_happyPath_generateUnityRequirement() {
        // arrange
        val sut = UnityBuildFeature(pluginDescriptorMock)

        val unityVersion = "2021.3.16"
        val parameters = mutableMapOf(
                UnityConstants.PARAM_UNITY_VERSION to unityVersion
        )

        // act
        val requirements = sut.getRequirements(parameters)

        // assert
        assertNotNull(requirements)
        assertEquals(1, requirements.size)
        val propertyString = requirements.first().propertyName.lowercase()
        assertContains(propertyString, "unity")
        assertContains(propertyString, "2021\\.3\\.16")
    }

    @Test
    fun getRequirements_versionNotSpecified_generateUnityRequirement() {
        // arrange
        val sut = UnityBuildFeature(pluginDescriptorMock)

        // act
        val requirements = sut.getRequirements(mutableMapOf())

        // assert
        assertNotNull(requirements)
        assertEquals(1, requirements.size)
        val propertyString = requirements.first().propertyName.lowercase()
        assertContains(propertyString, "unity")
    }
}