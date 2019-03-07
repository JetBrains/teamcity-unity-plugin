/*
 * Copyright 2000-2019 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.unity

import com.intellij.openapi.diagnostic.Logger
import com.vdurmont.semver4j.Semver
import jetbrains.buildServer.util.PEReader.PEUtil
import java.io.File

class WindowsUnityDetector : UnityDetectorBase() {

    override val editorPath = "Editor"
    override val editorExecutable = "Unity.exe"
    override val appConfigDir = "$userHome/AppData/Roaming"

    override fun findInstallations() = sequence {
        getHintPaths().distinct().forEach { path ->
            LOG.debug("Looking for Unity installation in $path")

            val executable = getEditorPath(path)
            if (!executable.exists()) return@forEach

            val version = PEUtil.getProductVersion(executable) ?: return@forEach
            yield(Semver("${version.p1}.${version.p2}.${version.p3}", Semver.SemverType.LOOSE) to path)
        }
    }

    override fun getHintPaths() = sequence {
        yieldAll(super.getHintPaths())

        val programFiles = hashSetOf<String>()

        System.getenv("ProgramFiles")?.let { programFiles.add(it) }
        System.getenv("ProgramFiles(X86)")?.let { programFiles.add(it) }
        System.getenv("ProgramW6432")?.let { programFiles.add(it) }

        programFiles.forEach { path ->
            if (path.isEmpty()) return@forEach
            yieldAll(findUnityPaths(File(path)))
        }
    }

    companion object {
        private val LOG = Logger.getInstance(WindowsUnityDetector::class.java.name)
    }
}
