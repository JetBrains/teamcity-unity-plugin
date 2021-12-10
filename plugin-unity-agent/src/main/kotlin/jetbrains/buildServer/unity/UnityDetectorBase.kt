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

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.unity.unityhub.Editor
import jetbrains.buildServer.unity.unityhub.HubInfo
import jetbrains.buildServer.util.FileUtil
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.internal.StringSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.map
import java.io.File

abstract class UnityDetectorBase : UnityDetector {

    private val additionalHintPaths = mutableListOf<File>()
    protected val userHome = System.getProperty("user.home").trimEnd('/', '\\')

    protected abstract val editorPath: String
    protected abstract val editorExecutable: String
    protected abstract val appConfigDir: String

    override fun getEditorPath(directory: File) = File(directory, "$editorPath/$editorExecutable")

    fun registerAdditionalHintPath(hintPath: File) {
        additionalHintPaths += hintPath
    }

    protected open fun getHintPaths(): Sequence<File> = sequence {
        // Get paths from "UNITY_HOME" environment variables
        System.getenv(UnityConstants.VAR_UNITY_HOME)?.let { unityHome ->
            LOG.info("UNITY_HOME: $unityHome")
            if (unityHome.isEmpty()) return@let
            yieldAll(unityHome.split(File.pathSeparatorChar).map { path ->
                LOG.info("UNITY_HOME path: $path")
                File(path)
            })
        }

        // Get paths from "UNITY_HINT_PATH" environment variables
        System.getenv(UnityConstants.VAR_UNITY_HINT_PATH)?.let { unityHintPaths ->
            LOG.info("UNITY_HINT_PATH: $unityHintPaths")
            if (unityHintPaths.isEmpty()) return@let
            unityHintPaths.split(File.pathSeparatorChar).forEach { path ->
                LOG.info("UNITY_HINT_PATH path: $unityHintPaths")
                yieldAll(findUnityPaths(File(path)))
            }
        }

        // Get paths from "PATH" variable
        System.getenv("PATH")?.let { systemPath ->
            if (systemPath.isEmpty()) return@let
            systemPath.split(File.pathSeparatorChar).forEach { path ->
                if (path.endsWith(editorPath, true)) {
                    val envPath = File(path.removeRange(path.length - editorPath.length, path.length))
                    LOG.info("PATH: $envPath")
                    yield(envPath)
                }
            }
        }

        // Get paths from "additional directories"
        additionalHintPaths.forEach { hintPath ->
            LOG.info("Hint path: $hintPath")
            yieldAll(findUnityPaths(hintPath))
        }

        // Find Editors installed by Unity Hub
        yieldAll(findUnityHubEditors(appConfigDir))
    }

    protected fun findUnityPaths(directory: File) = sequence {
        LOG.info("Looking in: $directory")

        // The convention to install multiple Unity versions is
        // to use suffixes for Unity directory, e.g. Unity_4.0b7
        directory.listFiles { file ->
            file.isDirectory && file.name.startsWith("Unity", true)
        }?.let { files ->
            for (file in files) {
                LOG.info("Found: $file")
                yield(file)
            }
        }

        // Unity Hub installs editors under Unity/Hub/Editor directory,
        // e.g. Unity/Hub/Editor/2018.1.9f2
        val unityHub = File(directory, "Unity/Hub/Editor")
        LOG.info("Looking in: $unityHub")
        unityHub.listFiles { file ->
            file.isDirectory
        }?.let { files ->
            for (file in files) {
                LOG.info("Found: $file")
                yield(file)
            }
        }
    }

    private fun findUnityHubEditors(configDir: String) = sequence {
        val unityHub = File(configDir, "UnityHub")
        LOG.info("Looking in: $unityHub")
        if (!unityHub.exists()) {
            LOG.info("$unityHub does not exist.")
            return@sequence
        }

        // Enumerate Editors in Unity Hub directory
        tryParse(HubInfo.serializer(), File(unityHub, "hubInfo.json"))?.let { hubInfo ->
            val directory = FileUtil.getCanonicalFile(File(hubInfo.executablePath, "../Editor"))
            yieldAll(listDirectories(directory))
        }

        // Enumerate installed Editors
        tryParse(mapSerializer, File(unityHub, "editors.json"))?.let { editors ->
            editors.values.forEach { editor ->
                editor.location?.let { locations ->
                    yieldAll(locations.map { location ->
                        val directory = FileUtil.getCanonicalFile(File(location, "../..") )
                        LOG.info("Found: $directory")
                        directory
                    })
                }
            }
        }

        // Enumerate editors in secondary installation path
        val secondaryInstallPath = try {
            val installPath = File(unityHub, "secondaryInstallPath.json")
            unquoteString(installPath.readText())
        } catch (e: Exception) {
            LOG.debug("Unable to read secondary Editor location path", e)
            null
        }
        if (!secondaryInstallPath.isNullOrEmpty()) {
            yieldAll(listDirectories(File(secondaryInstallPath)))
        }
    }

    private fun unquoteString(text: String): String {
        return if (text.startsWith('"') && text.endsWith('"')) {
            text.substring(1, text.length - 1)
        } else text
    }

    private fun <T> tryParse(deserializer: DeserializationStrategy<T>, file: File): T? {
        if (!file.exists()) return null
        LOG.debug("Reading Unity Hub configuration file $file")
        return try {
            parser.parse(deserializer, file.readText())
        } catch (e: Exception) {
            LOG.debug("Unable to parse file $file", e)
            null
        }
    }

    private fun listDirectories(directory: File) = sequence {
        LOG.info("Listing directories under $directory")
        directory.listFiles { file ->
            file.isDirectory
        }?.let { directories ->
            for (directory in directories) {
                LOG.info("Found: $directory")
                yield(directory)
            }
            yieldAll(directories.asSequence())
        }
    }

    companion object {
        private val LOG = Logger.getInstance(UnityDetectorBase::class.java.name)
        private val parser = Json.nonstrict
        private val mapSerializer = (StringSerializer to Editor.serializer()).map
    }
}
