package jetbrains.buildServer.unity

import com.github.zafarkhaja.semver.Version
import java.io.File

interface UnityDetector {
    fun findInstallations(): Sequence<Pair<Version, File>>
    fun getEditorPath(directory: File): File
}