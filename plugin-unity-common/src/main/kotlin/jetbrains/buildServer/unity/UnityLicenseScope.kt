package jetbrains.buildServer.unity

enum class UnityLicenseScope(
    val id: String,
    val displayName: String,
) {
    BUILD_STEP("buildStep", "Build step"),
    BUILD_CONFIGURATION("buildConfiguration", "Build configuration"),
    ;

    companion object {
        val description = """
            Specify the scope for Unity license. The scope determines when the Unity license is activated and returned.
            ${BUILD_STEP.displayName} - activate and return for every Unity build step in the build configuration.
            ${BUILD_CONFIGURATION.displayName} - activate and return only once for the entire build configuration.
        """.trimIndent()

        fun from(id: String): UnityLicenseScope? = when (id) {
            BUILD_STEP.id -> BUILD_STEP
            BUILD_CONFIGURATION.id -> BUILD_CONFIGURATION
            else -> null
        }
    }
}
