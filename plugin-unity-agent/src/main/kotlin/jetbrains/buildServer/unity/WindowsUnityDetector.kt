package jetbrains.buildServer.unity

import com.github.zafarkhaja.semver.Version
import jetbrains.buildServer.util.PEReader.PEUtil
import java.io.File

class WindowsUnityDetector : UnityDetectorBase() {

    override val editorPath: String
        get() = "Editor"

    override val editorExecutable: String
        get() = "Unity.exe"

    override fun findInstallations() = sequence {
        getHintPaths().distinct().forEach {  path ->
            val executable = getEditorPath(path)
            if (!executable.exists()) return@forEach

            val version = PEUtil.getProductVersion(executable) ?: return@forEach
            yield(Version.forIntegers(version.p1, version.p2, version.p3) to path)
        }
    }

    override fun getHintPaths() = sequence {
        yieldAll(super.getHintPaths())

        val programFiles = hashSetOf<String>()

        System.getenv("ProgramFiles")?.let {  programFiles.add(it)}
        System.getenv("ProgramFiles(X86)")?.let {  programFiles.add(it)}
        System.getenv("ProgramW6432")?.let {  programFiles.add(it)}

        programFiles.forEach { path ->
            if (path.isEmpty()) return@forEach
            yieldAll(findUnityPaths(File(path)))
        }
    }
}