package jetbrains.buildServer.unity.detectors

import com.intellij.openapi.util.TCSystemInfo
import java.lang.UnsupportedOperationException

class UnityDetectorFactory {
    fun unityDetector(): UnityDetector = when {
        TCSystemInfo.isWindows -> WindowsUnityDetector(PEProductVersionDetector())
        TCSystemInfo.isMac -> MacOsUnityDetector()
        TCSystemInfo.isLinux -> LinuxUnityDetector()
        else -> throw UnsupportedOperationException("Unable to provide Unity detector because the operating system is unknown")
    }
}
