

package jetbrains.buildServer.unity.fetchers

import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.nio.file.Files
import java.nio.file.Paths

class CSharpFileParserTest {
    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("file1.cs", mapOf("TestScript.Build" to null)),
                arrayOf("file2.cs", mapOf("MyScriptName.MyMethod" to null)),
                arrayOf("file3.cs", mapOf("WebGLBuilder.build" to null)),
                arrayOf("file4.cs", mapOf(
                        "BuildScript.BuildAndroid" to "Custom Build",
                        "BuildScript.Method1" to null,
                        "BuildScript.Method2" to null,
                        "BuildScript.Method3" to null
                )),
                arrayOf("file5.cs", mapOf("BuildScript.CustomBuild" to "Description")),
                arrayOf("file6.cs", mapOf("Namespace.BuildScript.CustomBuild" to "Description"))
        )
    }

    @Test(dataProvider = "testData")
    fun getTargetNames(fileName: String, expectedNames: Map<String, String?>) {
        val methods = Files.newInputStream(Paths.get("src/test/resources/methodName/$fileName")).use {
            CSharpFileParser.readStaticMethods(it)
        }
        Assert.assertEquals(methods, expectedNames)
        expectedNames.keys.forEach {
            Assert.assertTrue(methods.containsKey(it))
        }
    }
}