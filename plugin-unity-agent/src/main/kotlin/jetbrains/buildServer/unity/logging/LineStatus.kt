/*
 * Copyright 2000-2019 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.unity.logging

enum class LineStatus {
    Normal,
    Warning,
    Error;

    companion object {
        fun parse(text: String): LineStatus? {
            return values().firstOrNull {
                it.name.equals(text.trim(), true)
            }
        }
    }
}