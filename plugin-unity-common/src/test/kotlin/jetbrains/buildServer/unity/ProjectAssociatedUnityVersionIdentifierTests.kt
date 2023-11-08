package jetbrains.buildServer.unity

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test

class ProjectAssociatedUnityVersionIdentifierTests {
    @Test
    fun `should detect project associated Unity version`() {
        // arrange
        val identifier = ProjectAssociatedUnityVersionIdentifier()

        val unityVersion = UnityVersion(2023, 1, 17)
        val projectFilesAccessorStub = object : UnityProjectFilesAccessor {
            override fun directory(name: String) = this

            override fun file(name: String) = """
                m_EditorVersion: $unityVersion
                m_EditorVersionWithRevision: $unityVersion (4016570cf34f)
            """.trimIndent().byteInputStream()
        }

        // act
        val result = identifier.identify(projectFilesAccessorStub)

        // assert
        result shouldNotBe null
        result shouldBe unityVersion
    }
}