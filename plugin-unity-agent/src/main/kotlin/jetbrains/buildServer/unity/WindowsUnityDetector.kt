package jetbrains.buildServer.unity

import com.github.zafarkhaja.semver.Version
import jetbrains.buildServer.agent.runner.JavaCommandLineBuilder.LOG
import jetbrains.buildServer.util.PEReader.PEUtil
import java.io.File

class WindowsUnityDetector : UnityDetectorBase() {
    override fun findInstallations() = sequence {
        getHintPaths().distinct().forEach {  path ->
            LOG.info("Checking $path")
            val executable = getEditorPath(path)
            if (!executable.exists()) return@forEach

            val version = PEUtil.getProductVersion(executable) ?: return@forEach
            yield(Version.forIntegers(version.p1, version.p2, version.p3) to path)
        }
    }

    override fun getEditorPath(directory: File) = File(directory, "Editor/Unity.exe")

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