

package jetbrains.buildServer.unity.logging

import io.kotest.matchers.equals.shouldBeEqual
import jetbrains.buildServer.unity.logging.LineStatus.*
import org.testng.annotations.Test
import java.io.File

class LineStatusProviderTest {

    @Test
    fun testCustomFile() {
        // arrange
        val customSettingsFile = File("src/test/resources/logger/customLogging.xml")
        val provider = LineStatusProvider(customSettingsFile)

        // act // assert
        provider.getLineStatus("text") shouldBeEqual Normal
        provider.getLineStatus("error message") shouldBeEqual Normal
        provider.getLineStatus("warning message") shouldBeEqual Normal
        provider.getLineStatus("customWarning: message") shouldBeEqual Warning
        provider.getLineStatus("customError: message") shouldBeEqual Error
    }
}