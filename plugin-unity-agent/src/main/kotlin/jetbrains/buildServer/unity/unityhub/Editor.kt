

package jetbrains.buildServer.unity.unityhub

import kotlinx.serialization.Serializable

@Serializable
data class Editor(val version: String, val location: List<String>?, val manual: Boolean)
