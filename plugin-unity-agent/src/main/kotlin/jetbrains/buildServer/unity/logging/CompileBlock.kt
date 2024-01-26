

package jetbrains.buildServer.unity.logging

class CompileBlock : LogBlock {

    override val name = "Compile"

    override val logFirstLine = LogType.None

    override val logLastLine = LogType.None

    override fun isBlockStart(text: String) = text.contains("-----Compiler Commandline Arguments:")

    override fun isBlockEnd(text: String) = text.contains("-----EndCompilerOutput---------------")

    override fun getText(text: String) = text
}