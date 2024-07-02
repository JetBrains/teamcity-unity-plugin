package jetbrains.buildServer.unity

enum class UnityLicenseTypeParameter(
    val id: String,
    val displayName: String,
) {
    PROFESSIONAL("professionalLicense", "Unity Plus or Pro license"),
    PERSONAL("personalLicense", "Unity Personal license"),
    ;

    companion object {

        fun from(licenseId: String): UnityLicenseTypeParameter? = when (licenseId) {
            PROFESSIONAL.id -> PROFESSIONAL
            PERSONAL.id -> PERSONAL
            else -> null
        }
    }
}
