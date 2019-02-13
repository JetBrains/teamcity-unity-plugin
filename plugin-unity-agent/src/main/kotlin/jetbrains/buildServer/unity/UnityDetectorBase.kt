package jetbrains.buildServer.unity

import java.io.File

abstract class UnityDetectorBase : UnityDetector {

    private val additionalHintPaths = mutableListOf<File>()

    protected abstract val editorPath: String

    protected abstract val editorExecutable: String

    override fun getEditorPath(directory: File) = File(directory, "$editorPath/$editorExecutable")

    protected open fun getHintPaths(): Sequence<File> = sequence {
        // Get paths from "UNITY_HOME" environment variables
        System.getenv(UnityConstants.VAR_UNITY_HOME)?.let { unityHome ->
            if (unityHome.isEmpty()) return@let
            yieldAll(unityHome.split(File.pathSeparatorChar).map { path ->
                File(path)
            })
        }

        // Get paths from "UNITY_HINT_PATH" environment variables
        System.getenv(UnityConstants.VAR_UNITY_HINT_PATH)?.let { unityHintPaths ->
            if (unityHintPaths.isEmpty()) return@let
            unityHintPaths.split(File.pathSeparatorChar).forEach { path ->
                yieldAll(findUnityPaths(File(path)))
            }
        }

        // Get paths from "PATH" variable
        System.getenv("PATH")?.let { systemPath ->
            if (systemPath.isEmpty()) return@let
            systemPath.split(File.pathSeparatorChar).forEach { path ->
                if (path.endsWith(editorPath, true)) {
                    yield(File(path.removeRange(path.length - editorPath.length, path.length)))
                }
            }
        }

        // Get paths from "additional directories"
        additionalHintPaths.forEach { hintPath ->
            yieldAll(findUnityPaths(hintPath))
        }
    }

    protected fun findUnityPaths(directory: File) = sequence {
        // The convention to install multiple Unity versions is
        // to use suffixes for Unity directory, e.g. Unity_4.0b7
        directory.listFiles { file ->
            file.isDirectory && file.name.startsWith("Unity", true)
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

    fun registerAdditionalHintPath(hintPath: File) {
        additionalHintPaths += hintPath
    }
}
