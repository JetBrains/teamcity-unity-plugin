package jetbrains.buildServer.unity.util

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteIfExists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class FileSystemService {
    fun createFile(path: String): Path = Path.of(path)

    fun createFile(parent: Path, child: String): Path = Path.of(parent.absolutePathString(), child)

    fun createTempFile(directory: Path, prefix: String, suffix: String): Path {
        return Files.createTempFile(directory, prefix, suffix)
    }

    fun deleteFile(file: Path) = file.deleteIfExists()

    fun readText(file: Path) = file.readText()

    fun writeText(file: Path, text: String) = file.writeText(text)
}