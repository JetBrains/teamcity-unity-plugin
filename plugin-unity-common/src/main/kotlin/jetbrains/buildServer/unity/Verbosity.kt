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

package jetbrains.buildServer.unity

enum class Verbosity(val id: String, val description: String) {
    Minimal("minimal", "Minimal"),
    Normal("normal", "Normal");

    companion object {
        fun tryParse(id: String): Verbosity? {
            return Verbosity.values().singleOrNull { it.id.equals(id, true) }
        }
    }
}