

package jetbrains.buildServer.unity.logging

import java.util.*

class RefreshBlock : LogBlock {

    override val name = blockName

    override val logFirstLine = LogType.Inside

    override val logLastLine = LogType.Inside

    override fun isBlockStart(text: String) = text.contains(prefix)

    override fun isBlockEnd(text: String) = text.isEmpty() || blockEnd.containsMatchIn(text)

    override fun getText(text: String) = text.removePrefix(prefix)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    companion object {
        private const val blockName = "Refresh"
        private const val prefix = "$blockName: "
        private val blockEnd = Regex("Refresh completed in [^\\s]+ seconds")
    }
}