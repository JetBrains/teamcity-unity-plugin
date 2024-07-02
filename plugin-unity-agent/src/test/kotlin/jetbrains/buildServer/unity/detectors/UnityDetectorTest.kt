

package jetbrains.buildServer.unity.detectors

import com.intellij.openapi.util.TCSystemInfo
import org.testng.SkipException
import kotlin.test.Test

class UnityDetectorTest {

    @Test
    fun `print found installations`() {
        val detector = when {
            TCSystemInfo.isWindows -> WindowsUnityDetector(PEProductVersionDetector())
            TCSystemInfo.isMac -> MacOsUnityDetector()
            TCSystemInfo.isLinux -> LinuxUnityDetector()
            else -> null
        } ?: throw SkipException("Not supported platform")

        val installations = detector.findInstallations()
        println(
            installations.joinToString("\n") { (version, path) ->
                "$version in $path"
            },
        )
    }
}
