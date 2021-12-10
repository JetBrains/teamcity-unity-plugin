/*
 * Copyright 2000-2021 JetBrains s.r.o.
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

package jetbrains.buildServer.unity.fetchers

import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.RunTypeRegistry
import jetbrains.buildServer.unity.UnityConstants
import jetbrains.buildServer.unity.UnityRunnerRunType
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class UnityRunnerRunTypeTest {

    @DataProvider
    fun runnerRequirementsData(): Array<Array<Any?>> {
        return arrayOf(
                arrayOf<Any?>(
                        emptyMap<String, String>(),
                        emptyList<Requirement>()
                ),
                arrayOf<Any?>(
                        mapOf(UnityConstants.PARAM_DETECTION_MODE to UnityConstants.DETECTION_MODE_AUTO,
                                UnityConstants.PARAM_UNITY_VERSION to ""),
                        listOf(Requirement("Exists=>unity\\.path\\..+", null, RequirementType.EXISTS))
                ),
                arrayOf<Any?>(
                        mapOf(UnityConstants.PARAM_DETECTION_MODE to UnityConstants.DETECTION_MODE_MANUAL,
                                UnityConstants.PARAM_UNITY_ROOT to "C:\\My\\Custom\\Unity"),
                        emptyList<Requirement>()
                ),
                arrayOf<Any?>(
                        mapOf(UnityConstants.PARAM_UNITY_VERSION to "2018.2"),
                        listOf(Requirement("Exists=>unity\\.path\\.2018\\.2.*", null, RequirementType.EXISTS))
                ),
                arrayOf<Any?>(
                        mapOf(UnityConstants.PARAM_UNITY_VERSION to "%SOME_VAR.1%"),
                        listOf(Requirement("Exists=>unity\\.path\\.%SOME_VAR.1%.*", null, RequirementType.EXISTS))
                ),
                arrayOf<Any?>(
                        mapOf(UnityConstants.PARAM_DETECTION_MODE to UnityConstants.DETECTION_MODE_MANUAL,
                                UnityConstants.PARAM_UNITY_VERSION to "2018.2"),
                        emptyList<Requirement>()
                ),
                arrayOf<Any?>(
                        mapOf(UnityConstants.PARAM_DETECTION_MODE to UnityConstants.DETECTION_MODE_AUTO,
                                UnityConstants.PARAM_UNITY_VERSION to "2018.2"),
                        listOf(Requirement("Exists=>unity\\.path\\.2018\\.2.*", null, RequirementType.EXISTS))
                )
        )
    }

    @DataProvider
    fun runnerDefaultData(): Array<Array<Any?>> {
        return arrayOf(
                arrayOf<Any?>(
                    mapOf(UnityConstants.PARAM_DETECTION_MODE to UnityConstants.DETECTION_MODE_AUTO)
                )
        )
    }

    @Test(dataProvider = "runnerRequirementsData")
    fun testRunnerRequirements(parameters: Map<String, String>, expectedRequirements: List<Requirement>) {
        val m = Mockery()
        val pluginDescriptor = m.mock(PluginDescriptor::class.java)
        val runTypeRegistry = m.mock(RunTypeRegistry::class.java)

        m.checking(object : Expectations() {
            init {
                oneOf(runTypeRegistry).registerRunType(with(any(UnityRunnerRunType::class.java)))
            }
        })

        val runType = UnityRunnerRunType(pluginDescriptor, runTypeRegistry)

        val requirements = runType.getRunnerSpecificRequirements(parameters)
        Assert.assertEquals(requirements, expectedRequirements)
    }

    @Test(dataProvider = "runnerDefaultData")
    fun testDefaultParameters(expectedParameters: Map<String, String>) {
        val m = Mockery()
        val pluginDescriptor = m.mock(PluginDescriptor::class.java)
        val runTypeRegistry = m.mock(RunTypeRegistry::class.java)

        m.checking(object : Expectations() {
            init {
                oneOf(runTypeRegistry).registerRunType(with(any(UnityRunnerRunType::class.java)))
            }
        })

        val runType = UnityRunnerRunType(pluginDescriptor, runTypeRegistry)

        val defaultParameters = runType.defaultRunnerProperties
        Assert.assertEquals(defaultParameters, expectedParameters)
    }
}