/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.unity

import com.intellij.openapi.diagnostic.Logger
import com.vdurmont.semver4j.Semver
import jetbrains.buildServer.util.PEReader.PEUtil
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.lang.Exception
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.exists

class PEProductVersionDetector {
    private val detectorToolPath = "../plugins/teamcity-unity-agent/tools/PeProductVersionDetector.exe"

    fun detect(executable: File): Semver? {
        val version = PEUtil.getProductVersion(executable)

        if (version != null) {
            return Semver("${version.p1}.${version.p2}.${version.p3}", Semver.SemverType.LOOSE)
        }

        LOG.debug("unable to detect version via PEUtil, falling back to embedded tool")

        return try {
            detectWithEmbeddedTool(executable)
        } catch (e: Exception) {
            LOG.debug("Something went wrong during product version detection via embedded tool", e)
            null
        }
    }

    private fun detectWithEmbeddedTool(executable: File): Semver? {
        val detectorPath = Paths.get(detectorToolPath).toAbsolutePath()

        if (!detectorPath.exists()) {
            LOG.debug("PE product version detection tool missing")
            return null
        }

        val detector = ProcessBuilder(detectorPath.toString(), executable.absolutePath).start()

        val detectionTimeoutSeconds = 3L
        if (detector.waitFor(detectionTimeoutSeconds, TimeUnit.SECONDS)) {
            return if (detector.exitValue() == 0) {
                val versionJson = detector.inputStream
                    .bufferedReader(Charsets.UTF_8)
                    .use(BufferedReader::readText)

                val versionObject = Json.decodeFromString<ProductVersion?>(versionJson)

                if (versionObject?.majorPart != null) {
                    Semver("${versionObject.majorPart}.${versionObject.minorPart}.${versionObject.buildPart}",
                        Semver.SemverType.LOOSE)
                } else {
                    null
                }
            } else {
                val error = detector.errorStream
                    .bufferedReader(Charsets.UTF_8)
                    .use(BufferedReader::readText)

                LOG.debug(error)
                null
            }
        } else {
            detector.destroyForcibly()
            LOG.debug("PE product version detection timed out")
            return null
        }
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