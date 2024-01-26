

package jetbrains.buildServer.unity.logging

interface LogBlock {
    val name: String
    val logFirstLine: LogType
    val logLastLine: LogType
    fun isBlockStart(text: String): Boolean
    fun isBlockEnd(text: String): Boolean
    fun getText(text: String): String
}

enum class LogType {
    None,
    Inside,
    Outside
}