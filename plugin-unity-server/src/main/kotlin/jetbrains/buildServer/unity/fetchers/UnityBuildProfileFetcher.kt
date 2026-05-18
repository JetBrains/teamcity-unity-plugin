package jetbrains.buildServer.unity.fetchers

import jetbrains.buildServer.serverSide.DataItem
import jetbrains.buildServer.serverSide.ProjectDataFetcher
import jetbrains.buildServer.util.browser.Browser
import java.io.File

class UnityBuildProfileFetcher : ProjectDataFetcher {

    override fun getType() = "UnityBuildProfile"

    override fun retrieveData(fsBrowser: Browser, projectFilePath: String): MutableList<DataItem> {
        val items = mutableListOf<DataItem>()

        val conventionalDir = fsBrowser.getElement(File(projectFilePath, CONVENTIONAL_PROFILES_DIR).path)
        if (conventionalDir != null) {
            BuildProfileScanner.collectProfiles(conventionalDir, CONVENTIONAL_PROFILES_DIR, maxDepth = 1)
                .mapTo(items) { DataItem(it, null) }
        }

        if (items.isEmpty()) {
            val assetsDir = fsBrowser.getElement(File(projectFilePath, ASSETS_DIR).path)
            if (assetsDir != null) {
                BuildProfileScanner.collectProfiles(assetsDir, ASSETS_DIR, maxDepth = FALLBACK_SCAN_DEPTH)
                    .mapTo(items) { DataItem(it, null) }
            }
        }

        return items
    }

    companion object {
        private const val ASSETS_DIR = "Assets"
        private const val CONVENTIONAL_PROFILES_DIR = "Assets/Settings/Build Profiles"
        private const val FALLBACK_SCAN_DEPTH = 3
    }
}
