package jetbrains.buildServer.unity

import io.kotest.matchers.shouldBe
import org.testng.annotations.Test

class UnityEnvironmentTest {

    @Test
    fun `should create unity environment`() {
        // act
        val environment = UnityEnvironment("/path/to/unity", UnityVersion(2023, 1, 1), true)

        // assert
        environment.unityPath shouldBe "/path/to/unity"
        environment.unityVersion shouldBe UnityVersion(2023, 1, 1)
        environment.isVirtual shouldBe true
    }

    @Test
    fun `should create non-virtual unity environment by default`() {
        // act
        val environment = UnityEnvironment("/path/to/unity", UnityVersion(2023, 1, 1))

        // assert
        environment.unityPath shouldBe "/path/to/unity"
        environment.unityVersion shouldBe UnityVersion(2023, 1, 1)
        environment.isVirtual shouldBe false
    }
}
