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