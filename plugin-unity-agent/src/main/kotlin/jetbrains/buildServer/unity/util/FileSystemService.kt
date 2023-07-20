package jetbrains.buildServer.unity.util

import java.io.File
import java.io.File.createTempFile

class FileSystemService {
    fun createFile(path: String) = File(path)

    fun createFile(parent: File, child: String) = File(parent, child)

    fun createTempFile(directory: File, prefix: String, suffix: String): File {
        return createTempFile(prefix, suffix, directory)
    }

    fun readText(file: File) = file.readText()
}