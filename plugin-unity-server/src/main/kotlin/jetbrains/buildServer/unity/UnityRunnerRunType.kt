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

import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.RunType
import jetbrains.buildServer.serverSide.RunTypeRegistry
import jetbrains.buildServer.web.openapi.PluginDescriptor

/**
 * Cargo runner definition.
 */
class UnityRunnerRunType(private val myPluginDescriptor: PluginDescriptor,
                         private val myRunTypeRegistry: RunTypeRegistry) : RunType() {

    private val myDisplayName: String by lazy {
        val runType = myRunTypeRegistry.findRunType("unityRunner")
        if (runType == null) {
            UnityConstants.RUNNER_DISPLAY_NAME
        } else {
            "${UnityConstants.RUNNER_DISPLAY_NAME} (JetBrains plugin)"
        }
    }

    init {
        myRunTypeRegistry.registerRunType(this)
    }

    override fun getType(): String {
        return UnityConstants.RUNNER_TYPE
    }

    override fun getDisplayName() = myDisplayName

    override fun getDescription(): String {
        return UnityConstants.RUNNER_DESCRIPTION
    }

    override fun getRunnerPropertiesProcessor(): PropertiesProcessor? {
        return PropertiesProcessor { emptyList() }
    }

    override fun getEditRunnerParamsJspFilePath(): String? {
        return myPluginDescriptor.getPluginResourcesPath("editUnityParameters.jsp")
    }

    override fun getViewRunnerParamsJspFilePath(): String? {
        return myPluginDescriptor.getPluginResourcesPath("viewUnityParameters.jsp")
    }

    override fun getDefaultRunnerProperties(): Map<String, String>? {
        return emptyMap()
    }

    override fun describeParameters(parameters: Map<String, String>): String {
        val builder = StringBuilder()
        parameters[UnityConstants.PARAM_PROJECT_PATH]?.let {
            if (it.isNotBlank()) {
                builder.addParameter("Project path: $it")
            }
        }
        parameters[UnityConstants.PARAM_EXECUTE_METHOD]?.let {
            if (it.isNotBlank()) {
                builder.addParameter("Execute method: $it")
            }
        }
        parameters[UnityConstants.PARAM_BUILD_TARGET]?.let {
            if (it.isNotBlank()) {
                builder.addParameter("Build target: $it")
            }
        }
        parameters[UnityConstants.PARAM_BUILD_PLAYER]?.let { value ->
            StandalonePlayer.tryParse(value)?.let {
                builder.addParameter("Build player: ${it.description}")
            }
        }
        parameters[UnityConstants.PARAM_RUN_EDITOR_TESTS]?.let {
            if (it.toBoolean()) {
                builder.addParameter("Run editor tests: ON")
            }
        }
        return builder.toString().trim()
    }

    override fun getRunnerSpecificRequirements(parameters: Map<String, String>): List<Requirement> {
        val requirements = mutableListOf<Requirement>()
        parameters[UnityConstants.PARAM_UNITY_VERSION]?.let {
            if (it.isNotBlank()) {
                val name = escapeRegex(UnityConstants.UNITY_CONFIG_NAME) + escapeRegex(it.trim()) + ".*"
                requirements.add(Requirement(RequirementQualifier.EXISTS_QUALIFIER + name, null, RequirementType.EXISTS))
            }
        }
        if (requirements.isEmpty()) {
            val name = escapeRegex(UnityConstants.UNITY_CONFIG_NAME) + ".+"
            requirements.add(Requirement(RequirementQualifier.EXISTS_QUALIFIER + name, null, RequirementType.EXISTS))
        }
        return requirements
    }

    private fun escapeRegex(value: String) =
        if(value.contains('%')) value else value.replace(".", "\\.")

    private fun StringBuilder.addParameter(parameter: String) {
        if (this.isNotEmpty()) {
            append(" $parameter")
        } else {
            append(parameter)
        }
        append("\n")
    }
}
