package jetbrains.buildServer.unity.fetchers

import jetbrains.buildServer.unity.UnityConstants
import jetbrains.buildServer.util.browser.Element

internal object BuildProfileScanner {

    fun collectProfiles(
        rootElement: Element,
        relativePath: String,
        maxDepth: Int,
        verifyContent: Boolean = false,
        currentDepth: Int = 0,
    ): List<String> {
        if (currentDepth > maxDepth) return emptyList()
        val children = rootElement.children ?: return emptyList()
        return children.flatMap { child ->
            val childPath = "$relativePath/${child.name}"
            when {
                child.isLeaf && child.name.endsWith(UnityConstants.ASSET_FILE_EXTENSION) ->
                    if (!verifyContent || isBuildProfileAsset(child)) listOf(childPath) else emptyList()
                !child.isLeaf ->
                    collectProfiles(child, childPath, maxDepth, verifyContent, currentDepth + 1)
                else -> emptyList()
            }
        }
    }

    fun isBuildProfileAsset(element: Element): Boolean {
        return try {
            element.inputStream?.bufferedReader()?.use { reader ->
                reader.lineSequence().any { line -> line.trimStart().startsWith("BuildProfile:") }
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
}
