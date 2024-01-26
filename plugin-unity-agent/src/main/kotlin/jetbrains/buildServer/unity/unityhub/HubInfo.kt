

package jetbrains.buildServer.unity.unityhub

import kotlinx.serialization.Serializable

@Serializable
data class HubInfo(val version: String, val executablePath: String)