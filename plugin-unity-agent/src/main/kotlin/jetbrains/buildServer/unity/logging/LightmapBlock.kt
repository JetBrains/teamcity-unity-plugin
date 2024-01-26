

package jetbrains.buildServer.unity.logging

class LightmapBlock : LogBlock {

    override val name = "Lightmap"

    override val logFirstLine = LogType.None

    override val logLastLine = LogType.None

    override fun isBlockStart(text: String) = blockStart.containsMatchIn(text)

    override fun isBlockEnd(text: String) = blockEnd.containsMatchIn(text)

    override fun getText(text: String) = text

    companion object {
        private val blockStart = Regex("---- Lightmapping Start for (.*) ----")
        private val blockEnd = Regex("---- Lightmapping End for (.*) ----")
    }
}