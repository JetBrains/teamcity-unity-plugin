

package jetbrains.buildServer.unity.detectors

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.unity.UnityVersion
import java.io.File

class WindowsUnityDetector(
    private val peProductVersionDetector: PEProductVersionDetector,
) : UnityDetectorBase() {

    override val editorPath = "Editor"
    override val editorExecutable = "Unity.exe"
    override val appConfigDir = "$userHome/AppData/Roaming"

    override fun findInstallations() = sequence {
        getHintPaths().distinct().forEach { path ->
            val version = getVersionFromInstall(path) ?: return@forEach
            yield(version to path)
        }
    }

    override fun getHintPaths() = sequence {
        yieldAll(super.getHintPaths())

        val programFiles = hashSetOf<String>()

        System.getenv("ProgramFiles")?.let { programFiles.add(it) }
        System.getenv("ProgramFiles(X86)")?.let { programFiles.add(it) }
        System.getenv("ProgramW6432")?.let { programFiles.add(it) }

        programFiles.forEach { path ->
            if (path.isEmpty()) return@forEach
            yieldAll(findUnityPaths(File(path)))
        }
    }

    override fun getVersionFromInstall(editorRoot: File): UnityVersion? {
        LOG.debug("Looking for Unity installation in $editorRoot")

        val executable = getEditorPath(editorRoot)
        if(!executable.exists()) {
            LOG.debug("Cannot find $executable")
            return null
        }

        val version = peProductVersionDetector.detect(executable)
        if (version == null) {
            LOG.debug("Cannot get version from $executable")
        }
        return version
    }
    companion object {
        private val LOG = Logger.getInstance(WindowsUnityDetector::class.java.name)
    }
}