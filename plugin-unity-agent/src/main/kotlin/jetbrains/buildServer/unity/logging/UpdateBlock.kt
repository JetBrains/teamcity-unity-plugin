

package jetbrains.buildServer.unity.logging

class UpdateBlock : LogBlock {

    override val name = "Update"

    override val logFirstLine = LogType.Inside

    override val logLastLine = LogType.Inside

    override fun isBlockStart(text: String) = blockStart.containsMatchIn(text)

    override fun isBlockEnd(text: String) = blockEnd.containsMatchIn(text)

    override fun getText(text: String) = text

    companion object {
        private val blockStart = Regex("Updating (.+) - GUID: .*")
        private val blockEnd = Regex("\\s*done(: hash -|\\. \\[Time:) .+")
    }
}