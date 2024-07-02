

package jetbrains.buildServer.unity

enum class Verbosity(val id: String, val description: String) {
    Minimal("minimal", "Minimal"),
    Normal("normal", "Normal"),
    ;

    companion object {
        fun tryParse(id: String): Verbosity? {
            return Verbosity.values().singleOrNull { it.id.equals(id, true) }
        }
    }
}
