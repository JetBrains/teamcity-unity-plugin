

package jetbrains.buildServer.unity.logging

class BuildReportBlock : LogBlock {

    override val name = blockName

    override val logFirstLine = LogType.None

    override val logLastLine = LogType.None

    override fun isBlockStart(text: String) = text.contains(blockName)

    override fun isBlockEnd(text: String) = blockEnd.containsMatchIn(text)

    override fun getText(text: String) = text

    companion object {
        private const val blockName = "Build Report"
        private val blockEnd = Regex("-{79}")
    }
}