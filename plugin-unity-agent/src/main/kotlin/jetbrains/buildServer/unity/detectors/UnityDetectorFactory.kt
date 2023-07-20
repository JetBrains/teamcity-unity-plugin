package jetbrains.buildServer.unity.detectors

import com.intellij.openapi.util.SystemInfo

class UnityDetectorFactory {
    fun unityDetector(): UnityDetector? = when {
        SystemInfo.isWindows -> WindowsUnityDetector(PEProductVersionDetector())
        SystemInfo.isMac -> MacOsUnityDetector()
        SystemInfo.isLinux -> LinuxUnityDetector()
        else -> null
    }
}