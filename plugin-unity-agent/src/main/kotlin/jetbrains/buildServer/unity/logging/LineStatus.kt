package jetbrains.buildServer.unity.logging

enum class LineStatus {
    Normal,
    Warning,
    Error,
    ;

    companion object {
        fun parse(text: String): LineStatus? {
            return entries.firstOrNull {
                it.name.equals(text.trim(), true)
            }
        }
    }
}
