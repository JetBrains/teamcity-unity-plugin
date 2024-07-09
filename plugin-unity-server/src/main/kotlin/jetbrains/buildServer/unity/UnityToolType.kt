

package jetbrains.buildServer.unity

import jetbrains.buildServer.tools.ToolTypeAdapter

class UnityToolType : ToolTypeAdapter() {

    override fun getType(): String {
        return UnityConstants.UNITY_TOOL_NAME
    }

    override fun getDisplayName(): String {
        return UnityConstants.UNITY_TOOL_DISPLAY_NAME
    }

    override fun getShortDisplayName(): String {
        return displayName
    }
    override fun getDescription(): String {
        return UnityConstants.UNITY_TOOL_DESCRIPTION
    }

    override fun isSupportUpload(): Boolean {
        return true
    }

    override fun isSupportDownload(): Boolean {
        return false
    }

    override fun isSingleton(): Boolean {
        return false
    }

    override fun isServerOnly(): Boolean {
        return false
    }

    override fun getValidPackageDescription(): String {
        return """
                | Specify the path to a $displayName (.${UnityConstants.UNITY_TOOL_EXTENSION}).
                | <br/>Download <em>$type-&lt;VERSION&gt;.${UnityConstants.UNITY_TOOL_EXTENSION}</em> from
                | <a href="https://unity.com/releases/editor/archive" target="_blank" rel="noreferrer">www.unity3d.com</a>
        """.trimMargin()
    }
}
