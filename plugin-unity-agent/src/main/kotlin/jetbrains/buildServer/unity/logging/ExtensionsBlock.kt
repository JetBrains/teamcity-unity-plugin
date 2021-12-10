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

class ExtensionsBlock :LogBlock {

    override val name = "Initializing Unity extensions"

    override val logFirstLine = LogType.None

    override val logLastLine = LogType.Outside

    override fun isBlockStart(text: String) = text.contains("$name:")

    override fun isBlockEnd(text: String) = !blockItem.containsMatchIn(text)

    override fun getText(text: String) = text

    companion object {
        private val blockItem = Regex("'[^']+'\\s+GUID: .+")
    }
}