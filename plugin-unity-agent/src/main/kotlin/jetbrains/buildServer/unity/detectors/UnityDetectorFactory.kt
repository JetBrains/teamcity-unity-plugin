package jetbrains.buildServer.unity.detectors

import com.intellij.openapi.util.SystemInfo
import java.lang.UnsupportedOperationException

class UnityDetectorFactory {
    fun unityDetector(): UnityDetector = when {
        SystemInfo.isWindows -> WindowsUnityDetector(PEProductVersionDetector())
        SystemInfo.isMac -> MacOsUnityDetector()
        SystemInfo.isLinux -> LinuxUnityDetector()
        else -> throw UnsupportedOperationException("Unable to provide Unity detector because the operating system is unknown")
    }
}