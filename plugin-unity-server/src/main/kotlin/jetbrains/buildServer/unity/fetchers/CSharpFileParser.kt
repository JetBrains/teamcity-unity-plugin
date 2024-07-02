

package jetbrains.buildServer.unity.fetchers

import com.intellij.openapi.diagnostic.Logger
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.jetbrains.unity.CSharpLexer
import org.jetbrains.unity.CSharpParser
import java.io.InputStream

object CSharpFileParser {

    fun readStaticMethods(inputStream: InputStream): Map<String, String?> {
        val targetNamesListener = UnityStaticMethodNamesListener()
        inputStream.bufferedReader().use {
            try {
                val buildFileLexer = CSharpLexer(CharStreams.fromReader(it))
                val tokens = CommonTokenStream(buildFileLexer)
                val buildFileParser = CSharpParser(tokens)
                ParseTreeWalker().walk(targetNamesListener, buildFileParser.compilation_unit())
            } catch (e: Exception) {
                LOG.infoAndDebugDetails("Failed to read C# file", e)
            }
        }
        return targetNamesListener.names
    }

    private val LOG = Logger.getInstance(CSharpFileParser::class.java.name)
}
