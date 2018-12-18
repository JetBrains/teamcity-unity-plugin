package jetbrains.buildServer.unity

import com.github.zafarkhaja.semver.Version
import com.intellij.openapi.diagnostic.Logger
import java.io.File
import org.apache.commons.configuration.plist.XMLPropertyListConfiguration
import java.lang.Exception

class MacOsUnityDetector : UnityDetector {

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
                    ?.split("[abcfpr]")
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

    private fun getHintPaths() = sequence {
        val directory = File("/Applications")

        // The convention to install multiple Unity versions is
        // to use suffixes for Unity directory, e.g. Unity_4.0b7
        directory.listFiles { file ->
            file.isDirectory && file.name.startsWith("Unity")
        }?.let { files ->
            yieldAll(files.asSequence())
        }

        // Unity Hub installs editors under Unity/Hub/Editor directory,
        // e.g. Unity/Hub/Editor/2018.1.9f2
        val unityHub = File(directory, "Unity/Hub/Editor")
        unityHub.listFiles { file ->
            file.isDirectory
        }?.let { files ->
            yieldAll(files.asSequence())
        }
    }

    companion object {
        private val LOG = Logger.getInstance(MacOsUnityDetector::class.java.name)
    }
}