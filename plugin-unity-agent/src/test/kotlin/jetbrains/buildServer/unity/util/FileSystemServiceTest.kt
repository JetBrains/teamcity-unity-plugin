package jetbrains.buildServer.unity.util

import io.kotest.matchers.shouldBe
import org.testng.annotations.Test
import java.io.File.pathSeparator
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class FileSystemServiceTest {

    private val path = "some" + pathSeparator + "path"

    @Test
    fun `should create file`() {
        // arrange
        val service = createInstance()

        // act
        val result = service.createPath(path)

        // assert
        result shouldBe Path.of(path)
    }

    @Test
    fun `should create file in parent directory`() {
        // arrange
        val parentDirectory = Path.of(path)
        val filename = "file.txt"
        val service = createInstance()

        // act
        val result = service.createPath(parentDirectory, filename)

        // assert
        result shouldBe Path.of(parentDirectory.absolutePathString(), filename)
    }

    private fun createInstance() = FileSystemService()
}