package jetbrains.buildServer.unity.util

import io.kotest.matchers.shouldBe
import org.testng.annotations.Test
import java.io.File
import kotlin.io.path.absolutePathString

class FileSystemServiceTest {

    private val path = "some" + File.pathSeparator + "path"

    @Test
    fun `should create file`() {
        // arrange
        val service = createInstance()

        // act
        val result = service.createPath(path)

        // assert
        result shouldBe File(path).toPath()
    }

    @Test
    fun `should create file in parent directory`() {
        // arrange
        val parentDirectory = File(path).toPath()
        val filename = "file.txt"
        val service = createInstance()

        // act
        val result = service.createPath(parentDirectory, filename)

        // assert
        result shouldBe File(parentDirectory.absolutePathString(), filename).toPath()
    }

    private fun createInstance() = FileSystemService()
}
