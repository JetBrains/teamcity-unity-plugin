

package jetbrains.buildServer.unity.logging

class PrepareBlock : LogBlock {

    override val name = "Prepare Build"

    override val logFirstLine = LogType.None

    override val logLastLine = LogType.None

    override fun isBlockStart(text: String) = text.contains("---- PrepareBuild Start ----")

    override fun isBlockEnd(text: String) = text.contains("---- PrepareBuild End ----")

    override fun getText(text: String) = text
}
