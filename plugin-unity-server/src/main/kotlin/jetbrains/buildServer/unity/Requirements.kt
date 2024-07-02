

package jetbrains.buildServer.unity

import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType

object Requirements {
    object Unity {
        fun create(unityVersion: String): Requirement {
            val unityPath = buildString {
                append(escapeRegex(UnityConstants.UNITY_CONFIG_NAME))
                append(
                    if (unityVersion.isNotBlank()) {
                        escapeRegex(unityVersion) + ".*"
                    } else {
                        ".+"
                    },
                )
            }

            return Requirement(RequirementQualifier.EXISTS_QUALIFIER + unityPath, null, RequirementType.EXISTS)
        }

        private fun escapeRegex(value: String) =
            if (value.contains('%')) value else value.replace(".", "\\.")
    }
}
