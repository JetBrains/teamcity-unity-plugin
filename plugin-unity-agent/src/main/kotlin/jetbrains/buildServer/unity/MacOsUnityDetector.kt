package jetbrains.buildServer.unity

import com.github.zafarkhaja.semver.Version
import com.intellij.openapi.diagnostic.Logger
import java.io.File
import org.apache.commons.configuration.plist.XMLPropertyListConfiguration
import java.lang.Exception

class MacOsUnityDetector : UnityDetectorBase() {

    override fun findInstallations() = sequence {
        getHintPaths().forEach { path ->
            val executable = getEditorPath(path)
            if (!executable.exists()) return@forEach

            val plistFile = File(path, "Unity.app/Contents/Info.plist")
            if (!plistFile.exists()) return@forEach
            val config = XMLPropertyListConfiguration(plistFile)

            // Unity version looks like that: 2017.1.1f1
            // where suffix could be the following:
            // * a  - alpha
            // * b  - beta
            // * p  - patch
            // * rc - release candidate
            // * f  - final
            val version = config.getString("CFBundleVersion")
                    ?.split("a", "b", "p", "rc", "f")
                    ?.firstOrNull()
                    ?: return@forEach
            try {
                yield(Version.valueOf(version) to path)
            } catch (e: Exception) {
                LOG.infoAndDebugDetails("Invalid Unity version $version in directory $path", e)
            }
        }
    }

    override fun getEditorPath(directory: File) = File(directory, "Unity.app/Contents/MacOS/Unity")

    private fun getHintPaths() = findUnityPaths(File("/Applications"))

    companion object {
        private val LOG = Logger.getInstance(MacOsUnityDetector::class.java.name)
    }
}