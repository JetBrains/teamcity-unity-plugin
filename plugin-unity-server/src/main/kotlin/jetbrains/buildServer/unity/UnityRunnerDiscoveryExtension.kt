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

import jetbrains.buildServer.serverSide.BuildTypeSettings
import jetbrains.buildServer.serverSide.discovery.BuildRunnerDiscoveryExtension
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject
import jetbrains.buildServer.util.browser.Browser
import jetbrains.buildServer.util.browser.Element

/**
 * Performs unity build steps discovery.
 */
class UnityRunnerDiscoveryExtension : BuildRunnerDiscoveryExtension {

    private val depthLimit = 3

    override fun discover(settings: BuildTypeSettings, browser: Browser): MutableList<DiscoveredObject> {
        return discoverRunners(browser.root, 0).toMutableList()
    }

    private fun discoverRunners(currentElement: Element, currentElementDepth: Int)
            : Sequence<DiscoveredObject> = sequence {
        if (currentElementDepth > depthLimit || currentElement.name.contains("rule")) {
            return@sequence
        }

        val children = (currentElement.children?.filter { !it.isLeaf } ?: emptyList())
        val directories = children.map { it.name }.toSet()
        when {
            directories.containsAll(PROJECTS_DIRS) -> yield(DiscoveredObject(UnityConstants.RUNNER_TYPE, mapOf(
                    UnityConstants.PARAM_PROJECT_PATH to currentElement.fullName
            )))
            else -> {
                // Scan nested directories
                children.forEach {
                        yieldAll(discoverRunners(it, currentElementDepth + 1))
                }
            }
        }
    }

    companion object {
        val PROJECTS_DIRS = setOf("Assets", "ProjectSettings")
    }
}
