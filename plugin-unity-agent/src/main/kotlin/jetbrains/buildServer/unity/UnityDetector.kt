package jetbrains.buildServer.unity

import com.vdurmont.semver4j.Semver
import java.io.File

interface UnityDetector {
    fun findInstallations(): Sequence<Pair<Semver, File>>
    fun getEditorPath(directory: File): File
}