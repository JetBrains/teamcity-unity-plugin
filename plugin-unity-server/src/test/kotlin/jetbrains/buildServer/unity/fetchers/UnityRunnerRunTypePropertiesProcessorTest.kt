/*
 * Copyright 2020 Aaron Zurawski
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

import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.unity.UnityConstants
import jetbrains.buildServer.unity.UnityRunnerRunTypePropertiesProcessor
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class UnityRunnerRunTypePropertiesProcessorTest {

    @DataProvider
    fun processData(): Array<Array<Any?>> {
        return arrayOf(
                arrayOf<Any?>(
                        emptyMap<String, String>(),
                        listOf<InvalidProperty>()
                ),
                arrayOf<Any?>(
                        mutableMapOf(UnityConstants.PARAM_UNITY_ROOT to "C:\\Program Files\\Unity\\2018.4.9f1"),
                        listOf<InvalidProperty>()
                ),
                arrayOf<Any?>(
                        mutableMapOf(UnityConstants.PARAM_UNITY_ROOT to ""),
                        listOf<InvalidProperty>()
                ),
                arrayOf<Any?>(
                        mutableMapOf(UnityConstants.PARAM_DETECTION_MODE to UnityConstants.DETECTION_MODE_MANUAL,
                                UnityConstants.PARAM_UNITY_ROOT to "C:\\Program Files\\Unity\\2018.4.9f1"),
                        listOf<InvalidProperty>()
                ),
                arrayOf<Any?>(
                        mutableMapOf(UnityConstants.PARAM_DETECTION_MODE to UnityConstants.DETECTION_MODE_MANUAL,
                                UnityConstants.PARAM_UNITY_ROOT to ""),
                        listOf(InvalidProperty(UnityConstants.PARAM_UNITY_ROOT, ""))
                ),
                arrayOf<Any?>(
                        mutableMapOf(UnityConstants.PARAM_DETECTION_MODE to UnityConstants.DETECTION_MODE_AUTO,
                                UnityConstants.PARAM_UNITY_ROOT to "C:\\Program Files\\Unity\\2018.4.9f1"),
                        listOf<InvalidProperty>()
                ),
                arrayOf<Any?>(
                        mutableMapOf(UnityConstants.PARAM_DETECTION_MODE to UnityConstants.DETECTION_MODE_AUTO,
                                UnityConstants.PARAM_UNITY_ROOT to ""),
                        listOf<InvalidProperty>()
                )
        )
    }

    @Test(dataProvider = "processData")
    fun testProcess(parameters: MutableMap<String, String>, expectedInvalidProperties: List<InvalidProperty>) {
        val propertiesProcessorType = UnityRunnerRunTypePropertiesProcessor()

        val invalidProperties = propertiesProcessorType.process(parameters)

        Assert.assertEquals(invalidProperties.count(), expectedInvalidProperties.count())

        val invalidPropIter = invalidProperties.iterator()
        val expectedInvalidPropIter = expectedInvalidProperties.iterator()

        while (invalidPropIter.hasNext() && expectedInvalidPropIter.hasNext()) {
           Assert.assertEquals(invalidPropIter.next().propertyName, expectedInvalidPropIter.next().propertyName)
        }
    }
}