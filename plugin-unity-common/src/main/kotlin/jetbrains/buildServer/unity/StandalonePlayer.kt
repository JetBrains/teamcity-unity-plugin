

package jetbrains.buildServer.unity

enum class StandalonePlayer(val id: String, val description: String) {
    Linux32Player("buildLinux32Player", "Linux 32-bit"),
    Linux64Player("buildLinux64Player", "Linux 64-bit"),
    LinuxUniversalPlayer("buildLinuxUniversalPlayer", "Linux 32-bit and 64-bit"),
    OSXPlayer("buildOSXPlayer", "Mac OSX 32-bit"),
    OSX64Player("buildOSX64Player", "Mac OSX 64-bit"),
    OSXUniversalPlayer("buildOSXUniversalPlayer", "Mac OSX 32-bit and 64-bit"),
    WindowsPlayer("buildWindowsPlayer", "Windows 32-bit"),
    Windows64Player("buildWindows64Player", "Windows 64-bit"),
    ;

    companion object {
        fun tryParse(id: String): StandalonePlayer? {
            return StandalonePlayer.values().singleOrNull { it.id.equals(id, true) }
        }
    }
}
