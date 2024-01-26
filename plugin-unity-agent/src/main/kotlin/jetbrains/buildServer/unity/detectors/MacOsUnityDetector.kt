

package jetbrains.buildServer.unity.detectors

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.unity.UnityVersion
import jetbrains.buildServer.unity.UnityVersion.Companion.tryParseVersion
import org.apache.commons.configuration.plist.XMLPropertyListConfiguration
import java.io.File

class MacOsUnityDetector : UnityDetectorBase() {

    override val editorPath = "Unity.app/Contents/MacOS"
    override val editorExecutable = "Unity"
    override val appConfigDir = "$userHome/Library/Application support"

    override fun findInstallations() = sequence {
        getHintPaths().distinct().forEach { path ->
            val version = getVersionFromInstall(path) ?: return@forEach
            yield(version to path)
        }
    }

    override fun getHintPaths() = sequence {
        yieldAll(super.getHintPaths())
        yieldAll(findUnityPaths(File("/Applications")))
    }

    override fun getVersionFromInstall(editorRoot: File): UnityVersion? {
        LOG.debug("Looking for Unity installation in $editorRoot")
        val executable = getEditorPath(editorRoot)
        if (!executable.exists()) {
          LOG.debug("Cannot find $executable")
          return null
        }

        val plistFile = File(editorRoot, "Unity.app/Contents/Info.plist")
        if (!plistFile.exists()) {
          LOG.debug("Cannot find $plistFile")
          return null
        }

        val version = XMLPropertyListConfiguration(plistFile).getString("CFBundleVersion")
        return try {
            tryParseVersion(version)
        } catch (e: Exception) {
            LOG.infoAndDebugDetails("Invalid Unity version $version in directory $editorRoot", e)
            null
        }
    }
    companion object {
        private val LOG = Logger.getInstance(MacOsUnityDetector::class.java.name)
    }
}