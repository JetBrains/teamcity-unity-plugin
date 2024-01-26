

package jetbrains.buildServer.unity.detectors

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.unity.UnityVersion
import jetbrains.buildServer.unity.UnityVersion.Companion.tryParseVersion
import jetbrains.buildServer.unity.util.Completed
import jetbrains.buildServer.unity.util.Error
import jetbrains.buildServer.unity.util.Timeout
import jetbrains.buildServer.unity.util.execute
import java.io.File

class LinuxUnityDetector : UnityDetectorBase() {

    override val editorPath = "Editor"
    override val editorExecutable = "Unity"
    override val appConfigDir = "$userHome/.config"

    override fun findInstallations() = sequence {
        getHintPaths().distinct().forEach { path ->
            val version = getVersionFromInstall(path) ?: return@forEach
            yield(version to path)
        }
    }

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

    override fun getVersionFromInstall(editorRoot: File): UnityVersion? {
        LOG.debug("Looking for Unity installation in $editorRoot")
        val executable = getEditorPath(editorRoot)
        if (!executable.exists()) {
          LOG.debug("Cannot find $executable")
          return null
        }

        LOG.debug("Looking for package manager in $editorRoot")
        var version : String? = null
        val packageVersions = File(editorRoot, "Editor/Data/PackageManager/Unity/PackageManager")
        if (packageVersions.exists()) {
            LOG.debug("A package manager was found")
            val versions = packageVersions.listFiles { file ->
                file.isDirectory
            } ?: return null

            if (versions.size != 1) {
                LOG.warn("Multiple Unity versions found in directory $editorRoot")
            }

            version = versions.firstOrNull()?.name
        } else {
            LOG.debug("A package manager was not found")
        }

        version = version ?: getVersionFromEditor(executable)

        if (version == null) {
            version = editorRoot.name
        }

        LOG.info("Version is $version")
        return try {
            val unityVersion = tryParseVersion(version)
            LOG.debug("Reports version $unityVersion to ${editorRoot.name}")
            unityVersion
        } catch (e: Exception) {
            LOG.infoAndDebugDetails("Invalid Unity version $version in directory $editorRoot", e)
            null
        }
    }

    private fun getVersionFromEditor(executable: File): String? {
        when (val result = ProcessBuilder(executable.absolutePath, "-version").execute(timeoutSeconds = 3)) {
            is Completed -> when (result.exitCode) {
                0 -> {
                    LOG.info("Version detected via editor: ${result.stdout}")
                    return result.stdout.ifEmpty { null }
                }
                else -> LOG.info("Version detection process exited with non-zero code: ${result.exitCode}\n" +
                        "Error: ${result.stderr}")
            }
            is Timeout -> LOG.info("Version detection timed out")
            is Error -> LOG.info("Unable to detect version via editor binary", result.exception)
        }

        return null
    }

    companion object {
        private val LOG = Logger.getInstance(LinuxUnityDetector::class.java.name)
    }
}