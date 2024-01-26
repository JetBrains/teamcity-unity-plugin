

package jetbrains.buildServer.unity.logging

class DefaultBlock : LogBlock {

    override val name = ""

    override val logFirstLine = LogType.Inside

    override val logLastLine = LogType.Inside

    override fun isBlockStart(text: String) = false

    override fun isBlockEnd(text: String) = false

    override fun getText(text: String) = text
}