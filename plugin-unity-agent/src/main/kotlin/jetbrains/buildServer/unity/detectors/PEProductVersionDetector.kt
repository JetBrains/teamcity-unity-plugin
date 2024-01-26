

package jetbrains.buildServer.unity.detectors

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.unity.UnityVersion
import jetbrains.buildServer.unity.util.Completed
import jetbrains.buildServer.unity.util.Error
import jetbrains.buildServer.unity.util.Timeout
import jetbrains.buildServer.unity.util.execute
import jetbrains.buildServer.util.PEReader.PEUtil
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.exists

class PEProductVersionDetector {
    private val detectorToolPath = "../plugins/teamcity-unity-agent/tools/PeProductVersionDetector.exe"

    fun detect(executable: File): UnityVersion? {
        val version = PEUtil.getProductVersion(executable)

        if (version != null) {
            return UnityVersion(version.p1, version.p2, version.p3)
        }

        LOG.debug("unable to detect version via PEUtil, falling back to embedded tool")

        return try {
            detectWithEmbeddedTool(executable)
        } catch (e: Exception) {
            LOG.debug("Something went wrong during product version detection via embedded tool", e)
            null
        }
    }

    private fun detectWithEmbeddedTool(executable: File): UnityVersion? {
        val detectorPath = Paths.get(detectorToolPath).toAbsolutePath()

        if (!detectorPath.exists()) {
            LOG.debug("PE product version detection tool missing")
            return null
        }

        fun parseStdoutToVersion(stdout: String): UnityVersion? {
            return try {
                val versionObject = Json.decodeFromString<ProductVersion?>(stdout)

                if (versionObject?.majorPart != null) {
                    UnityVersion(versionObject.majorPart, versionObject.minorPart, versionObject.buildPart)
                } else {
                    LOG.info("version does not contain major part. stdout: $stdout")
                    null
                }
            } catch (e: Throwable) {
                LOG.info("Unable to parse stdout to version. stdout: $stdout")
                null
            }
        }

        when (val result = ProcessBuilder(detectorPath.toString(), executable.absolutePath).execute(timeoutSeconds = 3)) {
            is Completed -> when (result.exitCode) {
                0 -> parseStdoutToVersion(result.stdout)
                else -> LOG.info("Version detection process exited with non-zero code: ${result.exitCode}\n" +
                        "Error: ${result.stderr}")
            }
            is Error -> LOG.info(result.exception)
            Timeout -> LOG.info("PE product version detection timed out")
        }

        return null
    }

    @Serializable
    private data class ProductVersion(
        @SerialName("MajorPart") val majorPart: Int?,
        @SerialName("MinorPart") val minorPart: Int?,
        @SerialName("BuildPart") val buildPart: Int?,
        @SerialName("PrivatePart") val privatePart: Int?
    )

    companion object {
        private val LOG = Logger.getInstance(PEProductVersionDetector::class.java.name)
    }
}