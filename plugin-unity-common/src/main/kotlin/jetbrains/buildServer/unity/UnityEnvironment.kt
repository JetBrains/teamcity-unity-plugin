package jetbrains.buildServer.unity

data class UnityEnvironment(
    val unityPath: String,
    val unityVersion: UnityVersion,
    val isVirtual: Boolean = false,
)
