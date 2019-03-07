/*
 * Copyright 2000-2019 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.unity

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.unity.unityhub.Editor
import jetbrains.buildServer.unity.unityhub.HubInfo
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.internal.StringSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.map
import java.io.File

abstract class UnityDetectorBase : UnityDetector {

    private val additionalHintPaths = mutableListOf<File>()

    protected abstract val editorPath: String
    protected abstract val editorExecutable: String
    protected abstract val appConfigDir: String?

    override fun getEditorPath(directory: File) = File(directory, "$editorPath/$editorExecutable")

    fun registerAdditionalHintPath(hintPath: File) {
        additionalHintPaths += hintPath
    }

    protected open fun getHintPaths(): Sequence<File> = sequence {
        // Get paths from "UNITY_HOME" environment variables
        System.getenv(UnityConstants.VAR_UNITY_HOME)?.let { unityHome ->
            if (unityHome.isEmpty()) return@let
            yieldAll(unityHome.split(File.pathSeparatorChar).map { path ->
                File(path)
            })
        }

        // Get paths from "UNITY_HINT_PATH" environment variables
        System.getenv(UnityConstants.VAR_UNITY_HINT_PATH)?.let { unityHintPaths ->
            if (unityHintPaths.isEmpty()) return@let
            unityHintPaths.split(File.pathSeparatorChar).forEach { path ->
                yieldAll(findUnityPaths(File(path)))
            }
        }

        // Get paths from "PATH" variable
        System.getenv("PATH")?.let { systemPath ->
            if (systemPath.isEmpty()) return@let
            systemPath.split(File.pathSeparatorChar).forEach { path ->
                if (path.endsWith(editorPath, true)) {
                    yield(File(path.removeRange(path.length - editorPath.length, path.length)))
                }
            }
        }

        // Get paths from "additional directories"
        additionalHintPaths.forEach { hintPath ->
            yieldAll(findUnityPaths(hintPath))
        }

        // Find Editors installed by Unity Hub
        appConfigDir?.let {
            yieldAll(findUnityHubEditors(it))
        }
    }

    protected fun findUnityPaths(directory: File) = sequence {
        // The convention to install multiple Unity versions is
        // to use suffixes for Unity directory, e.g. Unity_4.0b7
        directory.listFiles { file ->
            file.isDirectory && file.name.startsWith("Unity", true)
        }?.let { files ->
            yieldAll(files.asSequence())
        }

        // Unity Hub installs editors under Unity/Hub/Editor directory,
        // e.g. Unity/Hub/Editor/2018.1.9f2
        val unityHub = File(directory, "Unity/Hub/Editor")
        unityHub.listFiles { file ->
            file.isDirectory
        }?.let { files ->
            yieldAll(files.asSequence())
        }
    }

    private fun findUnityHubEditors(configDir: String) = sequence {
        val unityHub = File(configDir, "UnityHub")
        if (!unityHub.exists()) return@sequence

        // Enumerate Editors in Unity Hub directory
        tryParse(HubInfo.serializer(), File(unityHub, "hubInfo.json"))?.let { hubInfo ->
            yieldAll(listDirectories(File(hubInfo.executablePath, "../Editor")))
        }

        // Enumerate installed Editors
        tryParse(mapSerializer, File(unityHub, "editors.json"))?.let { editors ->
            editors.values.forEach { editor ->
                editor.location?.let { locations ->
                    yieldAll(locations.map { location -> File(location, "../..") })
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
        LOG.debug("Listing directories under $directory")
        directory.listFiles { file ->
            file.isDirectory
        }?.let { directories ->
            yieldAll(directories.asSequence())
        }
    }

    companion object {
        private val LOG = Logger.getInstance(UnityDetectorBase::class.java.name)
        private val parser = Json.nonstrict
        private val mapSerializer = (StringSerializer to Editor.serializer()).map
    }
}
