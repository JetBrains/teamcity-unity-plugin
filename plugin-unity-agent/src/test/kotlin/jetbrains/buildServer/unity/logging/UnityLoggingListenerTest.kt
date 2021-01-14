/*
 * Copyright 2000-2021 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
                arrayOf<Any>("buildProblems.txt")
        )
    }
}