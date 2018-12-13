package jetbrains.buildServer.unity

import com.github.zafarkhaja.semver.Version
import com.intellij.openapi.util.SystemInfo
import jetbrains.buildServer.util.PEReader.PEUtil
import java.io.File

class WindowsUnityDetector : UnityDetector {
    override fun findInstallations() = sequence {
        getHintPaths().forEach {  path ->
            val executable = getEditorPath(path)
            if (!executable.exists()) return@forEach

            val version = PEUtil.getProductVersion(executable) ?: return@forEach
            yield(Version.forIntegers(version.p1, version.p2, version.p3) to path)
        }
    }

    override fun getEditorPath(directory: File) = File(directory, "Editor/Unity.exe")

    private fun getHintPaths() = sequence<File> {
        val programFiles = hashSetOf<String>()

        System.getenv("ProgramFiles")?.let {  programFiles.add(it)}
        System.getenv("ProgramFiles(X86)")?.let {  programFiles.add(it)}
        System.getenv("ProgramW6432")?.let {  programFiles.add(it)}

        programFiles.forEach {
            yieldAll(findUnderProgramFiles(it))
        }
    }

    private fun findUnderProgramFiles(path: String?) = sequence {
        if (path.isNullOrEmpty()) return@sequence
        val programFiles = File(path)

        // The convention to install multiple Unity versions is
        // to use suffixes for Unity directory, e.g. Unity_4.0b7
        programFiles.listFiles {
            file -> file.isDirectory && file.name.startsWith("Unity")
        }?.let { files ->
            yieldAll(files.asSequence())
        }

        // Unity Hub installs editors under Unity/Hub/Editor directory,
        // e.g. Unity/Hub/Editor/2018.1.9f2
        val unityHub = File(programFiles, "Unity/Hub/Editor")
        unityHub.listFiles {
            file -> file.isDirectory
        }?.let { files ->
            yieldAll(files.asSequence())
        }
    }
}