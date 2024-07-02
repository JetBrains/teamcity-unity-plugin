package jetbrains.buildServer.unity.util

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import jetbrains.buildServer.agent.AgentBuildFeature
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.unity.UnityLicenseScope
import jetbrains.buildServer.unity.UnityLicenseScope.BUILD_CONFIGURATION
import jetbrains.buildServer.unity.UnityLicenseScope.BUILD_STEP
import jetbrains.buildServer.unity.UnityLicenseTypeParameter
import jetbrains.buildServer.unity.UnityVersion
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class UnityParametersExtractorTest {

    private val runnerContext = mockk<BuildRunnerContext>()
    private val build = mockk<AgentRunningBuild>()
    private val buildFeature = mockk<AgentBuildFeature>()

    @BeforeMethod
    fun setUp() {
        clearAllMocks()

        every { runnerContext.build } returns build
        every { build.getBuildFeaturesOfType(any()) } returns setOf(buildFeature)
    }

    @Test
    fun `should return unityRoot from runner parameters`() {
        // arrange
        every { runnerContext.runnerParameters } returns mapOf("unityRoot" to "/path/to/unity")

        // act
        val result = runnerContext.unityRootParam()

        // assert
        result shouldNotBe null
        result?.shouldBeEqual("/path/to/unity")

        verify { build wasNot Called }
        verify { buildFeature wasNot Called }
    }

    @Test
    fun `should return unityRoot from build feature parameters`() {
        // arrange
        every { runnerContext.runnerParameters } returns mapOf()
        every { buildFeature.parameters } returns mapOf("unityRoot" to "/path/to/unity")

        // act
        val result = runnerContext.unityRootParam()

        // assert
        result shouldNotBe null
        result?.shouldBeEqual("/path/to/unity")
    }

    @Test
    fun `should return null if unityRoot parameter is not found`() {
        // arrange
        every { runnerContext.runnerParameters } returns mapOf()
        every { buildFeature.parameters } returns mapOf()

        // act
        val result = runnerContext.unityRootParam()

        // assert
        result shouldBe null
    }

    @Test
    fun `should return unity version from runner parameters`() {
        // arrange
        every { runnerContext.runnerParameters } returns mapOf("unityVersion" to "2023.1.1")

        // act
        val result = runnerContext.unityVersionParam()

        // assert
        result shouldNotBe null
        result?.shouldBeEqual(UnityVersion(2023, 1, 1))

        verify { build wasNot Called }
        verify { buildFeature wasNot Called }
    }

    @Test
    fun `should return unity version from build feature parameters`() {
        // arrange
        every { runnerContext.runnerParameters } returns mapOf()
        every { buildFeature.parameters } returns mapOf("unityVersion" to "2023.1.1")

        // act
        val result = runnerContext.unityVersionParam()

        // assert
        result shouldNotBe null
        result?.shouldBeEqual(UnityVersion(2023, 1, 1))
    }

    @Test
    fun `should return unity version when it is surrounded with spaces`() {
        // arrange
        every { runnerContext.runnerParameters } returns mapOf("unityVersion" to " 2023.1.1 ")

        // act
        val result = runnerContext.unityVersionParam()

        // assert
        result shouldNotBe null
        result?.shouldBeEqual(UnityVersion(2023, 1, 1))

        verify { build wasNot Called }
        verify { buildFeature wasNot Called }
    }

    @Test
    fun `should return null if unityVersion parameter is not found`() {
        // arrange
        every { runnerContext.runnerParameters } returns mapOf()
        every { buildFeature.parameters } returns mapOf()

        // act
        val result = runnerContext.unityVersionParam()

        // assert
        result shouldBe null
    }

    @Test
    fun `should return null if unityVersion parameter contains not valid version`() {
        // arrange
        every { runnerContext.runnerParameters } returns mapOf("unityVersion" to "invalid version string")

        // act
        val result = runnerContext.unityVersionParam()

        // assert
        result shouldBe null
    }

    @DataProvider
    fun `license type test data`(): Array<Array<Any?>> = arrayOf(
        arrayOf(mapOf("activateLicense" to "true"), UnityLicenseTypeParameter.PROFESSIONAL),
        arrayOf(mapOf("unityLicenseType" to "professionalLicense"), UnityLicenseTypeParameter.PROFESSIONAL),
        arrayOf(mapOf("unityLicenseType" to "personalLicense"), UnityLicenseTypeParameter.PERSONAL),
        arrayOf(mapOf("notRelevantParam" to "some value"), null),
        arrayOf(emptyMap<String, String>(), null),
    )

    @Test(dataProvider = "license type test data")
    fun `should return unity license type from build feature parameters`(
        params: Map<String, String>,
        expectedLicenseType: UnityLicenseTypeParameter?,
    ) {
        // arrange
        every { runnerContext.runnerParameters } returns mapOf()
        every { buildFeature.parameters } returns params

        // act
        val result = runnerContext.unityLicenseTypeParam()

        // assert
        if (expectedLicenseType == null) {
            result shouldBe null
        } else {
            result!! shouldBeEqual expectedLicenseType
        }
    }

    @DataProvider
    fun `license content test data`(): Array<Array<Any?>> = arrayOf(
        arrayOf(mapOf("secure:unityPersonalLicenseContent" to "someContent"), "someContent"),
        arrayOf(emptyMap<String, String>(), null),
    )

    @Test(dataProvider = "license content test data")
    fun `should return unity personal license content from build feature parameters`(
        params: Map<String, String>,
        expectedContent: String?,
    ) {
        // arrange
        every { runnerContext.runnerParameters } returns mapOf()
        every { buildFeature.parameters } returns params

        // act
        val result = runnerContext.unityPersonalLicenseContentParam()

        // assert
        if (expectedContent == null) {
            result shouldBe null
        } else {
            result!! shouldBeEqual expectedContent
        }
    }

    @DataProvider
    fun `license scope test data`(): Array<Array<Any?>> = arrayOf(
        arrayOf(mapOf("unityLicenseScope" to "buildStep"), BUILD_STEP),
        arrayOf(mapOf("unityLicenseScope" to "buildConfiguration"), BUILD_CONFIGURATION),
        arrayOf(mapOf("notRelevantParam" to "some value"), null),
        arrayOf(emptyMap<String, String>(), null),
    )

    @Test(dataProvider = "license scope test data")
    fun `should return unity license scope from build feature parameters`(
        params: Map<String, String>,
        expectedLicenseScope: UnityLicenseScope?,
    ) {
        // arrange
        every { runnerContext.runnerParameters } returns mapOf()
        every { buildFeature.parameters } returns params

        // act
        val result = build.unityLicenseScopeParam()

        // assert
        if (expectedLicenseScope == null) {
            result shouldBe null
        } else {
            result!! shouldBeEqual expectedLicenseScope
        }
    }
}
