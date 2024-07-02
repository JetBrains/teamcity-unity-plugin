

package jetbrains.buildServer.unity.fetchers

import jetbrains.buildServer.serverSide.DataItem
import jetbrains.buildServer.serverSide.ProjectDataFetcher
import jetbrains.buildServer.util.browser.Browser
import java.io.File

class UnityStaticMethodFetcher : ProjectDataFetcher {

    override fun getType() = "UnityStaticMethod"

    override fun retrieveData(fsBrowser: Browser, projectPath: String): MutableList<DataItem> {
        val items = mutableListOf<DataItem>()

        fsBrowser.getElement(File(projectPath, "Assets/Editor").path)
            ?.children
            ?.forEach { file ->
                if (!file.isLeaf || !file.name.endsWith(".cs") || !file.isContentAvailable) return@forEach
                CSharpFileParser.readStaticMethods(file.inputStream).entries.forEach { (key, value) ->
                    items.add(DataItem(key, value))
                }
            }

        return items
    }
}
