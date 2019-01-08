/*
 * Copyright 2000-2019 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
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