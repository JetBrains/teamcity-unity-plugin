package jetbrains.buildServer.unity

import com.intellij.openapi.diagnostic.Logger
import java.io.InputStream

enum class AssetPipelineVersion {
    V1,
    V2,
    ;

    companion object {
        fun from(value: String) = when (value) {
            "0" -> V1
            "1" -> V2
            else -> null
        }
    }
}

interface UnityProjectFilesAccessor {
    fun directory(name: String): UnityProjectFilesAccessor?
    fun file(name: String): InputStream?
}

class UnityProject(
    private val filesAccessor: UnityProjectFilesAccessor,
) {
    companion object {
        private const val PROJECT_VERSION_FILE_NAME = "ProjectVersion.txt"
        private const val EDITOR_SETTINGS_FILE_NAME = "EditorSettings.asset"
        private const val PROJECT_SETTINGS_DIR_NAME = "ProjectSettings"
        private val logger = Logger.getInstance(UnityProject::class.java.name)
    }

    private val configReader = UnityConfigReader()

    val assetPipelineVersion: AssetPipelineVersion? by lazy {
        val settings = filesAccessor
            .directory(PROJECT_SETTINGS_DIR_NAME)
            ?.file(EDITOR_SETTINGS_FILE_NAME)

        if (settings == null) {
            logger.debug(
                "Unable to detect asset pipeline version: either the Editor settings file " +
                    "'$EDITOR_SETTINGS_FILE_NAME' or the settings directory '$PROJECT_SETTINGS_DIR_NAME' was not found",
            )
            return@lazy null
        }

        val assetPipelineVersionString = configReader.readValue(settings, "m_AssetPipelineMode") ?: return@lazy null

        AssetPipelineVersion.from(assetPipelineVersionString)
            .also {
                if (it == null) {
                    logger.debug("Unable to parse asset pipeline version from $assetPipelineVersionString")
                } else {
                    logger.info("Asset Pipeline version $it picked up from the '$EDITOR_SETTINGS_FILE_NAME' file")
                }
            }
    }

    val unityVersion: UnityVersion? by lazy {
        val projectVersionFile = filesAccessor
            .directory(PROJECT_SETTINGS_DIR_NAME)
            ?.file(PROJECT_VERSION_FILE_NAME)

        if (projectVersionFile == null) {
            logger.debug(
                "Unable to detect Unity version associated with the project: either the project version file " +
                    "'$PROJECT_VERSION_FILE_NAME' or the settings directory '$PROJECT_SETTINGS_DIR_NAME' was not found",
            )

            return@lazy null
        }

        val versionString = configReader.readValue(projectVersionFile, "m_EditorVersion") ?: return@lazy null

        UnityVersion.tryParseVersion(versionString)
            .also {
                if (it == null) {
                    logger.debug("Unable to parse Unity version from $versionString")
                } else {
                    logger.info("Unity version $it picked up from the '$PROJECT_VERSION_FILE_NAME' file")
                }
            }
    }
}
