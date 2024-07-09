

package jetbrains.buildServer.unity.detectors

import jetbrains.buildServer.unity.UnityVersion
import java.io.File

interface UnityDetector {
    fun findInstallations(): Sequence<Pair<UnityVersion, File>>
    fun getEditorPath(directory: File): File
    fun getVersionFromInstall(editorRoot: File): UnityVersion?
}
