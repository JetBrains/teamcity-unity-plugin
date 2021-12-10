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

import jetbrains.buildServer.serverSide.Parameter
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SimpleParameter
import jetbrains.buildServer.serverSide.parameters.types.PasswordsProvider

class UnityPasswordsProvider : PasswordsProvider {

    override fun getPasswordParameters(build: SBuild): List<Parameter> {
        val feature = build.getBuildFeaturesOfType(UnityConstants.BUILD_FEATURE_TYPE).firstOrNull()?:return emptyList()

        val parameters = mutableListOf<Parameter>()

        UnityConstants.PARAM_SERIAL_NUMBER.apply {
            feature.parameters[this]?.let {
                parameters.add(SimpleParameter(this, it.trim()))
            }
        }
        UnityConstants.PARAM_PASSWORD.apply {
            feature.parameters[this]?.let {
                parameters.add(SimpleParameter(this, it.trim()))
            }
        }

        return parameters
    }
}