

package jetbrains.buildServer.unity.logging

class PackageManagerBlock : LogBlock {

    override val name = blockName

    override val logFirstLine = LogType.Inside

    override val logLastLine = LogType.Inside

    override fun isBlockStart(text: String) = blockStart.containsMatchIn(text)

    override fun isBlockEnd(text: String) = blockEnd.containsMatchIn(text)

    override fun getText(text: String) = text.removePrefix(prefix)

    companion object {
        private const val blockName = "Package Manager"
        private const val prefix = "[$blockName] "
        private val blockStart = Regex(Regex.escape(prefix) + "Registering \\d+ packages?:")
        private val blockEnd = Regex(Regex.escape(prefix) + "Done registering packages in [^\\s]+ seconds")
    }
}