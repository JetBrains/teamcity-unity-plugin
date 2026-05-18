package jetbrains.buildServer.unity.fetchers

import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.unity.UnityConstants
import jetbrains.buildServer.unity.UnityRunnerRunTypePropertiesProcessor
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class UnityRunnerRunTypePropertiesProcessorTest {

    @DataProvider
    fun processData(): Array<Array<Any?>> {
        return arrayOf(
            arrayOf<Any?>(
                emptyMap<String, String>(),
                listOf<InvalidProperty>(),
            ),
            arrayOf<Any?>(
                mutableMapOf(UnityConstants.PARAM_UNITY_ROOT to "C:\\Program Files\\Unity\\2018.4.9f1"),
                listOf<InvalidProperty>(),
            ),
            arrayOf<Any?>(
                mutableMapOf(UnityConstants.PARAM_UNITY_ROOT to ""),
                listOf<InvalidProperty>(),
            ),
            arrayOf<Any?>(
                mutableMapOf(
                    UnityConstants.PARAM_DETECTION_MODE to UnityConstants.DETECTION_MODE_MANUAL,
                    UnityConstants.PARAM_UNITY_ROOT to "C:\\Program Files\\Unity\\2018.4.9f1",
                ),
                listOf<InvalidProperty>(),
            ),
            arrayOf<Any?>(
                mutableMapOf(
                    UnityConstants.PARAM_DETECTION_MODE to UnityConstants.DETECTION_MODE_MANUAL,
                    UnityConstants.PARAM_UNITY_ROOT to "",
                ),
                listOf(InvalidProperty(UnityConstants.PARAM_UNITY_ROOT, "")),
            ),
            arrayOf<Any?>(
                mutableMapOf(
                    UnityConstants.PARAM_DETECTION_MODE to UnityConstants.DETECTION_MODE_AUTO,
                    UnityConstants.PARAM_UNITY_ROOT to "C:\\Program Files\\Unity\\2018.4.9f1",
                ),
                listOf<InvalidProperty>(),
            ),
            arrayOf<Any?>(
                mutableMapOf(
                    UnityConstants.PARAM_DETECTION_MODE to UnityConstants.DETECTION_MODE_AUTO,
                    UnityConstants.PARAM_UNITY_ROOT to "",
                ),
                listOf<InvalidProperty>(),
            ),
        )
    }

    @Test(dataProvider = "processData")
    fun testProcess(parameters: MutableMap<String, String>, expectedInvalidProperties: List<InvalidProperty>) {
        val propertiesProcessorType = UnityRunnerRunTypePropertiesProcessor()

        val invalidProperties = propertiesProcessorType.process(parameters)

        Assert.assertEquals(invalidProperties.count(), expectedInvalidProperties.count())

        for ((actual, expected) in invalidProperties.zip(expectedInvalidProperties)) {
            Assert.assertEquals(actual.propertyName, expected.propertyName)
        }
    }

    @DataProvider
    fun buildProfileValidationData(): Array<Array<Any?>> {
        return arrayOf(
            arrayOf<Any?>("", false),
            arrayOf<Any?>("Assets/Settings/Build Profiles/Android.asset", false),
            arrayOf<Any?>("Assets/Settings/Build Profiles/Android", true),
            arrayOf<Any?>("Assets/Settings/Build Profiles/Android.txt", true),
        )
    }

    @Test(dataProvider = "buildProfileValidationData")
    fun `build profile path validation`(buildProfile: String, expectsInvalidProperty: Boolean) {
        val processor = UnityRunnerRunTypePropertiesProcessor()
        val params = mutableMapOf(UnityConstants.PARAM_BUILD_PROFILE to buildProfile)

        val invalidProperties = processor.process(params)

        val hasInvalidBuildProfile = invalidProperties.any { it.propertyName == UnityConstants.PARAM_BUILD_PROFILE }
        Assert.assertEquals(hasInvalidBuildProfile, expectsInvalidProperty)
    }
}
