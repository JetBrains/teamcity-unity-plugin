package jetbrains.buildServer.unity

import com.github.zafarkhaja.semver.Version
import com.intellij.openapi.diagnostic.Logger
import java.io.File
import java.lang.Exception

class LinuxUnityDetector : UnityDetector {

    override fun findInstallations() = sequence {
        getHintPaths().forEach { path ->
            LOG.debug("Checking path $path")
            val executable = getEditorPath(path)
            if (!executable.exists()) return@forEach

            LOG.debug("Looking for package manager in $path")
            val packageVersions = File(path, "Editor/Data/PackageManager/Unity/PackageManager")
            if (!packageVersions.exists()) return@forEach

            val versions = packageVersions.listFiles { file ->
                file.isDirectory
            } ?: return@forEach

            if (versions.size != 1) {
                LOG.warn("Multiple Unity versions found in directory $path")
            }

            val version = versions.first().name
            try {
                yield(Version.valueOf(version) to path)
            } catch (e: Exception) {
                LOG.infoAndDebugDetails("Invalid Unity version $version in directory $path", e)
            }
        }
    }

    override fun getEditorPath(directory: File) = File(directory, "Editor/Unity")

    private fun getHintPaths() = sequence {
        val userHome = System.getProperty("user.home")?: return@sequence
        val directory = File(userHome)
        if (!directory.exists()) return@sequence

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
        private val LOG = Logger.getInstance(LinuxUnityDetector::class.java.name)
    }
}