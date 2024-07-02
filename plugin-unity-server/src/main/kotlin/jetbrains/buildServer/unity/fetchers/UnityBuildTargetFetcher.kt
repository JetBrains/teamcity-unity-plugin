

package jetbrains.buildServer.unity.fetchers

import jetbrains.buildServer.serverSide.DataItem
import jetbrains.buildServer.serverSide.ProjectDataFetcher
import jetbrains.buildServer.util.browser.Browser

class UnityBuildTargetFetcher : ProjectDataFetcher {

    override fun getType() = "UnityBuildTarget"

    override fun retrieveData(fsBrowser: Browser, projectFilePath: String) = players.map {
        DataItem(it, null)
    }.toMutableList()

    companion object {
        private val players = listOf(
            "Android",
            "EmbeddedLinux",
            "GameCoreXboxOne",
            "GameCoreXboxSeries",
            "iOS",
            "Linux64",
            "LinuxHeadlessSimulation",
            "OSXUniversal",
            "PS4",
            "PS5",
            "QNX",
            "Standalone",
            "StandaloneLinux64",
            "StandaloneOSX",
            "StandaloneWindows",
            "StandaloneWindows64",
            "Switch",
            "tvOS",
            "WebGL",
            "WindowsStoreApps",
            "XboxOne",
        )
    }
}
