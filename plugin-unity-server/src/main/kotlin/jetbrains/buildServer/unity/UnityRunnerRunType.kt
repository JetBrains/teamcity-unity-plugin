/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.unity

import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.RunType
import jetbrains.buildServer.serverSide.RunTypeRegistry
import jetbrains.buildServer.web.openapi.PluginDescriptor

/**
 * Cargo runner definition.
 */
class UnityRunnerRunType(private val myPluginDescriptor: PluginDescriptor,
                         runTypeRegistry: RunTypeRegistry) : RunType() {

    init {
        runTypeRegistry.registerRunType(this)
    }

    override fun getType(): String {
        return UnityConstants.RUNNER_TYPE
    }

    override fun getDisplayName(): String {
        return UnityConstants.RUNNER_DISPLAY_NAME
    }

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
        return "unity"
    }
}
