package jetbrains.buildServer.unity

import jetbrains.buildServer.unity.UnityVersion.Companion.parseVersion
import jetbrains.buildServer.unity.UnityVersion.Companion.tryParseVersion
import org.testng.Assert.*
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class UnityVersionTest {

    @Test
    fun `should have correct equals and hashcode implementation`() {
        // given
        val v1 = UnityVersion(2023, 1, 1)
        val v2 = UnityVersion(2023, 1, 1)

        val v3 = UnityVersion(2023, 1, 2)
        val v4 = UnityVersion(2023, 2, 1)
        val v5 = UnityVersion(2024, 1, 1)

        // then
        assertTrue(v1 == v2)
        assertEquals(v1.hashCode(), v2.hashCode())

        assertFalse(v1 == v3)
        assertFalse(v2 == v3)

        assertFalse(v2 == v4)
        assertFalse(v3 == v5)
    }

    @Test
    fun `should have correct toString implementation`() {
        // given
        val v1 = UnityVersion(2023, 1, 1)
        val v2 = UnityVersion(2023, 1)
        val v3 = UnityVersion(2023)

        // then
        assertEquals(v1.toString(), "2023.1.1")
        assertEquals(v2.toString(), "2023.1")
        assertEquals(v3.toString(), "2023")
    }

    @Test
    fun `should have correct compareTo implementation`() {
        // given
        val v1 = UnityVersion(2023, 1, 1)
        val v2 = UnityVersion(2023, 1, 2)

        // then
        assertTrue(v1 < v2)
        assertTrue(v1 <= v1)
        assertTrue(v2 > v1)
        assertTrue(v2 >= v1)
    }

    @DataProvider
    fun `valid version suffixes`(): Array<Array<String>> = arrayOf(
        arrayOf("a"),
        arrayOf("b"),
        arrayOf("p"),
        arrayOf("rc"),
        arrayOf("f"),
    )

    // Consider changing this behaviour in case the suffix becomes important
    @Test(dataProvider = "valid version suffixes")
    fun `should ignore version suffix`(versionSuffix: String) {
        // given
        val versionWithoutSuffix = UnityVersion(2023, 1, 1)
        val versionWithSuffix = parseVersion(versionWithoutSuffix.toString() + versionSuffix + "ignored part")

        // then
        assertEquals(versionWithoutSuffix, versionWithSuffix)
        assertEquals(versionWithoutSuffix.hashCode(), versionWithSuffix.hashCode())
    }

    @Test
    fun `should return next major version`() {
        // given
        val version = UnityVersion(2023, 1, 1)

        // when
        val nextMajor = version.nextMajor()

        // then
        assertEquals(nextMajor, UnityVersion(2024, 0, 0))
    }

    @Test
    fun `should return next minor version`() {
        // given
        val version = UnityVersion(2023, 1, 1)

        // when
        val nextMajor = version.nextMinor()

        // then
        assertEquals(nextMajor, UnityVersion(2023, 2, 0))
    }

    @DataProvider
    fun `valid unity versions`(): Array<Array<Any>> = arrayOf(
        arrayOf("2023.1", UnityVersion(2023, 1)),
        arrayOf("2023.1.1", UnityVersion(2023, 1, 1)),
        arrayOf("2023.3.4a", UnityVersion(2023, 3, 4)),
        arrayOf("2023.3.4a1", UnityVersion(2023, 3, 4)),
        arrayOf("2023.3.4b", UnityVersion(2023, 3, 4)),
        arrayOf("2023.3.4b1", UnityVersion(2023, 3, 4)),
        arrayOf("2023.3.4p", UnityVersion(2023, 3, 4)),
        arrayOf("2023.3.4p1", UnityVersion(2023, 3, 4)),
        arrayOf("2023.3.4rc", UnityVersion(2023, 3, 4)),
        arrayOf("2023.3.4rc1", UnityVersion(2023, 3, 4)),
        arrayOf("2023.3.4f", UnityVersion(2023, 3, 4)),
        arrayOf("2023.3.4f1", UnityVersion(2023, 3, 4)),
    )

    @Test(dataProvider = "valid unity versions")
    fun `should successfully try to parse valid Unity version`(version: String, expectedVersion: UnityVersion) {
        // when
        val result = tryParseVersion(version)

        // then
        assertEquals(result, expectedVersion)
    }

    @Test(dataProvider = "valid unity versions")
    fun `should successfully parse valid Unity version`(version: String, expectedVersion: UnityVersion) {
        // when
        val result = parseVersion(version)

        // then
        assertEquals(result, expectedVersion)
    }

    @DataProvider
    fun `invalid unity versions`(): Array<Array<Any>> = arrayOf(
        arrayOf("invalidVersion"),
        arrayOf("invalid version"),
    )

    @Test(dataProvider = "invalid unity versions")
    fun `should return null when Unity version cannot be parsed`(version: String) {
        // then
        assertNull(tryParseVersion(version))
    }

    @Test(dataProvider = "invalid unity versions")
    fun `should throw an exception when Unity version cannot be parsed`(version: String) {
        // then
        assertThrows(InvalidUnityVersionException::class.java) { parseVersion(version) }
    }
}