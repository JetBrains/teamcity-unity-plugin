package jetbrains.buildServer.unity

import com.intellij.openapi.diagnostic.Logger
import com.vdurmont.semver4j.Semver
import com.vdurmont.semver4j.Semver.SemverType.LOOSE
import kotlin.LazyThreadSafetyMode.NONE

data class UnityVersion(val major: Int, val minor: Int? = null, val patch: Int? = null) : Comparable<UnityVersion> {

    private constructor(semver: Semver) : this(semver.major, semver.minor, semver.patch)

    private val semver: Semver by lazy(mode = NONE) {
        val version = StringBuilder()
        version.append(major)
        if (minor != null) version.append(".$minor")
        if (patch != null) version.append(".$patch")

        Semver(version.toString(), LOOSE)
    }

    fun nextMajor() = UnityVersion(semver.toStrict().nextMajor())
    fun nextMinor() = UnityVersion(semver.toStrict().nextMinor())

    override fun compareTo(other: UnityVersion): Int = semver.compareTo(other.semver)
    override fun toString(): String = semver.value

    object UnitySpecialVersions {
        val UNITY_2018_2_0 = parseVersion("2018.2.0")
        val UNITY_2019_1_0 = parseVersion("2019.1.0")
    }

    companion object {

        private val LOG = Logger.getInstance(UnityVersion::class.java.name)

        /*
         * Unity version looks like that: 2017.1.1f1
         * where suffix could be the following:
         * a  - alpha
         * b  - beta
         * p  - patch
         * rc - release candidate
         * f  - final
         */
        fun tryParseVersion(value: String?): UnityVersion? {
            val version = value
                ?.split("a", "b", "p", "rc", "f")
                ?.firstOrNull()

            if (version != null) {
                return try {
                    UnityVersion(Semver(version, LOOSE))
                } catch (e: Exception) {
                    LOG.error("Failed to parse Unity version. Error: ${e.message}")
                    null
                }
            }

            return null
        }

        fun parseVersion(value: String): UnityVersion {
            return tryParseVersion(value) ?: throw InvalidUnityVersionException("Failed to parse Unity version: $value")
        }
    }
}
