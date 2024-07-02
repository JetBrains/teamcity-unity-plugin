

package jetbrains.buildServer.unity

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.unity.UnityConstants.PARAM_DETECTION_MODE
import jetbrains.buildServer.unity.UnityConstants.PARAM_UNITY_ROOT
import jetbrains.buildServer.unity.UnityConstants.PARAM_UNITY_VERSION
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class UnityBuildFeatureTest {
    private val pluginDescriptorMock = mockk<PluginDescriptor> {
        every { getPluginResourcesPath(any()) } returns "foo"
    }

    private val buildFeature = UnityBuildFeature(pluginDescriptorMock)

    @Test
    fun getRequirements_happyPath_returnEmptyUnityRequirement() {
        // arrange
        val sut = buildFeature

        val unityVersion = "2021.3.16"
        val parameters = mutableMapOf(
            PARAM_UNITY_VERSION to unityVersion,
        )

        // act
        val requirements = sut.getRequirements(parameters)

        // assert
        requirements shouldNotBe null
        requirements.shouldBeEmpty()
    }

    @DataProvider
    fun activateLicenseTestData(): Array<Array<Any?>> {
        return arrayOf(
            arrayOf(emptyMap<String, String>(), false),
            arrayOf(mapOf("activateLicense" to "true"), true),
            arrayOf(mapOf("activateLicense" to "false"), false),
            arrayOf(mapOf("activateLicense" to "invalid activate license value"), false),
            arrayOf(mapOf("unityLicenseType" to "personalLicense"), true),
            arrayOf(mapOf("unityLicenseType" to "professionalLicense"), true),
            arrayOf(mapOf("unityLicenseType" to "invalid license type"), false),
        )
    }

    @Test(dataProvider = "activateLicenseTestData")
    fun describeParameters_activateLicenseParamExists_describeWhenTrue(
        licenseTypeParam: Map<String, String>,
        shouldDescribe: Boolean,
    ) {
        // arrange
        val sut = buildFeature

        // act
        val description = sut.describeParameters(licenseTypeParam)

        // assert
        description.isNotBlank() shouldBe shouldDescribe
    }

    @Test
    fun describeParameters_activateLicenseParamAbsent_doNotDescribe() {
        // arrange
        val sut = buildFeature
        val parameters: MutableMap<String, String> = mutableMapOf()

        // act
        val description = sut.describeParameters(parameters)

        // assert
        description.isBlank() shouldBe true
    }

    @Test
    fun describeParameters_cacheServerParamExists_alwaysDescribe() {
        // arrange
        val sut = buildFeature
        val cacheServerParam = "localhost:8080/cs"
        val parameters = mutableMapOf(UnityConstants.PARAM_CACHE_SERVER to cacheServerParam)

        // act
        val description = sut.describeParameters(parameters)

        // assert
        description.isNotBlank() shouldBe true
        description shouldContain cacheServerParam
    }

    @Test
    fun describeParameters_cacheServerParamAbsent_doNotDescribe() {
        // arrange
        val sut = buildFeature
        val parameters: MutableMap<String, String> = mutableMapOf()

        // act
        val description = sut.describeParameters(parameters)

        // assert
        description.isBlank() shouldBe true
    }

    @DataProvider
    fun correctUnityLocationParamsSet(): Array<Array<Any?>> {
        return arrayOf(
            arrayOf(
                mapOf(
                    PARAM_DETECTION_MODE to "auto",
                    PARAM_UNITY_VERSION to "2020.1.1",
                ),
                setOf("2020.1.1"),
                setOf<String>(),
            ),
            arrayOf(
                mapOf(
                    PARAM_DETECTION_MODE to "auto",
                    PARAM_UNITY_VERSION to "2020.1.1",
                    PARAM_UNITY_ROOT to "path/to/unity",
                ),
                setOf("2020.1.1"),
                setOf("path/to/unity"),
            ),
            arrayOf(
                mapOf(
                    PARAM_DETECTION_MODE to "manual",
                    PARAM_UNITY_ROOT to "path/to/unity",
                ),
                setOf("path/to/unity"),
                setOf<String>(),
            ),
            arrayOf(
                mapOf(
                    PARAM_DETECTION_MODE to "manual",
                    PARAM_UNITY_VERSION to "2020.1.1",
                    PARAM_UNITY_ROOT to "path/to/unity",
                ),
                setOf("path/to/unity"),
                setOf("2020.1.1"),
            ),
        )
    }

    @Test(dataProvider = "correctUnityLocationParamsSet")
    fun describeParameters_correctUnityLocationParamsSet_describeDistinctively(
        parameters: MutableMap<String, String>,
        shouldContain: Set<String>,
        shouldNotContain: Set<String>,
    ) {
        // arrange
        val sut = buildFeature

        // act
        val description = sut.describeParameters(parameters)

        // assert
        description.isNotBlank() shouldBe true
        shouldContain.forEach { description shouldContain it }
        shouldNotContain.forEach { description shouldNotContain it }
    }

    @DataProvider
    fun incorrectUnityLocationParamsSet(): Array<Array<Any?>> {
        return arrayOf(
            arrayOf(
                mapOf<String, String>(),
            ),
            arrayOf(
                mapOf(
                    PARAM_UNITY_VERSION to "2020.1.1",
                    PARAM_UNITY_ROOT to "path/to/unity",
                ),
            ),
            arrayOf(
                mapOf(
                    PARAM_DETECTION_MODE to "auto",
                    PARAM_UNITY_ROOT to "path/to/unity",
                ),
            ),
            arrayOf(
                mapOf(
                    PARAM_DETECTION_MODE to "manual",
                    PARAM_UNITY_VERSION to "2020.1.1",
                ),
            ),
        )
    }

    @Test(dataProvider = "incorrectUnityLocationParamsSet")
    fun describeParameters_incorrectUnityLocationParamsSet_doNotDescribe(parameters: MutableMap<String, String>) {
        // arrange
        val sut = buildFeature

        // act
        val description = sut.describeParameters(parameters)

        // assert
        description.isBlank() shouldBe true
    }
}
