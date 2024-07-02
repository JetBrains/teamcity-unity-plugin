package jetbrains.buildServer.unity

import com.intellij.openapi.diagnostic.Logger
import java.io.InputStream

// https://unity.com/blog/engine-platform/understanding-unitys-serialization-language-yaml
class UnityConfigReader {
    companion object {
        private const val KEY_VALUE_SEPARATOR = ":"
        private val logger = Logger.getInstance(UnityConfigReader::class.java.name)
    }

    fun readValue(stream: InputStream, key: String): String? = stream.bufferedReader()
        .useLines { lines ->
            lines
                .mapNotNull { it.splitToKeyValueOrNull() }
                .firstOrNull { (currentKey, _) -> currentKey == key }?.second
                .also { value ->
                    if (value != null) {
                        logger.debug("Value '$value' was found for the given key '$key'")
                    } else {
                        logger.debug("No entry with key '$key' was found in the given config file")
                    }
                }
        }

    private fun String.splitToKeyValueOrNull() = trim()
        .split(KEY_VALUE_SEPARATOR, limit = 2)
        .let {
            if (it.size == 2) {
                it[0].trim() to it[1].trim()
            } else {
                null
            }
        }
}
