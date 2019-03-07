/*
 * Copyright 2000-2019 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.unity

import com.intellij.openapi.diagnostic.Logger
import com.vdurmont.semver4j.Semver
import java.io.File

class LinuxUnityDetector : UnityDetectorBase() {

    override val editorPath = "Editor"
    override val editorExecutable = "Unity"
    override val appConfigDir = "$userHome/.config"

    override fun findInstallations() = sequence {
        getHintPaths().distinct().forEach { path ->
            LOG.debug("Looking for Unity installation in $path")

            val executable = getEditorPath(path)
            if (!executable.exists()) return@forEach

            LOG.debug("Looking for package manager in $path")
            val packageVersions = File(path, "Editor/Data/PackageManager/Unity/PackageManager")
            if (!packageVersions.exists()) return@forEach

            val versions = packageVersions.listFiles { file ->
                file.isDirectory
            } ?: return@forEach

            if (versions.size != 1) {
                LOG.warn("Multiple Unity versions found in directory $path")
            }

            val version = versions.first().name
            try {
                yield(Semver(version, Semver.SemverType.LOOSE) to path)
            } catch (e: Exception) {
                LOG.infoAndDebugDetails("Invalid Unity version $version in directory $path", e)
            }
        }
    }

    override fun getHintPaths() = sequence {
        yieldAll(super.getHintPaths())

        // Find installations within user profile
        System.getProperty("user.home")?.let { userHome ->
            if (userHome.isNotEmpty()) {
                yieldAll(findUnityPaths(File(userHome)))
            }
        }

        // deb packages are installing Unity in the /opt/Unity directory
        yieldAll(findUnityPaths(File("/opt/")))
    }

    companion object {
        private val LOG = Logger.getInstance(LinuxUnityDetector::class.java.name)
    }
}