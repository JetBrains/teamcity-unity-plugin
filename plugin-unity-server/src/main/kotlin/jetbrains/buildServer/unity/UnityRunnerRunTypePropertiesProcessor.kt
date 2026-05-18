package jetbrains.buildServer.unity

import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.util.PropertiesUtil

class UnityRunnerRunTypePropertiesProcessor : PropertiesProcessor {
    override fun process(properties: MutableMap<String, String>?): MutableCollection<InvalidProperty> {
        val invalidProperties = ArrayList<InvalidProperty>()

        if (properties?.get(UnityConstants.PARAM_DETECTION_MODE).equals(UnityConstants.DETECTION_MODE_MANUAL)) {
            val unityRoot = properties?.get(UnityConstants.PARAM_UNITY_ROOT)
            if (PropertiesUtil.isEmptyOrNull(unityRoot)) {
                invalidProperties.add(InvalidProperty(UnityConstants.PARAM_UNITY_ROOT, "Unity version must be specified"))
            }
        }

        val buildProfile = properties?.get(UnityConstants.PARAM_BUILD_PROFILE)?.trim()
        if (!buildProfile.isNullOrEmpty() && !buildProfile.endsWith(UnityConstants.ASSET_FILE_EXTENSION)) {
            invalidProperties.add(
                InvalidProperty(
                    UnityConstants.PARAM_BUILD_PROFILE,
                    "Build Profile path should point to a .asset file (e.g. Assets/Settings/Build Profiles/MyProfile.asset)",
                )
            )
        }

        return invalidProperties
    }
}
