package jetbrains.buildServer.unity

import com.intellij.openapi.diagnostic.Logger
import java.io.InputStream

interface UnityProjectFilesAccessor {
    fun directory(name: String) : UnityProjectFilesAccessor?
    fun file(name: String) : InputStream?
}

class ProjectAssociatedUnityVersionIdentifier {
    companion object {
        const val PROJECT_VERSION_FILE_NAME = "ProjectVersion.txt"
        private val keyValueSeparator = "\\s".toRegex()
        private val logger = Logger.getInstance(ProjectAssociatedUnityVersionIdentifier::class.java.name)
    }

    fun identify(projectFilesAccessor: UnityProjectFilesAccessor): UnityVersion? {
        val projectVersionFile = projectFilesAccessor
            .directory("ProjectSettings")
            ?.file(PROJECT_VERSION_FILE_NAME)

        if (projectVersionFile == null) {
            logger.debug("$PROJECT_VERSION_FILE_NAME appears to be missing in the given Unity project, unable to identify version")
            return null
        }

        return projectVersionFile
            .bufferedReader()
            .useLines { lines ->
                for (line in lines ) {
                    val keyValue = line.split(keyValueSeparator)
                    if (keyValue.size != 2) {
                        logger.debug("Line doesn't seem to contain Unity version (without revision), skipping")
                        continue
                    }

                    if (keyValue.first() == "m_EditorVersion:") {
                        val versionString = keyValue.last()
                        val version = UnityVersion.tryParseVersion(versionString)

                        if (version != null) {
                            logger.info("Unity version $version picked up from the '$PROJECT_VERSION_FILE_NAME' file")
                            return@useLines version
                        }

                        logger.debug("Unable to parse Unity version from $versionString")
                    }

                    logger.debug("The key of the split line does not match 'm_EditorVersion:', skipping")
                }

                logger.info("No Unity version was found in '$PROJECT_VERSION_FILE_NAME' file")
                null
            }
    }
}