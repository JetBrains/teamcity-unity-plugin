package jetbrains.buildServer.unity

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.equals.shouldNotBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import jetbrains.buildServer.unity.UnityVersion.Companion.parseVersion
import jetbrains.buildServer.unity.UnityVersion.Companion.tryParseVersion
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class UnityVersionTest {

    @Test
    fun `should have correct equals and hashcode implementation`() {
        // arrange
        val v1 = UnityVersion(2023, 1, 1)
        val v2 = UnityVersion(2023, 1, 1)

        val v3 = UnityVersion(2023, 1, 2)
        val v4 = UnityVersion(2023, 2, 1)
        val v5 = UnityVersion(2024, 1, 1)

        // assert
        v1 shouldBeEqual v2
        v1.hashCode() shouldBeEqual v2.hashCode()

        v1 shouldNotBeEqual v3
        v2 shouldNotBeEqual v3

        v2 shouldNotBeEqual v4
        v3 shouldNotBeEqual v5
    }

    @Test
    fun `should have correct toString implementation`() {
        // arrange
        val v1 = UnityVersion(2023, 1, 1)
        val v2 = UnityVersion(2023, 1)
        val v3 = UnityVersion(2023)

        // assert
        v1.toString() shouldBeEqual "2023.1.1"
        v2.toString() shouldBeEqual "2023.1"
        v3.toString() shouldBeEqual "2023"
    }

    @Test
    fun `should have correct compareTo implementation`() {
        // arrange
        val v1 = UnityVersion(2023, 1, 1)
        val v2 = UnityVersion(2023, 1, 2)

        // assert
        v1 shouldBeLessThan v2
        v1 shouldBeLessThanOrEqualTo v1
        v2 shouldBeGreaterThan v1
        v2 shouldBeGreaterThanOrEqualTo v1
        v2 shouldBeGreaterThanOrEqualTo v2
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
        // arrange
        val versionWithoutSuffix = UnityVersion(2023, 1, 1)
        val versionWithSuffix = parseVersion(versionWithoutSuffix.toString() + versionSuffix + "ignored part")

        // assert
        versionWithoutSuffix shouldBeEqual versionWithSuffix
    }

    @Test
    fun `should return next major version`() {
        // arrange
        val version = UnityVersion(2023, 1, 1)

        // act
        val nextMajor = version.nextMajor()

        // assert
        nextMajor shouldBeEqual UnityVersion(2024, 0, 0)
    }

    @Test
    fun `should return next minor version`() {
        // arrange
        val version = UnityVersion(2023, 1, 1)

        // act
        val nextMinor = version.nextMinor()

        // assert
        nextMinor shouldBeEqual UnityVersion(2023, 2, 0)
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
        // act
        val result = tryParseVersion(version)

        // assert
        result shouldNotBe null
        result?.shouldBeEqual(expectedVersion)
    }

    @Test(dataProvider = "valid unity versions")
    fun `should successfully parse valid Unity version`(version: String, expectedVersion: UnityVersion) {
        // act
        val result = parseVersion(version)

        // assert
        result shouldBeEqual expectedVersion
    }

    @DataProvider
    fun `invalid unity versions`(): Array<Array<Any>> = arrayOf(
        arrayOf("invalidVersion"),
        arrayOf("invalid version"),
    )

    @Test(dataProvider = "invalid unity versions")
    fun `should return null when Unity version cannot be parsed`(version: String) {
        // assert
        tryParseVersion(version) shouldBe null
    }

    @Test(dataProvider = "invalid unity versions")
    fun `should throw an exception when Unity version cannot be parsed`(version: String) {
        // assert
        shouldThrowExactly<InvalidUnityVersionException> { parseVersion(version) }
    }
}
