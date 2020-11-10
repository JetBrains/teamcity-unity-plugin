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
import org.apache.commons.configuration.plist.XMLPropertyListConfiguration
import java.io.File

class MacOsUnityDetector : UnityDetectorBase() {

    override val editorPath = "Unity.app/Contents/MacOS"
    override val editorExecutable = "Unity"
    override val appConfigDir = "$userHome/Library/Application support"

    override fun findInstallations() = sequence {
        getHintPaths().distinct().forEach { path ->
            val version = getVersionFromInstall(path) ?: return@forEach
            yield(version to path)
        }
    }

    override fun getHintPaths() = sequence {
        yieldAll(super.getHintPaths())
        yieldAll(findUnityPaths(File("/Applications")))
    }

    override fun getVersionFromInstall(editorRoot: File): Semver? {
        LOG.debug("Looking for Unity installation in $editorRoot")
        val executable = getEditorPath(editorRoot)
        if (!executable.exists()) {
          LOG.debug("Cannot find $executable")
          return null
        }

        val plistFile = File(editorRoot, "Unity.app/Contents/Info.plist")
        if (!plistFile.exists()) {
          LOG.debug("Cannot find $plistFile")
          return null
        }
      
        val config = XMLPropertyListConfiguration(plistFile)

        // Unity version looks like that: 2017.1.1f1
        // where suffix could be the following:
        // * a  - alpha
        // * b  - beta
        // * p  - patch
        // * rc - release candidate
        // * f  - final
        val version = config.getString("CFBundleVersion")
                ?.split("a", "b", "p", "rc", "f")
                ?.firstOrNull()
                ?: return null

        return try {
            Semver(version, Semver.SemverType.LOOSE)
        } catch (e: Exception) {
            LOG.infoAndDebugDetails("Invalid Unity version $version in directory $editorRoot", e)
            null
        }
    }
    companion object {
        private val LOG = Logger.getInstance(MacOsUnityDetector::class.java.name)
    }
}