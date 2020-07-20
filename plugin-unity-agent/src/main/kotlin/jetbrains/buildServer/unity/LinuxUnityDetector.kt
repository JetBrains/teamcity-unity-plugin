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
            LOG.debug("Looking for Unity installation in $path")

            val executable = getEditorPath(path)
            if (!executable.exists()) return@forEach                                                       
            
            // Use last segment (version)
            val version = path.name
                                           
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
