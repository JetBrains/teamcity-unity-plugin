package jetbrains.buildServer.unity

import org.testng.Assert.*
import org.testng.annotations.Test

class UnityEnvironmentTest {

    @Test
    fun `should create unity environment`() {
        // when
        val environment = UnityEnvironment("/path/to/unity", UnityVersion(2023, 1, 1), true)

        // then
        assertEquals(environment.unityPath, "/path/to/unity")
        assertEquals(environment.unityVersion, UnityVersion(2023, 1, 1))
        assertTrue(environment.isVirtual)
    }

    @Test
    fun `should create non-virtual unity environment by default`() {
        // when
        val environment = UnityEnvironment("/path/to/unity", UnityVersion(2023, 1, 1))

        // then
        assertEquals(environment.unityPath, "/path/to/unity")
        assertEquals(environment.unityVersion, UnityVersion(2023, 1, 1))
        assertFalse(environment.isVirtual)
    }
}