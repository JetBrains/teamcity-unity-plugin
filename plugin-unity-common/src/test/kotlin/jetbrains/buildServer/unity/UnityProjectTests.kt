package jetbrains.buildServer.unity

import org.testng.annotations.DataProvider
import java.io.InputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UnityProjectTests {
    @DataProvider
    fun `unity version cases`(): Array<Array<Any>> = arrayOf(
        arrayOf("2022.3.12f1", UnityVersion.parseVersion("2022.3.12f1")),
        arrayOf("6000.0.7f1", UnityVersion.parseVersion("6000.0.7f1")),
    )

    @Test(dataProvider = "unity version cases")
    fun `determines Unity version associated with the project`(versionInConfig: String, expectedVersion: UnityVersion) {
        // arrange
        val settings = """
            m_EditorVersion: $versionInConfig
            m_EditorVersionWithRevision: $versionInConfig (4fe6e059c7ef)
        """.trimIndent()

        // act
        val unityVersion = UnityProject(createProjectFileAccessor(settings)).unityVersion

        // assert
        assertNotNull(unityVersion)
        assertEquals(expectedVersion, unityVersion)
    }

    @Test
    fun `returns null as Unity version when the directory with settings is missing`() {
        // arrange
        val fileAccessor = object : UnityProjectFilesAccessor {
            override fun directory(name: String) = if (name == "ProjectSettings") null else this
            override fun file(name: String) = """
                m_EditorVersion: 2022.3.12f1
            """.trimIndent().byteInputStream()
        }

        // act
        val unityVersion = UnityProject(fileAccessor).unityVersion

        // assert
        assertNull(unityVersion)
    }

    @Test
    fun `returns null as Unity version when the file with settings is missing`() {
        // arrange
        val fileAccessor = object : UnityProjectFilesAccessor {
            override fun directory(name: String) = this
            override fun file(name: String) = if (name == "ProjectVersion.txt") {
                null
            } else {
                """
                m_EditorVersion: 2022.3.12f1
                """.trimIndent().byteInputStream()
            }
        }

        // act
        val unityVersion = UnityProject(fileAccessor).unityVersion

        // assert
        assertNull(unityVersion)
    }

    @DataProvider
    fun `missing or malformed Unity version cases`(): Array<Array<Any>> = arrayOf(
        arrayOf(
            """
            foo: bar
            key: value
            """.trimIndent(),
        ),
        arrayOf(
            """
            EditorVersion: 2022.3.12f1
            """.trimIndent(),
        ),
        arrayOf(
            """
            m_EditorVersion: foo
            """.trimIndent(),
        ),
    )

    @Test(dataProvider = "missing or malformed Unity version cases")
    fun `returns null as Unity version when there is no corresponding key in settings or its value is malformed`(
        settingsContent: String,
    ) {
        // act
        val unityVersion = UnityProject(createProjectFileAccessor(settingsContent)).unityVersion

        // assert
        assertNull(unityVersion)
    }

    @DataProvider
    fun `asset pipeline version cases`(): Array<Array<Any>> = arrayOf(
        arrayOf("0", AssetPipelineVersion.V1),
        arrayOf("1", AssetPipelineVersion.V2),
    )

    @Test(dataProvider = "asset pipeline version cases")
    fun `determines asset pipeline version used in the project`(versionInConfig: String, expectedVersion: AssetPipelineVersion) {
        // arrange
        val settings = """
            %YAML 1.1
            %TAG !u! tag:unity3d.com,2011:
            --- !u!159 &1
            EditorSettings:
              m_ObjectHideFlags: 0
              m_AssetPipelineMode: $versionInConfig
        """.trimIndent()

        // act
        val assetPipelineVersion = UnityProject(createProjectFileAccessor(settings)).assetPipelineVersion

        // assert
        assertNotNull(assetPipelineVersion)
        assertEquals(expectedVersion, assetPipelineVersion)
    }

    @Test
    fun `returns null as Asset Pipeline version when the directory with settings is missing`() {
        // arrange
        val fileAccessor = object : UnityProjectFilesAccessor {
            override fun directory(name: String) = if (name == "ProjectSettings") null else this
            override fun file(name: String) = """
                EditorSettings:
                  m_ObjectHideFlags: 0
                  m_AssetPipelineMode: 0
            """.trimIndent().byteInputStream()
        }

        // act
        val assetPipelineVersion = UnityProject(fileAccessor).assetPipelineVersion

        // assert
        assertNull(assetPipelineVersion)
    }

    @Test
    fun `returns null as Asset Pipeline version when the file with settings is missing`() {
        // arrange
        val fileAccessor = object : UnityProjectFilesAccessor {
            override fun directory(name: String) = this
            override fun file(name: String) = if (name == "EditorSettings.asset") {
                null
            } else {
                """
                EditorSettings:
                  m_ObjectHideFlags: 0
                  m_AssetPipelineMode: 0
                """.trimIndent().byteInputStream()
            }
        }

        // act
        val assetPipelineVersion = UnityProject(fileAccessor).assetPipelineVersion

        // assert
        assertNull(assetPipelineVersion)
    }

    @DataProvider
    fun `missing or malformed Asset Pipeline version cases`(): Array<Array<Any>> = arrayOf(
        arrayOf(
            """
            foo: bar
            key: value
            """.trimIndent(),
        ),
        arrayOf(
            """
            EditorVersion: 2022.3.12f1
            """.trimIndent(),
        ),
        arrayOf(
            """
            m_EditorVersion: foo
            """.trimIndent(),
        ),
    )

    @Test(dataProvider = "missing or malformed Asset Pipeline version cases")
    fun `returns null as Asset Pipeline version when there is no corresponding key in settings or its value is malformed`(
        settingsContent: String,
    ) {
        // act
        val assetPipelineVersion = UnityProject(createProjectFileAccessor(settingsContent)).assetPipelineVersion

        // assert
        assertNull(assetPipelineVersion)
    }

    @Test
    fun `performs project property read only once`() {
        // arrange
        var callCount = 0
        val filesAccessor = object : UnityProjectFilesAccessor {
            override fun directory(name: String): UnityProjectFilesAccessor {
                callCount++
                return this
            }

            override fun file(name: String): InputStream? {
                callCount++
                return """
                    m_EditorVersion: 2022.3.12f1
                """.trimIndent().byteInputStream()
            }
        }

        val project = UnityProject(filesAccessor)

        // act
        project.unityVersion
        project.unityVersion
        project.unityVersion

        // assert
        assertEquals(2, callCount)
    }

    private fun createProjectFileAccessor(settingsContent: String) =
        object : UnityProjectFilesAccessor {
            override fun directory(name: String) = this
            override fun file(name: String) = settingsContent.byteInputStream()
        }
}
