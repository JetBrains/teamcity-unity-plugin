

package jetbrains.buildServer.unity.detectors

import com.intellij.openapi.util.SystemInfo
import org.testng.SkipException
import kotlin.test.Test

class UnityDetectorTest {

    @Test
    fun `print found installations`() {
        val detector = when {
            SystemInfo.isWindows -> WindowsUnityDetector(PEProductVersionDetector())
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