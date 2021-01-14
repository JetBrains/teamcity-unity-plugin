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

enum class StandalonePlayer(val id: String, val description: String) {
    Linux32Player("buildLinux32Player", "Linux 32-bit"),
    Linux64Player("buildLinux64Player", "Linux 64-bit"),
    LinuxUniversalPlayer("buildLinuxUniversalPlayer", "Linux 32-bit and 64-bit"),
    OSXPlayer("buildOSXPlayer", "Mac OSX 32-bit"),
    OSX64Player("buildOSX64Player", "Mac OSX 64-bit"),
    OSXUniversalPlayer("buildOSXUniversalPlayer", "Mac OSX 32-bit and 64-bit"),
    WindowsPlayer("buildWindowsPlayer", "Windows 32-bit"),
    Windows64Player("buildWindows64Player", "Windows 64-bit");

    companion object {
        fun tryParse(id: String): StandalonePlayer? {
            return StandalonePlayer.values().singleOrNull { it.id.equals(id, true) }
        }
    }
}