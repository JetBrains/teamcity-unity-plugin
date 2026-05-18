package jetbrains.buildServer.unity.logging

import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.util.FileUtil
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import java.util.*

class UnityLoggingListenerTest {

    @Test(dataProvider = "testTransformations")
    fun testTransformation(filename: String) {
        val originalFile = File("src/test/resources/logger/original/" + filename)
        val original = ArrayList(FileUtil.readFile(originalFile))
        val processedFile = File("src/test/resources/logger/processed/" + filename)
        val processed = ArrayList(FileUtil.readFile(processedFile))
        val m = Mockery()
        val logger = m.mock<BuildProgressLogger>(BuildProgressLogger::class.java)
        val matcher = getMatcher(processed)
        val numberOfLines = processed.size
        m.checking(object : Expectations() {
            init {
                exactly(numberOfLines).of(logger).message(with(matcher))
            }
        })

        val listener = UnityLoggingListener(logger, LineStatusProvider())

        for (message in original) {
            listener.onStandardOutput(message)
        }

        listener.processFinished(0)

        m.assertIsSatisfied()
    }

    @Test
    fun `error line emits BuildProblem inline when suppressBuildProblems is false`() {
        val m = Mockery()
        val logger = m.mock<BuildProgressLogger>(BuildProgressLogger::class.java)
        val errorLine = "Fatal Error! Something went wrong"

        m.checking(object : Expectations() {
            init {
                oneOf(logger).message(with(containsBuildProblem()))
                never(logger).message(with(isPlainText(errorLine)))
            }
        })

        val listener = UnityLoggingListener(logger, LineStatusProvider(), suppressBuildProblems = false)
        listener.onStandardOutput(errorLine)
        m.assertIsSatisfied()
    }

    @Test
    fun `error line emits WARNING when suppressBuildProblems is true`() {
        val m = Mockery()
        val logger = m.mock<BuildProgressLogger>(BuildProgressLogger::class.java)
        val errorLine = "Fatal Error! Something went wrong"

        m.checking(object : Expectations() {
            init {
                oneOf(logger).message(with(containsWarningStatus()))
                never(logger).message(with(containsBuildProblem()))
            }
        })

        val listener = UnityLoggingListener(logger, LineStatusProvider(), suppressBuildProblems = true)
        listener.onStandardOutput(errorLine)
        m.assertIsSatisfied()
    }

    @Test
    fun `processFinished does not emit extra messages after error line`() {
        val m = Mockery()
        val logger = m.mock<BuildProgressLogger>(BuildProgressLogger::class.java)
        val errorLine = "Fatal Error! Something went wrong"

        m.checking(object : Expectations() {
            init {
                oneOf(logger).message(with(containsBuildProblem()))
            }
        })

        val listener = UnityLoggingListener(logger, LineStatusProvider(), suppressBuildProblems = false)
        listener.onStandardOutput(errorLine)
        listener.processFinished(0)
        m.assertIsSatisfied()
    }

    private fun getMatcher(processed: MutableList<String>): BaseMatcher<String> {
        return object : BaseMatcher<String>() {
            override fun matches(actual: Any): Boolean {
                return processed.size > 0 && isEquals(actual as String, processed.removeAt(0))
            }

            override fun describeTo(description: Description) {
                description.appendText("List matcher")
            }
        }
    }

    private fun isEquals(actual: String, expected: String): Boolean {
        return actual == expected
    }

    private fun containsBuildProblem(): BaseMatcher<String> = object : BaseMatcher<String>() {
        override fun matches(actual: Any) = (actual as? String)?.contains("##teamcity[buildProblem") == true
        override fun describeTo(description: Description) { description.appendText("contains buildProblem service message") }
    }

    private fun containsWarningStatus(): BaseMatcher<String> = object : BaseMatcher<String>() {
        override fun matches(actual: Any) = (actual as? String)?.contains("status='WARNING'") == true
        override fun describeTo(description: Description) { description.appendText("contains WARNING status") }
    }

    private fun isPlainText(text: String): BaseMatcher<String> = object : BaseMatcher<String>() {
        override fun matches(actual: Any) = actual == text
        override fun describeTo(description: Description) { description.appendText("equals plain text '$text'") }
    }

    @DataProvider
    fun testTransformations(): Array<Array<Any>> {
        return arrayOf(
            arrayOf<Any>("unityCommandLineArgs1.txt"),
            arrayOf<Any>("packageManager.txt"),
            arrayOf<Any>("unityExtensions.txt"),
            arrayOf<Any>("mono.txt"),
            arrayOf<Any>("performance.txt"),
            arrayOf<Any>("refresh.txt"),
            arrayOf<Any>("scriptCompilation.txt"),
            arrayOf<Any>("compile.txt"),
            arrayOf<Any>("buildReport.txt"),
            arrayOf<Any>("buildProblems.txt"),
        )
    }
}
