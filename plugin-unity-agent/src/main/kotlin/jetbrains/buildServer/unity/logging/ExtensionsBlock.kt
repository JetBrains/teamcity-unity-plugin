

package jetbrains.buildServer.unity.logging

class ExtensionsBlock :LogBlock {

    override val name = "Initializing Unity extensions"

    override val logFirstLine = LogType.None

    override val logLastLine = LogType.Outside

    override fun isBlockStart(text: String) = text.contains("$name:")

    override fun isBlockEnd(text: String) = !blockItem.containsMatchIn(text)

    override fun getText(text: String) = text

    companion object {
        private val blockItem = Regex("'[^']+'\\s+GUID: .+")
    }
}