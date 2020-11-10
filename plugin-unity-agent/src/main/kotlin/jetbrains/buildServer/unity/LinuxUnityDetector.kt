/*
 * Copyright 2000-2020 JetBrains s.r.o.
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
import com.vdurmont.semver4j.Semver
import java.io.File

class LinuxUnityDetector : UnityDetectorBase() {

    override val editorPath = "Editor"
    override val editorExecutable = "Unity"
    override val appConfigDir = "$userHome/.config"

    override fun findInstallations() = sequence {
        getHintPaths().distinct().forEach { path ->
            val version = getVersionFromInstall(path) ?: return@forEach
            yield(version to path)
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

    override fun getVersionFromInstall(editorRoot: File): Semver? {
        LOG.debug("Looking for Unity installation in $editorRoot")
        val executable = getEditorPath(editorRoot)
        if (!executable.exists()) {
          LOG.debug("Cannot find $executable")
          return null
        }

        LOG.debug("Looking for package manager in $editorRoot")
        var version : String
        val packageVersions = File(editorRoot, "Editor/Data/PackageManager/Unity/PackageManager")
        if (packageVersions.exists()) {
            val versions = packageVersions.listFiles { file ->
                file.isDirectory
            } ?: return null

            if (versions.size != 1) {
                LOG.warn("Multiple Unity versions found in directory $editorRoot")
            }

            version = versions.first().name
        } else {
            version = editorRoot.name
        }

        // Unity version looks like that: 2017.1.1f1
        // where suffix could be the following:
        // * a  - alpha
        // * b  - beta
        // * p  - patch
        // * rc - release candidate
        // * f  - final
        version = version
                .split("a", "b", "p", "rc", "f")
                .firstOrNull()
                ?: return null

        return try {
            Semver(version, Semver.SemverType.LOOSE)
        } catch (e: Exception) {
            LOG.infoAndDebugDetails("Invalid Unity version $version in directory $editorRoot", e)
            null
        }
    }
    companion object {
        private val LOG = Logger.getInstance(LinuxUnityDetector::class.java.name)
    }
}