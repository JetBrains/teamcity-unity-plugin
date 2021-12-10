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

package jetbrains.buildServer.unity

import jetbrains.buildServer.serverSide.BuildFeature
import jetbrains.buildServer.web.openapi.PluginDescriptor

class UnityBuildFeature(descriptor: PluginDescriptor) : BuildFeature() {

    private val _editUrl: String = descriptor.getPluginResourcesPath("editBuildFeature.jsp")

    override fun getType() = UnityConstants.BUILD_FEATURE_TYPE

    override fun getDisplayName() = UnityConstants.BUILD_FEATURE_DISPLAY_NAME

    override fun getEditParametersUrl() = _editUrl

    override fun isMultipleFeaturesPerBuildTypeAllowed() = false

    override fun describeParameters(parameters: Map<String, String>): String {
        val builder = StringBuilder()
        parameters[UnityConstants.PARAM_ACTIVATE_LICENSE]?.let {
            if (it.toBoolean()) {
                builder.append("Activate Unity license: ON\n")
            }
        }
        parameters[UnityConstants.PARAM_CACHE_SERVER]?.let {
            if (it.isNotEmpty()) {
                builder.append("Use cache server: ${it.trim()}\n")
            }
        }
        return builder.toString().trim()
    }
}