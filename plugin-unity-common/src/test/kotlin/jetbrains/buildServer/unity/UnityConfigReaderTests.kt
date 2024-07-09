package jetbrains.buildServer.unity

import org.testng.annotations.DataProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UnityConfigReaderTests {
    data class SimpleKeyReadTestCase(
        val content: String,
        val key: String,
        val expectedValue: String,
    )

    @DataProvider
    fun `read simple key test cases`(): Array<SimpleKeyReadTestCase> = arrayOf(
        SimpleKeyReadTestCase(
            content = """
                %YAML 1.1
                %TAG !u! tag:unity3d.com,2011:
                --- !u!159 &1
                EditorSettings:
                  m_LineEndingsForNewScripts: 0
                  m_DefaultBehaviorMode: 0
                  m_PrefabRegularEnvironment: {fileID: 0}
            """.trimIndent(),
            key = "m_LineEndingsForNewScripts",
            expectedValue = "0",
        ),
        SimpleKeyReadTestCase(
            content = """
                m_EditorVersion: 2022.3.12f1
                m_EditorVersionWithRevision: 2022.3.12f1 (4fe6e059c7ef)
            """.trimIndent(),
            key = "m_EditorVersionWithRevision",
            expectedValue = "2022.3.12f1 (4fe6e059c7ef)",
        ),
        SimpleKeyReadTestCase(
            content = """
                productName: Foo
                defaultCursor: {fileID: 0}
                cursorHotspot: {x: 0, y: 0}
                m_SplashScreenBackgroundColor: {r: 0.1241, g: 0.332, b: 0.99583, a: 1}
            """.trimIndent(),
            key = "cursorHotspot",
            expectedValue = "{x: 0, y: 0}",
        ),
        SimpleKeyReadTestCase(
            content = """
                m_DefaultContactOffset: 0.01
                m_JobOptions:
                    serializedVersion: 2
            """.trimIndent(),
            key = "serializedVersion",
            expectedValue = "2",
        ),
    )

    @Test(dataProvider = "read simple key test cases")
    fun `reads simple key`(case: SimpleKeyReadTestCase) {
        // arrange
        val reader = UnityConfigReader()

        // act
        val result = reader.readValue(case.content.byteInputStream(), case.key)

        // assert
        assertEquals(case.expectedValue, result)
    }

    @DataProvider
    fun `missing key cases`(): Array<Array<String>> = arrayOf(
        arrayOf("foo"),
        arrayOf("defaultCursor:"),
        arrayOf("fileID"),
        arrayOf("m_BuildTarget"),
    )

    @Test(dataProvider = "missing key cases")
    fun `returns null when the specified key not found`(key: String) {
        // arrange
        val content = """
            %YAML 1.1
            %TAG !u! tag:unity3d.com,2011:
            --- !u!129 &1
            PlayerSettings:
                m_ObjectHideFlags: 0
                defaultCursor: {fileID: 0}
                m_BuildTargetIcons: []
                m_BuildTargetPlatformIcons:
                    - m_BuildTarget: iPhone
                        m_Icons:
                        - m_Textures: []
        """.trimIndent()

        // act
        val result = UnityConfigReader().readValue(content.byteInputStream(), key)

        // assert
        assertNull(result)
    }
}
