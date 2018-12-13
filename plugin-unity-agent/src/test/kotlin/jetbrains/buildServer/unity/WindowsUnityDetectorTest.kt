package jetbrains.buildServer.unity

import com.intellij.openapi.util.SystemInfo
import org.testng.SkipException
import org.testng.annotations.Test

class WindowsUnityDetectorTest {

    @Test
    fun testFindInstallations() {
        if (!SystemInfo.isWindows) {
            throw SkipException("Non Windows platform")
        }

        val installations = WindowsUnityDetector().findInstallations()
        println(installations.joinToString("\n") { (version, path) ->
            "$version in $path"
        })
    }
}