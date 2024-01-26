

package jetbrains.buildServer.unity

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.serverSide.discovery.BreadthFirstRunnerDiscoveryExtension
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject
import jetbrains.buildServer.util.browser.Element

private data class DiscoveredUnityProject(
    val path: String,
    val unityVersion: UnityVersion? = null,
): DiscoveredObject(UnityConstants.RUNNER_TYPE, buildMap {
    put(UnityConstants.PARAM_PROJECT_PATH, path)

    unityVersion?.let {
        put(UnityConstants.PARAM_UNITY_VERSION, unityVersion.toString())
    }
})

class UnityRunnerDiscoveryExtension(
    private val projectAssociatedUnityVersionIdentifier: ProjectAssociatedUnityVersionIdentifier,
) : BreadthFirstRunnerDiscoveryExtension(DEPTH_LIMIT) {
    companion object {
        private const val DEPTH_LIMIT = 3
        private const val PROJECT_SETTINGS_DIR = "ProjectSettings"
        private val PROJECTS_DIRS = listOf("Assets", PROJECT_SETTINGS_DIR)
        private val logger = Logger.getInstance(UnityRunnerDiscoveryExtension::class.java.name)
    }

    override fun discoverRunnersInDirectory(
        dir: Element,
        filesAndDirs: MutableList<Element>
    ): MutableList<DiscoveredObject> {
        if (!dir.isUnityProjectDirectory()) {
            logger.debug("Directory: ${dir.fullName} seems not to be a Unity project directory, skipping")
            return mutableListOf()
        }

        val unityVersion = projectAssociatedUnityVersionIdentifier.identify(
            object : UnityProjectFilesAccessor {
                private var current = dir
                override fun directory(name: String): UnityProjectFilesAccessor? {
                    current = current.children
                        ?.firstOrNull { it.name == name } ?: return null
                    return this
                }

                override fun file(name: String) = current.children
                    ?.filter { it.isContentAvailable }
                    ?.firstOrNull { it.name == name }
                    ?.inputStream
            })

        logger.info("Unity project was found in directory '${dir.fullName}'${if (unityVersion == null) "" else ", associated Unity version: '$unityVersion'"}")
        return mutableListOf(DiscoveredUnityProject(dir.fullName, unityVersion))
    }

    private fun Element.isUnityProjectDirectory(): Boolean =
        children
            ?.filter { !it.isLeaf }
            ?.map { it.name }
            ?.containsAll(PROJECTS_DIRS)
            ?: false
}