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

package jetbrains.buildServer.unity.logging

import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class LineStatusProvider {

    private val patterns = mutableListOf<Pair<Regex, LineStatus>>()

    constructor() {
        patterns.addAll(listOf(
                Regex(".*?warning CS\\d+.*?") to LineStatus.Warning,
                Regex("WARNING.*") to LineStatus.Warning,
                Regex(".*?error CS\\d+.*?") to LineStatus.Error,
                Regex("Compilation failed:.*") to LineStatus.Error,
                Regex("Scripts have compiler errors\\..*") to LineStatus.Error,
                Regex("Error building player because script class layout is incompatible between the editor and the player.") to LineStatus.Error,
                Regex("Fatal Error!.*") to LineStatus.Error,
                Regex("executeMethod method .+ threw exception") to LineStatus.Error,
                Regex("executeMethod class .+ could not be found") to LineStatus.Error,
                Regex("Couldn't set project path to:.+") to LineStatus.Error,
                Regex("Failed to activate/update license") to LineStatus.Error,
                Regex("Error building player .+") to LineStatus.Error
        ))
    }

    constructor(lineStatusesFile: File) {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()
        val document = documentBuilder.parse(lineStatusesFile)
        document.documentElement.normalize()
        document.getElementsByTagName("line").nodes<Element>().forEach {
            val level = it.getAttribute("level")?.let { value ->
                LineStatus.parse(value)
            } ?: return@forEach
            val message = it.getAttribute("message") ?: return@forEach

            patterns += Regex(message) to level
        }
    }

    /**
     * Provides line status details.
     * @text is a text line.
     * @return status.
     */
    fun getLineStatus(text: String): LineStatus {
        return patterns.firstOrNull {
            it.first.containsMatchIn(text)
        }?.second ?: LineStatus.Normal
    }

    companion object {
        inline fun <reified T> NodeList.nodes(): Sequence<T> {
            val list = this
            return sequence {
                for (i in 0..list.length) {
                    yield(list.item(i))
                }
            }.filterIsInstance<T>()
        }
    }
}