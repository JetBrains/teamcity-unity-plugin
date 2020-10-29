/*
 * Copyright 2020 Aaron Zurawski
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

package jetbrains.buildServer.unity

import jetbrains.buildServer.tools.ToolTypeAdapter

class UnityToolType : ToolTypeAdapter() {

    override fun getType(): String {
        return UnityConstants.UNITY_TOOL_NAME
    }

    override fun getDisplayName(): String {
        return UnityConstants.UNITY_TOOL_DISPLAY_NAME
    }

    override fun getShortDisplayName(): String {
        return displayName
    }
    override fun getDescription(): String? {
        return UnityConstants.UNITY_TOOL_DESCRIPTION
    }

    override fun isSupportUpload(): Boolean {
        return true
    }

    override fun isSupportDownload(): Boolean {
        return false
    }

    override fun isSingleton(): Boolean {
        return false
    }

    override fun isServerOnly(): Boolean {
        return false
    }

    override fun getValidPackageDescription(): String? {
        return """Specify the path to a $displayName (.${UnityConstants.UNITY_TOOL_EXTENSION}).
                <br/>Download <em>$type-&lt;VERSION&gt;.${UnityConstants.UNITY_TOOL_EXTENSION}</em> from
                <a href=\"https://unity3d.com/get-unity/download/archive\" target=\"_blank\" rel=\"noreferrer\">www.unity3d.com</a>"""
    }
}