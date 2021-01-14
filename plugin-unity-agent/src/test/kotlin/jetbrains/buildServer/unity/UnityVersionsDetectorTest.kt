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

import com.intellij.openapi.util.SystemInfo
import org.testng.SkipException
import org.testng.annotations.Test

class UnityVersionsDetectorTest {

    @Test
    fun testFindInstallations() {
        val detector = when {
            SystemInfo.isWindows -> WindowsUnityDetector()
            SystemInfo.isMac -> MacOsUnityDetector()
            SystemInfo.isLinux -> LinuxUnityDetector()
            else -> null
        } ?: throw SkipException("Not supported platform")

        val installations = detector.findInstallations()
        println(installations.joinToString("\n") { (version, path) ->
            "$version in $path"
        })
    }
}