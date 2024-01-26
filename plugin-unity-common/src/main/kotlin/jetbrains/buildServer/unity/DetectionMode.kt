

package jetbrains.buildServer.unity

enum class DetectionMode(val id: String, val description:String) {
    Auto("auto", "Auto"),
    Manual("manual", "Manual");

    companion object {
        fun tryParse(id: String): DetectionMode? {
            return values().singleOrNull { it.id.equals(id, true) }
        }
    }
}