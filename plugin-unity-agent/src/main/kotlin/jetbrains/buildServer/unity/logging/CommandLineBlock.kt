

package jetbrains.buildServer.unity.logging

class CommandLineBlock : LogBlock {

    override val name = "Command line arguments"

    override val logFirstLine = LogType.None

    override val logLastLine = LogType.Outside

    override fun isBlockStart(text: String) = blockStart.containsMatchIn(text)

    override fun isBlockEnd(text: String) = blockEnd.containsMatchIn(text)

    override fun getText(text: String) = text

    companion object {
        private val blockStart = Regex("\\s*COMMAND LINE ARGUMENTS:")
        private val blockEnd = Regex("(Successfully changed|Couldn't set) project path to:")
    }
}