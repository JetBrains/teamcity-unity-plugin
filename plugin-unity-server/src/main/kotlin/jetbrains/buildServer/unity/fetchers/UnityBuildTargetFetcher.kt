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
                "iOS",
                "Linux",
                "Linux64",
                "LinuxUniversal",
                "N3DS",
                "OSXUniversal",
                "PS4",
                "standalone",
                "Switch",
                "tvOS",
                "Web",
                "WebGL",
                "WebStreamed",
                "Win",
                "Win64",
                "WindowsStoreApps",
                "XboxOne"
        )
    }
}