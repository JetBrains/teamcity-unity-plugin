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

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.serverSide.discovery.BreadthFirstRunnerDiscoveryExtension
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject
import jetbrains.buildServer.util.browser.Element

private data class DiscoveredUnityProject(
    val path: String,
    val unityVersion: UnityVersion? = null,
): DiscoveredObject(UnityConstants.RUNNER_TYPE, buildMap {
    put(UnityConstants.PARAM_PROJECT_PATH, path)

    unityVersion?.let {
        put(UnityConstants.PARAM_UNITY_VERSION, unityVersion.toString())
    }
})

class UnityRunnerDiscoveryExtension : BreadthFirstRunnerDiscoveryExtension(DEPTH_LIMIT) {
    companion object {
        private const val DEPTH_LIMIT = 3
        private const val PROJECT_SETTINGS_DIR = "ProjectSettings"
        private val PROJECTS_DIRS = listOf("Assets", PROJECT_SETTINGS_DIR)
        private val logger = Logger.getInstance(UnityRunnerDiscoveryExtension::class.java.name)
    }

    override fun discoverRunnersInDirectory(
        dir: Element,
        filesAndDirs: MutableList<Element>
    ): MutableList<DiscoveredObject> {
        if (!dir.isUnityProjectDirectory()) {
            logger.debug("Directory: ${dir.fullName} seems not to be a Unity project directory, skipping")
            return mutableListOf()
        }

        val unityVersion = tryToFindAssociatedUnityVersionForProject(dir)

        logger.info("Unity project was found in directory '${dir.fullName}'${if (unityVersion == null) "" else ", associated Unity version: '$unityVersion'"}")
        return mutableListOf(DiscoveredUnityProject(dir.fullName, unityVersion))
    }

    private fun tryToFindAssociatedUnityVersionForProject(projectDir: Element): UnityVersion? = projectDir.children?.let { children ->
        val projectSettingsDir = children.first { it.name == PROJECT_SETTINGS_DIR }

        projectSettingsDir.children
            ?.filter { it.isContentAvailable }
            ?.firstOrNull { it.name == "ProjectVersion.txt" }
            ?.let {
                it.inputStream
                    .bufferedReader()
                    .useLines { lines ->
                        for (line in lines ) {
                            val keyValue = line.split(" ")
                            if (keyValue.size != 2) {
                                continue
                            }

                            if (keyValue.first() == "m_EditorVersion:") {
                                return@useLines UnityVersion.tryParseVersion(keyValue.last())
                            }
                        }
                        null
                    }
            }
    }

    private fun Element.isUnityProjectDirectory(): Boolean =
        children
            ?.filter { !it.isLeaf }
            ?.map { it.name }
            ?.containsAll(PROJECTS_DIRS)
            ?: false
}
