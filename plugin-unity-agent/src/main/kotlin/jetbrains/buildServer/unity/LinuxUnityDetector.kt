package jetbrains.buildServer.unity

import com.github.zafarkhaja.semver.Version
import com.intellij.openapi.diagnostic.Logger
import java.io.File
import java.lang.Exception

class LinuxUnityDetector : UnityDetectorBase() {

    override fun findInstallations() = sequence {
        getHintPaths().distinct().forEach { path ->
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

    override fun getHintPaths() = sequence {
        yieldAll(super.getHintPaths())

        // Find installations within user profile
        System.getProperty("user.home")?.let { userHome ->
            if (userHome.isNotEmpty()) {
                yieldAll(findUnityPaths(File(userHome)))
            }
        }

        // deb packages are installing Unity in the /opt/Unity directory
        yieldAll(findUnityPaths(File("/opt/")))
    }

    companion object {
        private val LOG = Logger.getInstance(LinuxUnityDetector::class.java.name)
    }
}