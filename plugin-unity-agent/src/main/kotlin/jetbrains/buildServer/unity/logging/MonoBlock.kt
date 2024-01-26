

package jetbrains.buildServer.unity.logging

class MonoBlock :LogBlock {

    override val name = "Initialize mono"

    override val logFirstLine = LogType.None

    override val logLastLine = LogType.Inside

    override fun isBlockStart(text: String) = text.contains(name)

    override fun isBlockEnd(text: String) = text.contains("Mono: successfully reloaded assembly")

    override fun getText(text: String) = text
}