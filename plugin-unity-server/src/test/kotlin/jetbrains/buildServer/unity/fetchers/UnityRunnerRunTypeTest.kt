

package jetbrains.buildServer.unity.fetchers

import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.RunTypeRegistry
import jetbrains.buildServer.unity.UnityConstants
import jetbrains.buildServer.unity.UnityLicenseScope
import jetbrains.buildServer.unity.UnityRunnerRunType
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class UnityRunnerRunTypeTest {

    @DataProvider
    fun runnerRequirementsData(): Array<Array<Any?>> {
        return arrayOf(
            arrayOf<Any?>(
                emptyMap<String, String>(),
                emptyList<Requirement>(),
            ),
            arrayOf<Any?>(
                mapOf(
                    UnityConstants.PARAM_DETECTION_MODE to UnityConstants.DETECTION_MODE_AUTO,
                    UnityConstants.PARAM_UNITY_VERSION to "",
                ),
                listOf(Requirement("Exists=>unity\\.path\\..+", null, RequirementType.EXISTS)),
            ),
            arrayOf<Any?>(
                mapOf(
                    UnityConstants.PARAM_DETECTION_MODE to UnityConstants.DETECTION_MODE_MANUAL,
                    UnityConstants.PARAM_UNITY_ROOT to "C:\\My\\Custom\\Unity",
                ),
                emptyList<Requirement>(),
            ),
            arrayOf<Any?>(
                mapOf(UnityConstants.PARAM_UNITY_VERSION to "2018.2"),
                listOf(Requirement("Exists=>unity\\.path\\.2018\\.2.*", null, RequirementType.EXISTS)),
            ),
            arrayOf<Any?>(
                mapOf(UnityConstants.PARAM_UNITY_VERSION to "%SOME_VAR.1%"),
                listOf(Requirement("Exists=>unity\\.path\\.%SOME_VAR.1%.*", null, RequirementType.EXISTS)),
            ),
            arrayOf<Any?>(
                mapOf(
                    UnityConstants.PARAM_DETECTION_MODE to UnityConstants.DETECTION_MODE_MANUAL,
                    UnityConstants.PARAM_UNITY_VERSION to "2018.2",
                ),
                emptyList<Requirement>(),
            ),
            arrayOf<Any?>(
                mapOf(
                    UnityConstants.PARAM_DETECTION_MODE to UnityConstants.DETECTION_MODE_AUTO,
                    UnityConstants.PARAM_UNITY_VERSION to "2018.2",
                ),
                listOf(Requirement("Exists=>unity\\.path\\.2018\\.2.*", null, RequirementType.EXISTS)),
            ),
            arrayOf(
                mapOf(
                    UnityConstants.PARAM_DETECTION_MODE to UnityConstants.DETECTION_MODE_AUTO,
                    UnityConstants.PARAM_UNITY_VERSION to "",
                    UnityConstants.PLUGIN_DOCKER_IMAGE to "some-image",
                ),
                emptyList<Requirement>(),
            ),
        )
    }

    @DataProvider
    fun runnerDefaultData(): Array<Array<Any?>> {
        return arrayOf(
            arrayOf<Any?>(
                mapOf(
                    UnityConstants.PARAM_DETECTION_MODE to UnityConstants.DETECTION_MODE_AUTO,
                    UnityConstants.PARAM_UNITY_LICENSE_SCOPE to UnityLicenseScope.BUILD_STEP.id,
                ),
            ),
        )
    }

    @Test(dataProvider = "runnerRequirementsData")
    fun testRunnerRequirements(parameters: Map<String, String>, expectedRequirements: List<Requirement>) {
        val m = Mockery()
        val pluginDescriptor = m.mock(PluginDescriptor::class.java)
        val runTypeRegistry = m.mock(RunTypeRegistry::class.java)

        m.checking(object : Expectations() {
            init {
                oneOf(runTypeRegistry).registerRunType(with(any(UnityRunnerRunType::class.java)))
            }
        })

        val runType = UnityRunnerRunType(pluginDescriptor, runTypeRegistry)

        val requirements = runType.getRunnerSpecificRequirements(parameters)
        Assert.assertEquals(requirements, expectedRequirements)
    }

    @Test(dataProvider = "runnerDefaultData")
    fun testDefaultParameters(expectedParameters: Map<String, String>) {
        val m = Mockery()
        val pluginDescriptor = m.mock(PluginDescriptor::class.java)
        val runTypeRegistry = m.mock(RunTypeRegistry::class.java)

        m.checking(object : Expectations() {
            init {
                oneOf(runTypeRegistry).registerRunType(with(any(UnityRunnerRunType::class.java)))
            }
        })

        val runType = UnityRunnerRunType(pluginDescriptor, runTypeRegistry)

        val defaultParameters = runType.defaultRunnerProperties
        Assert.assertEquals(defaultParameters, expectedParameters)
    }
}
