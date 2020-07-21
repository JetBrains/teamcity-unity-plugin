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

package jetbrains.buildServer.unity

import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.util.PropertiesUtil

class UnityRunnerRunTypePropertiesProcessor  : PropertiesProcessor {
    override fun process(properties: MutableMap<String, String>?): MutableCollection<InvalidProperty> {
        val invalidProperties = ArrayList<InvalidProperty>()

        if(properties?.get(UnityConstants.PARAM_DETECTION_MODE).equals(UnityConstants.DETECTION_MODE_MANUAL)) {
            val unityRoot = properties?.get(UnityConstants.PARAM_UNITY_ROOT)
            if(PropertiesUtil.isEmptyOrNull(unityRoot)) {
                invalidProperties.add(InvalidProperty(UnityConstants.PARAM_UNITY_ROOT, "Unity version must be specified"))
            }
        }

        return invalidProperties
    }
}