package jetbrains.buildServer.unity.util

import io.kotest.matchers.shouldBe
import org.testng.annotations.Test
import java.io.File
import java.io.File.pathSeparator

class FileSystemServiceTest {

    private val path = "some" + pathSeparator + "path"

    @Test
    fun `should create file`() {
        // given
        val service = createInstance()

        // when
        val result = service.createFile(path)

        // then
        result shouldBe File(path)
    }

    @Test
    fun `should create file in parent directory`() {
        // given
        val parentDirectory = File(path)
        val filename = "file.txt"
        val service = createInstance()

        // when
        val result = service.createFile(parentDirectory, filename)

        // then
        result shouldBe File(parentDirectory, filename)
    }

    private fun createInstance() = FileSystemService()
}