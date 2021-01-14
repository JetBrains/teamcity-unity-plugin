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

import jetbrains.buildServer.serverSide.DataItem
import jetbrains.buildServer.serverSide.ProjectDataFetcher
import jetbrains.buildServer.util.browser.Browser
import java.io.File

class UnityStaticMethodFetcher : ProjectDataFetcher {

    override fun getType() = "UnityStaticMethod"

    override fun retrieveData(fsBrowser: Browser, projectPath: String): MutableList<DataItem> {
        val items = mutableListOf<DataItem>()

        fsBrowser.getElement(File(projectPath, "Assets/Editor").path)
                ?.children
                ?.forEach { file ->
                    if (!file.isLeaf || !file.name.endsWith(".cs") || !file.isContentAvailable) return@forEach
                    CSharpFileParser.readStaticMethods(file.inputStream).entries.forEach { (key, value) ->
                        items.add(DataItem(key, value))
                    }
                }

        return items
    }
}