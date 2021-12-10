/*
 * Copyright 2000-2021 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.unity.fetchers

import com.intellij.openapi.diagnostic.Logger
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.jetbrains.unity.CSharpParser
import org.jetbrains.unity.CSharpLexer
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