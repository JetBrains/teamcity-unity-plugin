package jetbrains.buildServer.unity

import java.io.File

abstract class UnityDetectorBase : UnityDetector {
    private val additionalHintPaths: MutableCollection<File> = mutableListOf()

    protected open fun getHintPaths() = sequence {
        // Get paths from "UNITY_HOME" environment variables
        System.getenv(UnityConstants.VAR_UNITY_HOME)?.let { unityHome ->
            if (unityHome.isNotEmpty()) {
                yieldAll(unityHome.split(File.pathSeparatorChar).map { path ->
                    File(path)
                })
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
            file.isDirectory && (file.name.toLowerCase().startsWith("unity"))
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

    fun registerAdditionalHintPath(hintPath: File)
    {
        additionalHintPaths.add(hintPath)
    }
}