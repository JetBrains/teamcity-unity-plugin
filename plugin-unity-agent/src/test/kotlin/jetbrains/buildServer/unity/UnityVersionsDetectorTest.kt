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